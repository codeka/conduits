package com.codeka.justconduits.common.impl.item;

import com.codeka.justconduits.common.ChannelColor;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.impl.ConduitType;
import com.codeka.justconduits.common.impl.common.CommonClientStatePacket;
import com.codeka.justconduits.common.impl.common.CommonExternalConnection;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/** Client state packet for {@link ItemConduit}s. */
public class ItemConduitClientStatePacket extends CommonClientStatePacket<ItemExternalConnection> {
  private static final Logger L = LogManager.getLogger();
  private final HashMap<Direction, ItemExternalConnection> externalConnections = new HashMap<>();

  public ItemConduitClientStatePacket() {
    super(null, ConduitType.SIMPLE_ITEM);
  }

  public ItemConduitClientStatePacket(ConduitBlockEntity conduitBlockEntity, ConduitType conduitType) {
    super(conduitBlockEntity, conduitType);

    if (conduitBlockEntity != null) {
      for (ConduitConnection conn : conduitBlockEntity.getConnections()) {
        if (conn.getConnectionType() != ConduitConnection.ConnectionType.EXTERNAL) {
          continue;
        }

        ItemExternalConnection externalConnection = conn.getNetworkExternalConnection(conduitType);
        externalConnections.put(conn.getDirection(), externalConnection);
      }
    }
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    super.encode(buffer);

    buffer.writeVarInt(externalConnections.size());
    for (Map.Entry<Direction, ItemExternalConnection> entry : externalConnections.entrySet()) {
      buffer.writeEnum(entry.getKey());
      buffer.writeVarInt(entry.getValue().getExtractChannelColor().getNumber());
      buffer.writeVarInt(entry.getValue().getInsertChannelColor().getNumber());
    }
  }

  @Override
  public void decode(FriendlyByteBuf buffer) {
    super.decode(buffer);

    int numConnections = buffer.readVarInt();
    for (int i = 0; i < numConnections; i++) {
      Direction dir = buffer.readEnum(Direction.class);
      int extractChannelColor = buffer.readVarInt();
      int insertChannelColor = buffer.readVarInt();

      ItemExternalConnection externalConnection = externalConnections.get(dir);
      if (externalConnection == null) {
        // If we don't have the connection yet, add it now.
        externalConnection = new ItemExternalConnection();
        externalConnections.put(dir, externalConnection);
      }
      externalConnection.setExtractChannelColor(ChannelColor.fromNumber(extractChannelColor));
      externalConnection.setInsertChannelColor(ChannelColor.fromNumber(insertChannelColor));
    }
  }
}
