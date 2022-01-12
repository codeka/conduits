package com.codeka.justconduits.common.blocks;

import com.codeka.justconduits.common.capabilities.network.ConduitType;
import com.codeka.justconduits.common.capabilities.network.NetworkExternalConnection;
import com.codeka.justconduits.common.capabilities.network.NetworkType;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
  public <T extends NetworkExternalConnection> T getNetworkExternalConnection(ConduitType conduitType) {
    checkState(connectionType == ConnectionType.EXTERNAL);

    // This should be non-null when connection type is EXTERNAL
    checkNotNull(conduitConnections);

    NetworkExternalConnection networkExternalConnection = conduitConnections.get(conduitType.getNetworkType());
    if (networkExternalConnection == null) {
      networkExternalConnection = conduitType.newNetworkExternalConnection();
      conduitConnections.put(conduitType.getNetworkType(), networkExternalConnection);
    }

    return (T) networkExternalConnection;
  }

  /** Gets the collection of {@link NetworkType} that we have connected. */
  public Collection<NetworkType> getConnectedNetworks() {
    if (conduitConnections == null) {
      return Collections.emptyList();
    }
    return conduitConnections.keySet();
  }

  public ConnectionType getConnectionType() {
    return connectionType;
  }
}
