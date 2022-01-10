package com.codeka.justconduits.common.shape;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The {@link ShapeBuilder} is responsible for building the different shapes of the blocks.
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
 */
public class ShapeBuilder {
  private final ConduitBlockEntity conduitBlockEntity;

  private VoxelShape collisionShape;
  private boolean dirty;

  public ShapeBuilder(ConduitBlockEntity conduitBlockEntity) {
    dirty = true;
    this.conduitBlockEntity = conduitBlockEntity;
  }

  public VoxelShape getCollisionShape() {
    if (dirty ) {
      updateShapes();
    }
    return collisionShape;
  }

  /** Mark ourselves dirty. We'll update the shape the next time they're needed. */
  public void markDirty() {
    dirty = true;
  }

  /**
   * Called whenever the shape of the {@link ConduitBlockEntity} needs to be updated (e.g. when a connection changes,
   */
  private void updateShapes() {
    // TODO: start with the middle bit.
    VoxelShape shape = Shapes.box(0.375f, 0.375f, 0.375f, 0.625f, 0.625f, 0.625f);
    for (ConduitConnection conn : conduitBlockEntity.getConnections()) {
      shape = Shapes.or(shape, conn.getVoxelShape());
    }

    this.collisionShape = shape;
  }
}
