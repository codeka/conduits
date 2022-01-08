package com.codeka.justconduits.common.capabilities.network.fluid;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.capabilities.network.ConduitType;
import com.codeka.justconduits.common.capabilities.network.NetworkType;
import com.codeka.justconduits.common.capabilities.network.item.ItemExternalConnection;
import com.codeka.justconduits.packets.IConduitTypeClientStatePacket;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;

/** Client state packet for {@link FluidConduit}s. */
public class FluidConduitClientStatePacket implements IConduitTypeClientStatePacket {
  private final HashMap<Direction, FluidExternalConnection> externalConnections = new HashMap<>();

  public FluidConduitClientStatePacket() {
  }

  public FluidConduitClientStatePacket(ConduitBlockEntity conduitBlockEntity) {
    for (ConduitConnection conn : conduitBlockEntity.getConnections()) {
      if (conn.getConnectionType() != ConduitConnection.ConnectionType.EXTERNAL) {
        continue;
      }

      FluidExternalConnection externalConnection = conn.getNetworkExternalConnection(ConduitType.SIMPLE_FLUID);
      externalConnections.put(conn.getDirection(), externalConnection);
    }
  }

  public Map<Direction, FluidExternalConnection> getExternalConnections() {
    return externalConnections;
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(externalConnections.size());
    for (Map.Entry<Direction, FluidExternalConnection> entry : externalConnections.entrySet()) {
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
      FluidExternalConnection externalConnection = new FluidExternalConnection();
      externalConnection.setExtractEnabled(buffer.readBoolean());
      externalConnection.setInsertEnabled(buffer.readBoolean());

      externalConnections.put(dir, externalConnection);
    }
  }
}
