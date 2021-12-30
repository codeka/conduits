package com.codeka.justconduits.packets;

import com.codeka.justconduits.client.blocks.Connection;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitBlockPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

/**
 * This packet is sent to the client when the state of a conduit changes on the server. We only send the state that is
 * necessary for actually rendering the conduit.
 */
// TODO: can we make this more generic or something?
public class ConduitClientStatePacket {
  private final BlockPos blockPos;
  private final ArrayList<Connection> connections;

  public ConduitClientStatePacket(FriendlyByteBuf buffer) {
    blockPos = buffer.readBlockPos();
    connections = buffer.readCollection(ArrayList::new, (buf) -> {
      return new Connection(buf.readEnum(Direction.class));
    });
  }

  public ConduitClientStatePacket(ConduitBlockEntity conduitBlockEntity) {
    blockPos = conduitBlockEntity.getBlockPos();
    this.connections = new ArrayList<>(conduitBlockEntity.getConnections());
  }

  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBlockPos(blockPos);
    buffer.writeCollection(connections, (buf, conn) -> {
      buf.writeEnum(conn.getDirection());
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

  public HashMap<Direction, Connection> getConnections() {
    HashMap<Direction, Connection> conns = new HashMap<>();
    for (Connection conn : connections) {
      conns.put(conn.getDirection(), conn);
    }
    return conns;
  }
}
