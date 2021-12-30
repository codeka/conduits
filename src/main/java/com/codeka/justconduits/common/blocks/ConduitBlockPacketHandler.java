package com.codeka.justconduits.common.blocks;

import com.codeka.justconduits.packets.ConduitClientStatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ConduitBlockPacketHandler {
  public static void handle(ConduitClientStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
    ClientLevel level = Minecraft.getInstance().level;
    if (level != null) {
      if (level.getBlockEntity(msg.getBlockPos()) instanceof ConduitBlockEntity conduitBlockEntity) {
        conduitBlockEntity.onClientUpdate(msg);
      }
    }
  }
}
