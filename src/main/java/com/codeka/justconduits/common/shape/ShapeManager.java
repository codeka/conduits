package com.codeka.justconduits.common.shape;

import com.codeka.justconduits.client.blocks.ConduitModelLoader;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.capabilities.network.ConduitType;
import com.codeka.justconduits.helpers.QuadHelper;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
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
  private ArrayList<BakedQuad> bakedQuads;
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
    return createBakedQuads(spriteGetter);
  }

  /** Mark ourselves dirty. We'll update the shape the next time they're needed. */
  public void markDirty() {
    dirty = true;
  }

  /**
   * Called whenever the shape of the {@link ConduitBlockEntity} needs to be updated (e.g. when a connection changes,
   */
  private void updateShapes() {
    ConduitShape mainShape = generateMainShape();
    collisionShape = cache.getCollisionShape(conduitBlockEntity, () -> createCollisionShape(mainShape));
    visualShape = cache.getVisualShape(conduitBlockEntity, () -> createVisualShape(mainShape));
    dirty = false;
  }

  /**
   * Generates the "main" shape of the conduit. The main shape is the center of each of the nobbly bits and a list of
   * directions that the "pipes" come out of.
   */
  private ConduitShape generateMainShape() {
    ConduitShape mainShape = new ConduitShape();
    ArrayList<ConduitType> conduitTypes = new ArrayList<>(conduitBlockEntity.getConduitTypes());
    if (conduitTypes.size() == 1) {
      var shape = mainShape.addConduit(conduitTypes.get(0), new Vec3(0.5f, 0.5f, 0.5f));
      populateSingleShape(shape, conduitTypes.get(0));
    } else if (conduitTypes.size() > CONDUIT_CENTER_OFFSETS.length) {
      L.atInfo().log("Too many conduits, we can't draw it");
      return mainShape;
    } else {
      for (int i = 0; i < conduitTypes.size(); i++) {
        var shape = mainShape.addConduit(conduitTypes.get(i), CONDUIT_CENTER_OFFSETS[i]);
        populateSingleShape(shape, conduitTypes.get(i));
      }
    }

    return mainShape;
  }

  private void populateSingleShape(ConduitShape.SingleConduitShape shape, ConduitType conduitType) {
    for (ConduitConnection conn : conduitBlockEntity.getConnections()) {
      // TODO: check that there's actually another conduit of this type at the next block over.
      shape.addDirection(conn.getDirection());
    }
  }

  private VoxelShape createCollisionShape(ConduitShape mainShape) {
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

    return collisionShape.optimize();
  }

  private VisualShape createVisualShape(ConduitShape mainShape) {
    return new VisualShape();
  }

  private ArrayList<BakedQuad> createBakedQuads(Function<Material, TextureAtlasSprite> spriteGetter) {
    ArrayList<ConduitType> conduitTypes = new ArrayList<>(conduitBlockEntity.getConduitTypes());

    ArrayList<BakedQuad> quads = new ArrayList<>();
    for (int i = 0; i < conduitTypes.size(); i++) {
      ConduitType conduitType = conduitTypes.get(i);
      Transformation conduitTransform = getTransformationForConduit(i, conduitTypes.size());

      TextureAtlasSprite texture;
      if (conduitType == ConduitType.SIMPLE_ITEM) {
        texture = spriteGetter.apply(ConduitModelLoader.SIMPLE_ITEM_CONDUIT_MATERIAL);
      } else if (conduitType == ConduitType.SIMPLE_FLUID) {
        texture = spriteGetter.apply(ConduitModelLoader.SIMPLE_FLUID_CONDUIT_MATERIAL);
      } else {
        // Invalid conduit type (or at least, not yet supported)
        texture = spriteGetter.apply(ConduitModelLoader.MISSING_MATERIAL);
      }

      Transformation transformation = new Transformation(Matrix4f.createScaleMatrix(0.25f, 0.25f, 0.25f));
      quads.addAll(QuadHelper.createCube(conduitTransform.compose(transformation), texture));

      for (ConduitConnection conn : conduitBlockEntity.getConnections()) {
        quads.addAll(QuadHelper.generateQuads(conn.getVoxelShape(), conduitTransform, texture));
      }
    }

    return quads;
  }

  private Transformation getTransformationForConduit(int index, int totalConduits) {
    if (totalConduits <= 1) {
      return Transformation.identity();
    }

    if (index == 0) {
      return new Transformation(Matrix4f.createTranslateMatrix(0.25f, 0.0f, 0.0f));
    } else if (index == 1) {
      return new Transformation(Matrix4f.createTranslateMatrix(-0.25f, 0.0f, 0.0f));
    } else {
      // TODO: others
      return Transformation.identity();
    }
  }
}
