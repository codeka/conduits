package com.codeka.justconduits.common.capabilities.network.fluid;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.capabilities.network.AbstractConduit;
import com.codeka.justconduits.common.capabilities.network.ConduitHolder;
import com.codeka.justconduits.packets.ConduitUpdatePacket;
import com.codeka.justconduits.packets.IConduitTypeClientStatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

public class FluidConduit extends AbstractConduit {
  @Override
  public void tickServer(
      @Nonnull Level level, @Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitHolder conduitHolder) {
  }

  @Override
  public void onClientUpdate(
      @Nonnull IConduitTypeClientStatePacket packet, @Nonnull ConduitBlockEntity conduitBlockEntity,
      @Nonnull ConduitHolder conduitHolder) {
  }

  @Override
  @Nonnull
  public IConduitTypeClientStatePacket createClientState(@Nonnull ConduitBlockEntity conduitBlockEntity) {
    return new FluidConduitClientStatePacket();
  }

  @Override
  public void onServerUpdate(
      @Nonnull ConduitUpdatePacket packet, @Nonnull ConduitBlockEntity conduitBlockEntity,
      @Nonnull ConduitHolder conduitHolder) {
  }

  @Override
  public void saveAdditional(
      @Nonnull CompoundTag tag, @Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitHolder conduitHolder) {
  }

  @Override
  public void loadAdditional(
      @Nonnull CompoundTag tag, @Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitHolder conduitHolder) {
  }

  @Override
  public boolean canConnect(@Nonnull BlockEntity blockEntity, @Nonnull BlockPos blockPos, @Nonnull Direction face) {
    return false;
  }
}
