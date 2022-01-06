package com.codeka.justconduits.common.blocks;

import com.codeka.justconduits.common.capabilities.network.ConduitType;
import com.codeka.justconduits.common.capabilities.network.NetworkExternalConnection;
import com.codeka.justconduits.common.capabilities.network.NetworkType;
import com.google.common.base.Preconditions;
import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.HashMap;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

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

  private final BlockPos blockPos;
  private final Direction dir;
  private ConnectionType connectionType;

  /** This will be null for any connection type that's not EXTERNAL. */
  @Nullable
  private HashMap<NetworkType, NetworkExternalConnection> conduitConnections;

  public ConduitConnection(@Nonnull BlockPos blockPos, @Nonnull Direction dir, @Nonnull ConnectionType connectionType) {
    this.blockPos = checkNotNull(blockPos);
    this.dir = checkNotNull(dir);
    this.connectionType = checkNotNull(connectionType);

    if (connectionType == ConnectionType.EXTERNAL) {
      conduitConnections = new HashMap<>();
    }
  }

  /** Gets the {@link BlockPos} of the {@link ConduitBlockEntity} this connection belongs to. */
  public BlockPos getBlockPos() {
    return blockPos;
  }

  public Direction getDirection() {
    return dir;
  }

  /**
   * Helper method to get the {@link BlockEntity} that we are actually connected to.
   */
  public BlockEntity getConnectedBlockEntity(Level level) {
    return level.getBlockEntity(blockPos.relative(dir));
  }

  public boolean updateFrom(ConduitConnection other) {
    boolean updated = false;
    if (other.connectionType != connectionType) {
      connectionType = other.connectionType;
      updated = true;
    }

    return updated;
  }

  /**
   * Gets the {@link NetworkExternalConnection} subclass for the given network connection. Created a new one if
   * one does not exist yet.
   */
  public <T extends NetworkExternalConnection> T getNetworkExternalConnection(
      NetworkType networkType, ConduitType conduitType) {
    checkArgument(conduitType.getNetworkType() == networkType);
    checkState(connectionType == ConnectionType.EXTERNAL);

    // This should be non-null when connection type is EXTERNAL
    checkNotNull(conduitConnections);

    NetworkExternalConnection networkExternalConnection = conduitConnections.get(networkType);
    if (networkExternalConnection == null) {
      networkExternalConnection = conduitType.newNetworkExternalConnection();
      conduitConnections.put(networkType, networkExternalConnection);
    }

    return (T) networkExternalConnection;
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
