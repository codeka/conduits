package com.codeka.justconduits.common.impl.energy;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.impl.ConduitHolder;
import com.codeka.justconduits.common.impl.common.CommonConduit;
import com.codeka.justconduits.packets.IConduitTypeClientStatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

public class EnergyConduit extends CommonConduit {
  private static final Logger L = LogManager.getLogger();

  @Override
  public void tickServer(
      @Nonnull Level level, @Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitHolder conduitHolder) {
    // TODO: implememt me
  }

  @Override
  @Nonnull
  public IConduitTypeClientStatePacket createClientState(@Nonnull ConduitBlockEntity conduitBlockEntity) {
    return new EnergyConduitClientStatePacket(conduitBlockEntity);
  }

  @Override
  public boolean canConnect(@Nonnull BlockEntity blockEntity, @Nonnull BlockPos blockPos, @Nonnull Direction face) {
    // TODO: implement.
    return false;
  }
}
