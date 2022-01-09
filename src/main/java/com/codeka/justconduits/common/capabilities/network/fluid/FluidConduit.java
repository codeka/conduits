package com.codeka.justconduits.common.capabilities.network.fluid;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.capabilities.network.ConduitHolder;
import com.codeka.justconduits.common.capabilities.network.common.CommonConduit;
import com.codeka.justconduits.packets.IConduitTypeClientStatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

public class FluidConduit extends CommonConduit {
  private static final Logger L = LogManager.getLogger();

  @Override
  public void tickServer(
      @Nonnull Level level, @Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitHolder conduitHolder) {
  }

  @Override
  @Nonnull
  public IConduitTypeClientStatePacket createClientState(@Nonnull ConduitBlockEntity conduitBlockEntity) {
    return new FluidConduitClientStatePacket(conduitBlockEntity);
  }

  @Override
  public boolean canConnect(@Nonnull BlockEntity blockEntity, @Nonnull BlockPos blockPos, @Nonnull Direction face) {
    LazyOptional<IFluidHandler> fluidHandlerOptional =
        blockEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face);
    if (fluidHandlerOptional.resolve().isPresent()) {
      IFluidHandler fluidHandler = fluidHandlerOptional.resolve().get();
      // TODO: use it? make modifications?

      return true;
    }

    return false;
  }
}
