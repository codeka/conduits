package com.codeka.justconduits.packets;

import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitBlockPacketHandler;
import com.codeka.justconduits.common.capabilities.network.ConduitType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This packet is sent to the client when the state of a conduit changes on the server. We only send the state that is
 * necessary for actually rendering the conduit.
 */
// TODO: can we make this more generic or something?
public class ConduitClientStatePacket {
  private final BlockPos blockPos;
  private final ArrayList<ConduitConnection> connections;
  private final HashMap<ConduitType, IConduitTypeClientStatePacket> conduitTypes;

  public ConduitClientStatePacket(ConduitBlockEntity conduitBlockEntity) {
    blockPos = conduitBlockEntity.getBlockPos();
    connections = new ArrayList<>(conduitBlockEntity.getConnections());
    conduitTypes = new HashMap<>();
    for (ConduitType conduitType : conduitBlockEntity.getConduitTypes()) {
      conduitTypes.put(conduitType, conduitType.getConduitImpl().createClientState(conduitBlockEntity));
    }
  }

  public ConduitClientStatePacket(FriendlyByteBuf buffer) {
    blockPos = buffer.readBlockPos();
    ArrayList<HashMap.Entry<ConduitType, IConduitTypeClientStatePacket>> entries =
        buffer.readCollection(ArrayList::new, (buf) -> {
          int len = buf.readVarInt();
          String name = buf.readCharSequence(len, StandardCharsets.UTF_8).toString();

          ConduitType conduitType = checkNotNull(ConduitType.fromName(name));
          IConduitTypeClientStatePacket packet = conduitType.newConduitTypeClientStatePacket();
          packet.decode(buf);

          return new AbstractMap.SimpleEntry<>(conduitType, packet);
        });
    conduitTypes = new HashMap<>();
    for (var entry : entries) {
      conduitTypes.put(entry.getKey(), entry.getValue());
    }
    connections = buffer.readCollection(ArrayList::new, (buf) -> {
      Direction dir = buf.readEnum(Direction.class);
      ConduitConnection.ConnectionType connectionType = buf.readEnum(ConduitConnection.ConnectionType.class);

      return new ConduitConnection(blockPos, dir, connectionType);
    });
  }

  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBlockPos(blockPos);
    buffer.writeCollection(conduitTypes.entrySet(), (buf, entry) -> {
      ConduitType conduitType = entry.getKey();
      IConduitTypeClientStatePacket packet = entry.getValue();

      buf.writeVarInt(conduitType.getName().length());
      buf.writeCharSequence(conduitType.getName(), StandardCharsets.UTF_8);
      packet.encode(buffer);
    });
    buffer.writeCollection(connections, (buf, conn) -> {
      buf.writeEnum(conn.getDirection());
      buf.writeEnum(conn.getConnectionType());
    });
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ConduitBlockPacketHandler.handle(this, ctx));
    });
    ctx.get().setPacketHandled(true);
  }

  public BlockPos getBlockPos() {
    return blockPos;
  }

  public Map<ConduitType, IConduitTypeClientStatePacket> getConduits() {
    return conduitTypes;
  }

  public HashMap<Direction, ConduitConnection> getConnections() {
    HashMap<Direction, ConduitConnection> conns = new HashMap<>();
    for (ConduitConnection conn : connections) {
      conns.put(conn.getDirection(), conn);
    }
    return conns;
  }
}
