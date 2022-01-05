package com.codeka.justconduits.packets;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Interface implemented by specific conduits (e.g. item, fluid, etc)
 */
public interface IConduitTypeClientStatePacket {
  void encode(FriendlyByteBuf buffer);

  void decode(FriendlyByteBuf buffer);
}
