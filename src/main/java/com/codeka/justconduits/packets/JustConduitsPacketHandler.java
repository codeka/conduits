package com.codeka.justconduits.packets;

import com.codeka.justconduits.JustConduitsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JustConduitsPacketHandler {
  private static final Logger L = LogManager.getLogger();

  private static final String PROTOCOL_VERSION = "1";

  public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
      new ResourceLocation(JustConduitsMod.MODID, "net"),
      () -> PROTOCOL_VERSION,
      PROTOCOL_VERSION::equals,
      PROTOCOL_VERSION::equals);

  public static void init() {
    int id = 1;
    CHANNEL.registerMessage(
        id++,
        ConduitClientStatePacket.class,
        ConduitClientStatePacket::encode,
        ConduitClientStatePacket::new,
        ConduitClientStatePacket::handle);
  }
}
