package com.codeka.justconduits.common.capabilities.network.item;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.capabilities.network.ConduitType;
import com.codeka.justconduits.common.capabilities.network.NetworkType;
import com.codeka.justconduits.packets.IConduitTypeClientStatePacket;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;

/** Client state packet for {@link ItemConduit}s. */
public class ItemConduitClientStatePacket implements IConduitTypeClientStatePacket {
  private final HashMap<Direction, ItemExternalConnection> externalConnections = new HashMap<>();

  public ItemConduitClientStatePacket() {
  }

  public ItemConduitClientStatePacket(ConduitBlockEntity conduitBlockEntity) {
    for (ConduitConnection conn : conduitBlockEntity.getConnections()) {
      if (conn.getConnectionType() != ConduitConnection.ConnectionType.EXTERNAL) {
        continue;
      }

      ItemExternalConnection externalConnection =
          conn.getNetworkExternalConnection(NetworkType.ITEM, ConduitType.SIMPLE_ITEM);
      externalConnections.put(conn.getDirection(), externalConnection);
    }
  }

  public Map<Direction, ItemExternalConnection> getExternalConnections() {
    return externalConnections;
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(externalConnections.size());
    for (Map.Entry<Direction, ItemExternalConnection> entry : externalConnections.entrySet()) {
      buffer.writeEnum(entry.getKey());
      buffer.writeBoolean(entry.getValue().isExtractEnabled());
      buffer.writeBoolean(entry.getValue().isInsertEnabled());
    }
  }

  @Override
  public void decode(FriendlyByteBuf buffer) {
    int n = buffer.readVarInt();
    for (int i = 0; i < n; i++) {
      Direction dir = buffer.readEnum(Direction.class);
      ItemExternalConnection externalConnection = new ItemExternalConnection();
      externalConnection.setExtractEnabled(buffer.readBoolean());
      externalConnection.setInsertEnabled(buffer.readBoolean());

      externalConnections.put(dir, externalConnection);
    }
  }
}
