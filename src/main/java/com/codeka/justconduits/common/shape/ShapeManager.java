package com.codeka.justconduits.common.shape;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.impl.ConduitType;
import com.codeka.justconduits.helpers.QuadHelper;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
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
    return VisualShapeBuilder.createBakedQuads(spriteGetter, visualShape);
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
   * Gets the center offset for the given {@link ConduitType} in the given {@link ConduitBlockEntity}.
   *
   * @return null if the conduit block entity doesn't have the given conduit type.
   */
  @Nullable
  public Vec3 getCenterOffset(ConduitBlockEntity conduitBlockEntity, ConduitType conduitType) {
    // TODO: this is quite inefficient.
    HashSet<Direction> directions = new HashSet<>();
    for (ConduitConnection conn : conduitBlockEntity.getConnections()) {
      directions.add(conn.getDirection());
    }

    ArrayList<ConduitType> conduitTypes = new ArrayList<>(conduitBlockEntity.getConduitTypes());
    for (int i = 0; i < conduitTypes.size(); i++) {
      if (conduitTypes.get(i) == conduitType) {
        return CenterOffsets.getCenterOffset(i, conduitTypes.size(), directions);
      }
    }

    return null;
  }

  /**
   * Called whenever the shape of the {@link ConduitBlockEntity} needs to be updated (e.g. when a connection changes,
   */
  private void updateShapes() {
    ConduitShape mainShape = ShapeBuilder.generateMainShape(conduitBlockEntity);
    collisionShape = cache.getCollisionShape(conduitBlockEntity, () -> ShapeBuilder.createCollisionShape(mainShape));
    visualShape = cache.getVisualShape(conduitBlockEntity, () -> VisualShapeBuilder.createVisualShape(mainShape));
    selectionShape =
        cache.getSelectionShape(conduitBlockEntity, () -> ShapeBuilder.createSelectionShape(mainShape));
    dirty = false;
  }
}
