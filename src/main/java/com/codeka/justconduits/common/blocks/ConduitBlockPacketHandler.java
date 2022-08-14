package com.codeka.justconduits.common.blocks;

import com.codeka.justconduits.packets.ConduitClientStatePacket;
import com.codeka.justconduits.packets.ConduitUpdatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class ConduitBlockPacketHandler {
  private static final Logger L = LogManager.getLogger();

  public static void handle(ConduitClientStatePacket packet, Supplier<NetworkEvent.Context> ctx) {
    ClientLevel level = Minecraft.getInstance().level;
    if (level != null) {
      if (level.getBlockEntity(packet.getBlockPos()) instanceof ConduitBlockEntity conduitBlockEntity) {
        conduitBlockEntity.onClientUpdate(packet);
      }
    }
  }

  public static void handle(ConduitUpdatePacket packet, Supplier<NetworkEvent.Context> ctx) {
    ServerPlayer serverPlayer = ctx.get().getSender();
    if (serverPlayer == null) {
      L.warn("Cannot handle ConduitUpdatePacket because Sender is null.");
      return;
    }

    if (serverPlayer.level.getBlockEntity(packet.getBlockPos()) instanceof ConduitBlockEntity conduitBlockEntity) {
      conduitBlockEntity.onServerUpdate(packet);
    } else {
      L.warn("Cannot handle ConduitUpdatePacket because block is not a ConduitBlockEntity");
    }
  }
}
