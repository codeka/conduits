package com.codeka.justconduits.common.capabilities.network.item;

import com.codeka.justconduits.packets.IConduitTypeClientStatePacket;
import net.minecraft.network.FriendlyByteBuf;

/** Client state packet for {@link ItemConduit}s. */
public class ItemConduitClientStatePacket implements IConduitTypeClientStatePacket {
  @Override
  public void encode(FriendlyByteBuf buffer) {

  }

  @Override
  public void decode(FriendlyByteBuf buffer) {

  }
}
