package com.codeka.justconduits.common.impl.fluid;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.impl.ConduitType;
import com.codeka.justconduits.common.impl.common.CommonClientStatePacket;
import net.minecraft.network.FriendlyByteBuf;

/** Client state packet for {@link FluidConduit}s. */
public class FluidConduitClientStatePacket extends CommonClientStatePacket {
  public FluidConduitClientStatePacket() {
    super(null, ConduitType.SIMPLE_FLUID);
  }

  public FluidConduitClientStatePacket(ConduitBlockEntity conduitBlockEntity) {
    super(conduitBlockEntity, ConduitType.SIMPLE_FLUID);
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    super.encode(buffer);

    // TODO: add more?
  }

  @Override
  public void decode(FriendlyByteBuf buffer) {
    super.decode(buffer);

    // TODO: add more?
  }
}
