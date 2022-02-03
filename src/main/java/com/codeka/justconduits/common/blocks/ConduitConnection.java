package com.codeka.justconduits.common.blocks;

import com.codeka.justconduits.common.impl.ConduitType;
import com.codeka.justconduits.common.impl.NetworkExternalConnection;
import com.codeka.justconduits.common.impl.NetworkType;
import com.codeka.justconduits.packets.ConduitClientStatePacket;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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

  /**
   * A set of all the conduit types this connection has. This will be a subset of the conduit types in the block: being
   * only the conduits that are common between this block and the one we're connected to.
   */
  private final HashSet<ConduitType> conduitTypes = new HashSet<>();

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

  /** Gets the {@link BlockPos} we are actually connected to. */
  public BlockPos getConnectedBlockPos() {
    return blockPos.relative(dir);
  }

  /**
   * Helper method to get the {@link BlockEntity} that we are actually connected to. Could be null if the block entity
   * was just destroyed and we haven't updated the network yet.
   */
  @Nullable
  public BlockEntity getConnectedBlockEntity(Level level) {
    return level.getBlockEntity(getConnectedBlockPos());
  }

  /** Gets the name of the block that this connection is connected to. */
  public Component getConnectionName(Level level) {
    if (getConnectedBlockEntity(level) instanceof Nameable nameable) {
      return nameable.hasCustomName() ? nameable.getCustomName() : nameable.getDisplayName();
    }

    BlockState blockState = level.getBlockState(getBlockPos().relative(dir));
    return blockState.getBlock().getName();
  }

  /**
   * Call this when we expect the conduit types might've updated. We'll update our cache.
   */
  public void updateConduitTypes(Level level) {
    switch (connectionType) {
      case CONDUIT -> {
        if (level.getBlockEntity(blockPos) instanceof ConduitBlockEntity conduitBlockEntity &&
            getConnectedBlockEntity(level) instanceof ConduitBlockEntity neighbor) {
          conduitTypes.clear();
          conduitTypes.addAll(Sets.intersection(
              new HashSet<>(conduitBlockEntity.getConduitTypes()),
              new HashSet<>(neighbor.getConduitTypes())));
        } else {
          L.atError().log("Expected neighbor to be a conduit.");
        }
      }
      case EXTERNAL -> {
        if (level.getBlockEntity(blockPos) instanceof ConduitBlockEntity conduitBlockEntity) {
          BlockEntity blockEntity = getConnectedBlockEntity(level);
          conduitTypes.clear();
          for (ConduitType conduitType : conduitBlockEntity.getConduitTypes()) {
            if (conduitType.getConduitImpl().canConnect(blockEntity, blockEntity.getBlockPos(), dir.getOpposite())) {
              conduitTypes.add(conduitType);
            }
          }
        } else {
          L.atError().log("Block is not a Conduit?");
        }
      }
    }
  }

  /**
   * When removing a conduit, we just remove that one conduit type from the connections and the neighbor pointing back
   * to us. Afterwards, we can then update the network knowing that the conduit is removed at least from this block.
   */
  public void removeConduitType(@Nonnull Level level, @Nonnull ConduitType conduitType) {
    conduitTypes.remove(conduitType);
    if (getConnectedBlockEntity(level) instanceof ConduitBlockEntity neighbor) {
      ConduitConnection reverseConnection = neighbor.getConnection(dir.getOpposite());
      if (reverseConnection != null) {
        reverseConnection.conduitTypes.remove(conduitType);
      }
    }
  }

  /**
   * Gets the set of {@link ConduitType}s in this connection.
   *
   * When {@link #getConnectionType()} is {@link ConnectionType#CONDUIT}, this is the connections in both this block
   * and the block we are connected to. When it's {@link ConnectionType#EXTERNAL}, it is the connections that can
   * actually connect to the external block.
   */
  public Set<ConduitType> getConduitTypes() {
    return conduitTypes;
  }

  public void load(CompoundTag tag) {
    for (String s : tag.getString("ConduitTypes").split(" ")) {
      if (!s.isEmpty()) {
        conduitTypes.add(ConduitType.fromName(s));
      }
    }
  }

  public void save(CompoundTag tag) {
    StringBuilder sb = new StringBuilder();
    for (ConduitType conduitType : conduitTypes) {
      sb.append(" ");
      sb.append(conduitType.getName());
    }
    tag.putString("ConduitTypes", sb.toString().trim());
  }

  /**
   * Called on the client when we get an update from the server.
   *
   * @param packet The packet we received from the server with the updates.
   * @return True if something actually changed, false if we're unchanged based on this new state.
   */
  public boolean updateFrom(ConduitClientStatePacket.ConnectionPacket packet) {
    boolean updated = false;
    if (packet.getConnectionType() != connectionType) {
      connectionType = packet.getConnectionType();
      updated = true;
    }
    if (!packet.getConduitTypes().equals(conduitTypes)) {
      conduitTypes.clear();
      conduitTypes.addAll(packet.getConduitTypes());
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
