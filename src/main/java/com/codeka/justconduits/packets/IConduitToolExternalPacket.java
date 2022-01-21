package com.codeka.justconduits.packets;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Interface implemented by classes that want to send packets to the client to display in the conduit tool gui.
 */
public interface IConduitToolExternalPacket {
  void encode(FriendlyByteBuf buffer);
  void decode(FriendlyByteBuf buffer);
}
