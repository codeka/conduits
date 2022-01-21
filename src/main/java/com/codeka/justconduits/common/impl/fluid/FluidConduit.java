package com.codeka.justconduits.common.impl.fluid;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.impl.ConduitHolder;
import com.codeka.justconduits.common.impl.ConnectionMode;
import com.codeka.justconduits.common.impl.NetworkRegistry;
import com.codeka.justconduits.common.impl.common.CommonConduit;
import com.codeka.justconduits.packets.IConduitToolExternalPacket;
import com.codeka.justconduits.packets.IConduitTypeClientStatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class FluidConduit extends CommonConduit {
  private static final Logger L = LogManager.getLogger();

  @Override
  public void tickServer(
      @Nonnull Level level, @Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitHolder conduitHolder) {
    // TODO: make sure there is at least one fluid conduit external connection here before we start ticking
    // so we can avoid the whole tick method entirely.

    // If we haven't been added to a network yet, nothing to tick.
    if (conduitHolder.getNetworkId() <= 0) {
      return;
    }

    int mbToTransfer = 256;

    for (ConduitConnection conduitConnection : conduitBlockEntity.getConnections()) {
      if (conduitConnection.getConnectionType() != ConduitConnection.ConnectionType.EXTERNAL) {
        continue;
      }

      FluidExternalConnection conn = conduitConnection.getNetworkExternalConnection(conduitHolder.getConduitType());

      if (conn.getExtractMode() != ConnectionMode.ALWAYS_ON) {
        // Item conduits only do something when extracting.
        // TODO: handle redstone modes.
        continue;
      }

      // If it's not time to extract yet, then don't do anything.
      if (conn.ticksUntilNextExtract > 0) {
        conn.ticksUntilNextExtract --;
        return;
      }

      FluidNetwork network = NetworkRegistry.getNetwork(conduitHolder.getNetworkId());
      if (network == null) {
        L.atError().log("Network {} does not exist.", conduitHolder.getNetworkId());
        continue;
      }

      // find an insert-enabled connection to insert items into.
      ArrayList<IFluidHandler> candidateTargets = new ArrayList<>();
      for (ConduitConnection outputConnection : network.getExternalConnections()) {
        FluidExternalConnection outConn = outputConnection.getNetworkExternalConnection(conduitHolder.getConduitType());
        if (outConn.getInsertMode() != ConnectionMode.ALWAYS_ON) {
          // It doesn't have insert enabled, so we can't insert.
          // TODO: handle the redstone modes.
          continue;
        }

        if (outConn == conn) {
          // TODO: if self-insert is enabled, this is OK.
          continue;
        }

        candidateTargets.add(getFluidHandler(level, outputConnection));
      }

      // TODO: handle speed upgrades.
      transferFluid(level, conduitConnection, candidateTargets, mbToTransfer);

      // TODO: config this, and also handle speed upgrades.
      conn.ticksUntilNextExtract = 20;
    }
  }

  @Override
  @Nonnull
  public IConduitTypeClientStatePacket createClientState(@Nonnull ConduitBlockEntity conduitBlockEntity) {
    return new FluidConduitClientStatePacket(conduitBlockEntity);
  }

  @Override
  public IConduitToolExternalPacket createConduitToolPacket(
      @NotNull ConduitBlockEntity conduitBlockEntity, @NotNull ConduitHolder conduitHolder) {
    // TODO: implement.
    return null;
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

  /** Helper method to return the fluid handler for a given conduit connection. */
  @Nullable
  private IFluidHandler getFluidHandler(Level level, ConduitConnection connection) {
    BlockEntity toBlockEntity = connection.getConnectedBlockEntity(level);
    if (toBlockEntity == null) {
      return null;
    }
    Optional<IFluidHandler> fluidHandler =
        toBlockEntity.getCapability(
            CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
            connection.getDirection().getOpposite()).resolve();
    if (fluidHandler.isEmpty()) {
      return null;
    }
    return fluidHandler.get();
  }

  /**
   * Transfers fluid between the given from {@link ConduitConnection} to the given targets, if possible.
   *
   * @param level The {@link Level}.
   * @param fromConnection The {@link ConduitConnection} to transfer from.
   * @param candidateTargets A list of {@link IFluidHandler}s that we'll try to insert into.
   * @param amount The maximum amount of millibuckets to transfer.
   * @return The millibuckets actually transferred. This could be zero if, for example, the to connection does
   *         not have space, etc.
   */
  private int transferFluid(
      @Nonnull Level level, @Nonnull ConduitConnection fromConnection,
      @Nonnull Collection<IFluidHandler> candidateTargets, int amount) {
    IFluidHandler fromFluidHandler = getFluidHandler(level, fromConnection);
    if (fromFluidHandler == null) {
      return 0;
    }

    FluidStack fluidStack = fromFluidHandler.drain(amount, IFluidHandler.FluidAction.SIMULATE);
    if (fluidStack.isEmpty()) {
      // There's nothing left.
      return 0;
    }
    FluidStack remainingFluid = fluidStack.copy();
    for (IFluidHandler toTank : candidateTargets) {
      int amountFilled = toTank.fill(remainingFluid, IFluidHandler.FluidAction.EXECUTE);
      remainingFluid.setAmount(remainingFluid.getAmount() - amountFilled);
      if (remainingFluid.isEmpty()) {
        break;
      }
    }

    int amountTransferred = 0;
    if (fluidStack.getAmount() != remainingFluid.getAmount()) {
      // We actually transferred some fluid. Reduce the amount by the amount we transferred.
      amountTransferred = fluidStack.getAmount() - remainingFluid.getAmount();

      // Actually remove the fluid from the source now.
      FluidStack drainedFluid = fromFluidHandler.drain(amountTransferred, IFluidHandler.FluidAction.EXECUTE);
      // Check for mismatches between what we simulated before and what we actually drained this time. It shouldn't
      // happen, if the source block implements its interface correctly, but it's always possible that it does something
      // bad.
      if (drainedFluid.getAmount() != amountTransferred || drainedFluid.getFluid() != fluidStack.getFluid()) {
        L.atError().log(
            "Fluid extracted during execute phase different from simulate phase. " +
                "Simulated extracting {} {}, but executed {} {}",
            amountTransferred, fluidStack.getDisplayName(), drainedFluid.getAmount(), drainedFluid.getDisplayName());
      }
    }

    return amountTransferred;
  }
}
