package com.codeka.justconduits.common.impl.common;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.impl.ConduitType;
import com.codeka.justconduits.common.impl.ConnectionMode;
import com.codeka.justconduits.packets.IConduitTypeClientStatePacket;
import com.google.common.base.MoreObjects;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class CommonClientStatePacket<T extends CommonExternalConnection> implements IConduitTypeClientStatePacket {
  private final HashMap<Direction, T> externalConnections = new HashMap<>();
  private final ConduitType conduitType;

  public CommonClientStatePacket(@Nullable ConduitBlockEntity conduitBlockEntity, ConduitType conduitType) {
    this.conduitType = conduitType;

    if (conduitBlockEntity != null) {
      for (ConduitConnection conn : conduitBlockEntity.getConnections()) {
        if (conn.getConnectionType() != ConduitConnection.ConnectionType.EXTERNAL) {
          continue;
        }

        T externalConnection = conn.getNetworkExternalConnection(conduitType);
        externalConnections.put(conn.getDirection(), externalConnection);
      }
    }
  }

  public Map<Direction, T> getExternalConnections() {
    return externalConnections;
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(externalConnections.size());
    for (Map.Entry<Direction, T> entry : externalConnections.entrySet()) {
      buffer.writeEnum(entry.getKey());
      buffer.writeEnum(entry.getValue().getExtractMode());
      buffer.writeEnum(entry.getValue().getInsertMode());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void decode(FriendlyByteBuf buffer) {
    int n = buffer.readVarInt();
    for (int i = 0; i < n; i++) {
      Direction dir = buffer.readEnum(Direction.class);

      T externalConnection = (T) conduitType.newNetworkExternalConnection();
      externalConnection.setExtractMode(buffer.readEnum(ConnectionMode.class));
      externalConnection.setInsertMode(buffer.readEnum(ConnectionMode.class));

      externalConnections.put(dir, externalConnection);
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("externalConnections", externalConnections)
        .add("conduitType", conduitType)
        .toString();
  }
}
