package com.codeka.justconduits.common.blocks;

import com.mojang.math.Vector3f;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Keeps track of the information that connects one {@link ConduitBlockEntity} to another.
 */
public class ConduitConnection {
  private static final Logger L = LogManager.getLogger();

  public enum ConnectionType {
    /** An unknown type of connection. Shouldn't be used. */
    UNKNOWN,

    /** The connection is to another conduit. */
    CONDUIT,

    /** The connection is to an external block. */
    EXTERNAL
  }

  private final Direction dir;
  private final ConnectionType connectionType;

  public ConduitConnection(@Nonnull Direction dir, @Nonnull ConnectionType connectionType) {
    this.dir = checkNotNull(dir);
    this.connectionType = checkNotNull(connectionType);
  }

  public Direction getDirection() {
    return dir;
  }

  public ConnectionType getConnectionType() {
    return connectionType;
  }

  /** Calculates the {@link VoxelShape} for this connection. */
  public VoxelShape getVoxelShape() {
    Vector3f normal = new Vector3f(dir.getStepX(), dir.getStepY(), dir.getStepZ());
    Vector3f shapeCenter =
        new Vector3f(0.5f + normal.x() * 0.25f, 0.5f + normal.y() * 0.25f, 0.5f + normal.z() * 0.25f);

    // TODO: this is the shape of the conduit connection only
    return switch (connectionType) {
      case CONDUIT -> Shapes.box(
          shapeCenter.x() - (f(1.5f) + Math.abs(normal.x()) * (0.25f - f(1.5f))),
          shapeCenter.y() - (f(1.5f) + Math.abs(normal.y()) * (0.25f - f(1.5f))),
          shapeCenter.z() - (f(1.5f) + Math.abs(normal.z()) * (0.25f - f(1.5f))),
          shapeCenter.x() + (f(1.5f) + Math.abs(normal.x()) * (0.25f - f(1.5f))),
          shapeCenter.y() + (f(1.5f) + Math.abs(normal.y()) * (0.25f - f(1.5f))),
          shapeCenter.z() + (f(1.5f) + Math.abs(normal.z()) * (0.25f - f(1.5f))));
      case EXTERNAL -> Shapes.or(
              Shapes.box(
                  shapeCenter.x() - (f(1.5f) + Math.abs(normal.x()) * (0.25f - f(1.5f))),
                  shapeCenter.y() - (f(1.5f) + Math.abs(normal.y()) * (0.25f - f(1.5f))),
                  shapeCenter.z() - (f(1.5f) + Math.abs(normal.z()) * (0.25f - f(1.5f))),
                  shapeCenter.x() + (f(1.5f) + Math.abs(normal.x()) * (0.25f - f(1.5f))),
                  shapeCenter.y() + (f(1.5f) + Math.abs(normal.y()) * (0.25f - f(1.5f))),
                  shapeCenter.z() + (f(1.5f) + Math.abs(normal.z()) * (0.25f - f(1.5f)))),
              Shapes.box(
                  shapeCenter.x() + normal.x() * 0.125f - (f(6) + Math.abs(normal.x()) * (0.125f - f(6))),
                  shapeCenter.y() + normal.y() * 0.125f - (f(6) + Math.abs(normal.y()) * (0.125f - f(6))),
                  shapeCenter.z() + normal.z() * 0.125f - (f(6) + Math.abs(normal.z()) * (0.125f - f(6))),
                  shapeCenter.x() + normal.x() * 0.125f + (f(6) + Math.abs(normal.x()) * (0.125f - f(6))),
                  shapeCenter.y() + normal.y() * 0.125f + (f(6) + Math.abs(normal.y()) * (0.125f - f(6))),
                  shapeCenter.z() + normal.z() * 0.125f + (f(6) + Math.abs(normal.z()) * (0.125f - f(6))))
          );
      default -> Shapes.empty();
    };
  }

  private static float f(float f) {
    return f / 16.0f;
  }
}
