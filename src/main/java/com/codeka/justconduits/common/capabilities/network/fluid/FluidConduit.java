package com.codeka.justconduits.common.capabilities.network.fluid;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.capabilities.network.AbstractConduit;
import com.codeka.justconduits.common.capabilities.network.ConduitHolder;
import com.codeka.justconduits.packets.ConduitUpdatePacket;
import com.codeka.justconduits.packets.IConduitTypeClientStatePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class FluidConduit extends AbstractConduit {
  @Override
  public void tickServer(Level level, ConduitBlockEntity conduitBlockEntity, ConduitHolder conduitHolder) {
  }

  @Override
  public void onClientUpdate(
      IConduitTypeClientStatePacket packet, ConduitBlockEntity conduitBlockEntity, ConduitHolder conduitHolder) {
  }

  @Override
  public IConduitTypeClientStatePacket createClientState(ConduitBlockEntity conduitBlockEntity) {
    return new FluidConduitClientStatePacket();
  }

  @Override
  public void onServerUpdate(
      ConduitUpdatePacket packet, ConduitBlockEntity conduitBlockEntity, ConduitHolder conduitHolder) {
  }

  @Override
  public void saveAdditional(CompoundTag tag, ConduitBlockEntity conduitBlockEntity, ConduitHolder conduitHolder) {
  }

  @Override
  public void loadAdditional(CompoundTag tag, ConduitBlockEntity conduitBlockEntity, ConduitHolder conduitHolder) {
  }
}
