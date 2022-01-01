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
          shapeCenter.x() - (0.05f + Math.abs(normal.x()) * 0.2f),
          shapeCenter.y() - (0.05f + Math.abs(normal.y()) * 0.2f),
          shapeCenter.z() - (0.05f + Math.abs(normal.z()) * 0.2f),
          shapeCenter.x() + (0.05f + Math.abs(normal.x()) * 0.2f),
          shapeCenter.y() + (0.05f + Math.abs(normal.y()) * 0.2f),
          shapeCenter.z() + (0.05f + Math.abs(normal.z()) * 0.2f));
      case EXTERNAL -> Shapes.box(
          shapeCenter.x() - (0.1f + Math.abs(normal.x()) * 0.15f),
          shapeCenter.y() - (0.1f + Math.abs(normal.y()) * 0.15f),
          shapeCenter.z() - (0.1f + Math.abs(normal.z()) * 0.15f),
          shapeCenter.x() + (0.1f + Math.abs(normal.x()) * 0.15f),
          shapeCenter.y() + (0.1f + Math.abs(normal.y()) * 0.15f),
          shapeCenter.z() + (0.1f + Math.abs(normal.z()) * 0.15f));
      default -> Shapes.empty();
    };
  }
}
