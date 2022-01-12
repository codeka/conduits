package com.codeka.justconduits.common.shape;

import com.codeka.justconduits.client.blocks.ConduitModelLoader;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.capabilities.network.ConduitType;
import com.codeka.justconduits.helpers.QuadHelper;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.function.Function;

/**
 * The {@link ShapeManager} is responsible for managing the different shapes of the blocks.
 *
 * <p>We build slightly different shapes to handle collisions, selections and rendering:
 * <ul>
 *   <li>For collisions, we have slightly simpler shapes, trying to join the connections together into larger shapes
 *       to make collisions easier on the game. We only generate shapes in the +ve direction, with the expectation that
 *       the block in the -ve direction will create it's own collision shape. This simplifies the shape overall (halving
 *       the total number of shapes needed).
 *   <li>For selections, we need to track which shapes belong with which connections so that we can actually perform
 *       the selection. We make the selection shape slightly larger than the rendered shape.
 *   <li>For rendering, it's the most complicated shape of all, as we have to worry about what textures to include
 *       in addition to just the shapes. Similar to the collision shape, we only generate shapes in the +ve direction.
 * </ul>
 *
 * <p>However, overall, the shapes should be similar enough that we can share a lot of the same logic.
 *
 * <p>One big function of this class is to cache the results. A conduit with the same connections should have exactly
 *    the same shape. Even if they are different types of conduit, only the texture are different.
 */
public class ShapeManager {
  private static final Logger L = LogManager.getLogger();

  private static final ShapeCache cache = new ShapeCache();

  // When there's more than 1 conduit, we add them to the block space using these center points.
  // TODO: it depends on what directions the conduits run where the centers should go.
  private static final Vec3[] CONDUIT_CENTER_OFFSETS = new Vec3[] {
      new Vec3(0.25f, 0.5f, 0.5f), new Vec3(0.75f, 0.5f, 0.5f), new Vec3(0.0f, 0.75f, 0.5f),
      new Vec3(0.0f, 0.25f, 0.5f), new Vec3(0.25f, 0.75f, 0.5f), new Vec3(0.75f, 0.75f, 0.5f),
      new Vec3(0.25f, 0.25f, 0.5f), new Vec3(0.75f, 0.25f, 0.5f)};

  private final ConduitBlockEntity conduitBlockEntity;

  private VoxelShape collisionShape;
  private VisualShape visualShape;
  private SelectionShape selectionShape;
  private boolean dirty;

  public ShapeManager(ConduitBlockEntity conduitBlockEntity) {
    dirty = true;
    this.conduitBlockEntity = conduitBlockEntity;
  }

  public VoxelShape getCollisionShape() {
    if (dirty) {
      updateShapes();
    }
    return collisionShape;
  }

  public ArrayList<BakedQuad> getBakedQuads(Function<Material, TextureAtlasSprite> spriteGetter) {
    if (dirty) {
      updateShapes();
    }
    return createBakedQuads(spriteGetter, visualShape);
  }

  public SelectionShape getSelectionShape() {
    if (dirty) {
      updateShapes();
    }
    return selectionShape;
  }

  /** Mark ourselves dirty. We'll update the shape the next time they're needed. */
  public void markDirty() {
    dirty = true;
  }

  /**
   * Called whenever the shape of the {@link ConduitBlockEntity} needs to be updated (e.g. when a connection changes,
   */
  private void updateShapes() {
    ConduitShape mainShape = generateMainShape(conduitBlockEntity);
    collisionShape = cache.getCollisionShape(conduitBlockEntity, () -> createCollisionShape(mainShape));
    visualShape = cache.getVisualShape(conduitBlockEntity, () -> createVisualShape(conduitBlockEntity, mainShape));
    selectionShape =
        cache.getSelectionShape(conduitBlockEntity, () -> createSelectionShape(conduitBlockEntity, mainShape));
    dirty = false;
  }

  /**
   * Generates the "main" shape of the conduit. The main shape is the center of each of the nobbly bits and a list of
   * directions that the "pipes" come out of.
   */
  private static ConduitShape generateMainShape(ConduitBlockEntity conduitBlockEntity) {
    ConduitShape mainShape = new ConduitShape();
    ArrayList<ConduitType> conduitTypes = new ArrayList<>(conduitBlockEntity.getConduitTypes());
    if (conduitTypes.size() == 1) {
      var shape = mainShape.addConduit(conduitTypes.get(0), new Vec3(0.5f, 0.5f, 0.5f));
      populateSingleShape(conduitBlockEntity, shape, conduitTypes.get(0));
    } else if (conduitTypes.size() > CONDUIT_CENTER_OFFSETS.length) {
      L.atInfo().log("Too many conduits, we can't draw it");
      return mainShape;
    } else {
      for (int i = 0; i < conduitTypes.size(); i++) {
        var shape = mainShape.addConduit(conduitTypes.get(i), CONDUIT_CENTER_OFFSETS[i]);
        populateSingleShape(conduitBlockEntity, shape, conduitTypes.get(i));
      }
    }

    // Add shapes for the external connections. For the external connections, we record the complete shape of the box
    // as the calculation is kind of complicated.
    for (ConduitConnection conn : conduitBlockEntity.getConnections()) {
      if (conn.getConnectionType() != ConduitConnection.ConnectionType.EXTERNAL) {
        continue;
      }

      // TODO: this is actually a lot simpler than I have it here.
      var dir = conn.getDirection();
      Vector3f normal = new Vector3f(dir.getStepX(), dir.getStepY(), dir.getStepZ());
      Vector3f min = new Vector3f(
          0.5f - (0.5f * (1.0f - Math.abs(normal.x()))) - 0.125f * Math.abs(normal.x()) + 0.375f * normal.x(),
          0.5f - (0.5f * (1.0f - Math.abs(normal.y()))) - 0.125f * Math.abs(normal.y()) + 0.375f * normal.y(),
          0.5f - (0.5f * (1.0f - Math.abs(normal.z()))) - 0.125f * Math.abs(normal.z()) + 0.375f * normal.z());
      Vector3f max = new Vector3f(
          0.5f + (0.5f * (1.0f - Math.abs(normal.x()))) + 0.125f * Math.abs(normal.x()) + 0.375f * normal.x(),
          0.5f + (0.5f * (1.0f - Math.abs(normal.y()))) + 0.125f * Math.abs(normal.y()) + 0.375f * normal.y(),
          0.5f + (0.5f * (1.0f - Math.abs(normal.z()))) + 0.125f * Math.abs(normal.z()) + 0.375f * normal.z());
      mainShape.addExternalConnectionShape(conn, min, max);
    }

    return mainShape;
  }

  private static void populateSingleShape(
      ConduitBlockEntity conduitBlockEntity, ConduitShape.SingleConduitShape shape, ConduitType conduitType) {
    for (ConduitConnection conn : conduitBlockEntity.getConnections()) {
      // TODO: check that there's actually another conduit of this type at the next block over.
      shape.addDirection(conn.getDirection());
    }
  }

  private static VoxelShape createCollisionShape(ConduitShape mainShape) {
    VoxelShape collisionShape = Shapes.empty();
    for (var shape : mainShape.getShapes().values()) {
      Vec3 c = shape.getCenter();
      VoxelShape voxelShape =
          Shapes.box(c.x - 0.125f, c.y - 0.125f, c.z - 0.125f, c.x + 0.125f, c.y + 0.125f, c.z + 0.125f);
      collisionShape = Shapes.joinUnoptimized(collisionShape, voxelShape, BooleanOp.OR);
      /*
      for (ConduitConnection conn : conduitBlockEntity.getConnections()) {
        shape = Shapes.or(shape, conn.getVoxelShape());
      }
      */
    }

    for (var shape : mainShape.getExternalConnectionShapes()) {
      collisionShape = Shapes.joinUnoptimized(collisionShape, shape.getVoxelShape(), BooleanOp.OR);
    }

    return collisionShape.optimize();
  }

  private static SelectionShape createSelectionShape(ConduitBlockEntity conduitBlockEntity, ConduitShape mainShape) {
    SelectionShape selectionShape = new SelectionShape();
    for (ConduitShape.ExternalConnectionShape shape : mainShape.getExternalConnectionShapes()) {
      VoxelShape voxelShape = Shapes.box(
          shape.getMin().x(), shape.getMin().y(), shape.getMin().z(),
          shape.getMax().x(), shape.getMax().y(), shape.getMax().z());
      selectionShape.addShape(shape.getConnection(), voxelShape);
    }
    return selectionShape;
  }

  private static VisualShape createVisualShape(ConduitBlockEntity conduitBlockEntity, ConduitShape mainShape) {
    VisualShape visualShape = new VisualShape();
    for (var entry : mainShape.getShapes().entrySet()) {
      ConduitType conduitType = entry.getKey();
      ConduitShape.SingleConduitShape conduitShape = entry.getValue();

      // TODO: make this generic.
      Material material = ConduitModelLoader.MISSING_MATERIAL;
      if (conduitType == ConduitType.SIMPLE_ITEM) {
        material = ConduitModelLoader.SIMPLE_ITEM_CONDUIT_MATERIAL;
      } else if (conduitType == ConduitType.SIMPLE_FLUID) {
        material = ConduitModelLoader.SIMPLE_FLUID_CONDUIT_MATERIAL;
      }

      final Vec3 c = conduitShape.getCenter();
      visualShape.addBox(
          new VisualShape.Box(
              new Vector3f((float) c.x - 0.125f, (float) c.y - 0.125f, (float) c.z - 0.125f),
              new Vector3f((float) c.x + 0.125f, (float) c.y + 0.125f, (float) c.z + 0.125f),
              material));

      // TODO: only add a connection if there is a conduit of the same type in the next block over.
      for (ConduitConnection conn : conduitBlockEntity.getConnections()) {
        if (conn.getConnectionType() == ConduitConnection.ConnectionType.EXTERNAL) {
          // We handle external connections in the next loop.
          continue;
        }

        var dir = conn.getDirection();
        // We only do the visual for the position axis direction, the conduit in the negative direction will draw
        // the other connection for us.
        if (dir.getAxisDirection() != Direction.AxisDirection.POSITIVE) {
          continue;
        }

        Vector3f normal = new Vector3f(dir.getStepX(), dir.getStepY(), dir.getStepZ());
        Vector3f min = new Vector3f(
            (float) c.x + normal.x() * 0.125f - (1.0f - normal.x()) * 0.09375f,
            (float) c.y + normal.y() * 0.125f - (1.0f - normal.y()) * 0.09375f,
            (float) c.z + normal.z() * 0.125f - (1.0f - normal.z()) * 0.09375f);
        Vector3f max = new Vector3f(
            (float) c.x + normal.x() * 0.875f + (1.0f - normal.x()) * 0.09375f,
            (float) c.y + normal.y() * 0.875f + (1.0f - normal.y()) * 0.09375f,
            (float) c.z + normal.z() * 0.875f + (1.0f - normal.z()) * 0.09375f);
        visualShape.addBox(new VisualShape.Box(min, max, material));
      }

    }

    for (ConduitShape.ExternalConnectionShape externalConnectionShape : mainShape.getExternalConnectionShapes()) {
      visualShape.addBox(
          new VisualShape.Box(
              externalConnectionShape.getMin(),
              externalConnectionShape.getMax(),
              ConduitModelLoader.CONNECTOR_MATERIAL));
    }

    return visualShape;
  }

  private static ArrayList<BakedQuad> createBakedQuads(
      Function<Material, TextureAtlasSprite> spriteGetter, VisualShape visualShape) {
    ArrayList<BakedQuad> quads = new ArrayList<>();

    for (var box : visualShape.getBoxes()) {
      TextureAtlasSprite texture = spriteGetter.apply(box.getMaterial());
      quads.addAll(QuadHelper.createCube(box.getMin(), box.getMax(), Transformation.identity(), texture));
    }

    return quads;
  }
}
