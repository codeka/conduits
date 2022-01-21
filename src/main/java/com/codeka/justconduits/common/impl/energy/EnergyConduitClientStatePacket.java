package com.codeka.justconduits.common.impl.energy;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.impl.ConduitType;
import com.codeka.justconduits.common.impl.common.CommonClientStatePacket;
import net.minecraft.network.FriendlyByteBuf;

public class EnergyConduitClientStatePacket extends CommonClientStatePacket {
  public EnergyConduitClientStatePacket() {
    super(null, ConduitType.SIMPLE_ENERGY);
  }

  public EnergyConduitClientStatePacket(ConduitBlockEntity conduitBlockEntity) {
    super(conduitBlockEntity, ConduitType.SIMPLE_ENERGY);
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
