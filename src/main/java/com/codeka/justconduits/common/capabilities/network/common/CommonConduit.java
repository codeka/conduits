package com.codeka.justconduits.common.capabilities.network.common;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.capabilities.network.AbstractConduit;
import com.codeka.justconduits.common.capabilities.network.ConduitHolder;
import com.codeka.justconduits.common.capabilities.network.ConnectionMode;
import com.codeka.justconduits.packets.ConduitUpdatePacket;
import com.codeka.justconduits.packets.IConduitTypeClientStatePacket;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Map;

public abstract class CommonConduit extends AbstractConduit {
  private static final Logger L = LogManager.getLogger();

  @Override
  public void onClientUpdate(
      @Nonnull IConduitTypeClientStatePacket basePacket, @Nonnull ConduitBlockEntity conduitBlockEntity,
      @Nonnull ConduitHolder conduitHolder) {
    CommonClientStatePacket packet = (CommonClientStatePacket) basePacket;
    for (Map.Entry<Direction, CommonExternalConnection> entry : packet.getExternalConnections().entrySet()) {
      Direction dir = entry.getKey();

      ConduitConnection conn = conduitBlockEntity.getConnection(dir);
      if (conn == null) {
        // TODO error
        continue;
      }
      CommonExternalConnection existing = conn.getNetworkExternalConnection(conduitHolder.getConduitType());
      existing.setExtractMode(entry.getValue().getExtractMode());
      existing.setInsertMode(entry.getValue().getInsertMode());
    }
  }

  @Override
  public void onServerUpdate(
      @Nonnull ConduitUpdatePacket packet, @Nonnull ConduitBlockEntity conduitBlockEntity,
      @Nonnull ConduitHolder conduitHolder) {
    ConduitConnection connection = conduitBlockEntity.getConnection(packet.getDirection());
    if (connection == null) {
      L.atError().log("No connection found when updating from client.");
      return;
    }

    CommonExternalConnection conn = connection.getNetworkExternalConnection(conduitHolder.getConduitType());
    switch (packet.getUpdateType()) {
      case INSERT_MODE -> conn.setInsertMode(ConnectionMode.values()[packet.getIntValue()]);
      case EXTRACT_MODE -> conn.setExtractMode(ConnectionMode.values()[packet.getIntValue()]);
      default -> L.atError().log("Unexpected update type: {}", packet.getUpdateType());
    }
  }

  @Override
  public void saveAdditional(
      @Nonnull CompoundTag tag, @Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitHolder conduitHolder) {
    CompoundTag connectionsTag = new CompoundTag();
    for (ConduitConnection conduitConnection : conduitBlockEntity.getConnections()) {
      if (conduitConnection.getConnectionType() != ConduitConnection.ConnectionType.EXTERNAL) {
        continue;
      }

      CompoundTag connectionTag = new CompoundTag();
      CommonExternalConnection conn = conduitConnection.getNetworkExternalConnection(conduitHolder.getConduitType());
      connectionTag.putString("ExtractMode", conn.getExtractMode().name());
      connectionTag.putString("InsertMode", conn.getInsertMode().name());
      connectionsTag.put(conduitConnection.getDirection().getName(), connectionTag);
    }

    tag.put("Connections", connectionsTag);
  }

  @Override
  public void loadAdditional(
      @Nonnull CompoundTag tag, @Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitHolder conduitHolder) {
    CompoundTag connectionsTag = tag.getCompound("Connections");
    for (String dirName : connectionsTag.getAllKeys()) {
      Direction dir = Direction.byName(dirName);
      if (dir == null) {
        L.atWarn().log("Unknown direction: {}", dirName);
        continue;
      }

      ConduitConnection conduitConnection = conduitBlockEntity.getConnection(dir);
      if (conduitConnection == null) {
        continue;
      }

      CompoundTag connectionTag = connectionsTag.getCompound(dirName);
      CommonExternalConnection conn = conduitConnection.getNetworkExternalConnection(conduitHolder.getConduitType());
      conn.setExtractMode(ConnectionMode.valueOf(connectionTag.getString("ExtractMode")));
      conn.setInsertMode(ConnectionMode.valueOf(connectionTag.getString("InsertMode")));
    }
  }
}
