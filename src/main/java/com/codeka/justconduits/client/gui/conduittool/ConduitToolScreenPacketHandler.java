package com.codeka.justconduits.client.gui.conduittool;

import com.codeka.justconduits.packets.ConduitToolStatePacket;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;

public class ConduitToolScreenPacketHandler {
  interface Handler {
    void onPacket(ConduitToolStatePacket packet);
  }

  private static final ArrayList<Handler> handlers = new ArrayList<>();

  public static void register(Handler handler) {
    handlers.add(handler);
  }

  public static void unregister(Handler handler) {
    handlers.remove(handler);
  }

  public static void handle(ConduitToolStatePacket packet, NetworkEvent.Context ctx) {
    for (Handler handler : handlers) {
      handler.onPacket(packet);
    }
  }
}
