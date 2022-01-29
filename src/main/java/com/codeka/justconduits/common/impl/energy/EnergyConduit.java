package com.codeka.justconduits.common.impl.energy;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.impl.ConduitHolder;
import com.codeka.justconduits.common.impl.ConnectionMode;
import com.codeka.justconduits.common.impl.NetworkRegistry;
import com.codeka.justconduits.common.impl.common.CommonConduit;
import com.codeka.justconduits.common.impl.item.ItemConduit;
import com.codeka.justconduits.common.impl.item.ItemExternalConnection;
import com.codeka.justconduits.common.impl.item.ItemNetwork;
import com.codeka.justconduits.packets.IConduitToolExternalPacket;
import com.codeka.justconduits.packets.IConduitTypeClientStatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class EnergyConduit extends CommonConduit {
  private static final Logger L = LogManager.getLogger();

  @Override
  public void tickServer(
      @Nonnull Level level, @Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitHolder conduitHolder) {
    // TODO: make sure there is at least one energy conduit external connection here before we start ticking
    // so we can avoid the whole tick method entirely.

    // If we haven't been added to a network yet, nothing to tick.
    if (conduitHolder.getNetworkId() <= 0) {
      return;
    }

    EnergyNetwork network = NetworkRegistry.getNetwork(conduitHolder.getNetworkId());
    if (network == null) {
      L.atError().log("Network {} does not exist.", conduitHolder.getNetworkId());
      return;
    }

    if (!network.canExtract()) {
      // The network has no energy at the moment, nothing to do.
      return;
    }

    ArrayList<ConnectedEnergyStorage> connectedEnergyStorages = new ArrayList<>();
    for (ConduitConnection conduitConnection : network.getExternalConnections()) {
      if (conduitConnection.getConnectionType() != ConduitConnection.ConnectionType.EXTERNAL) {
        // Should always be external, but just in case.
        continue;
      }

      EnergyExternalConnection conn = conduitConnection.getNetworkExternalConnection(conduitHolder.getConduitType());
      if (conn.getInsertMode() != ConnectionMode.ALWAYS_ON) {
        // Energy conduits only do something when extracting.
        // TODO: handle redstone modes.
        continue;
      }

      BlockEntity blockEntity = conduitConnection.getConnectedBlockEntity(level);
      if (blockEntity == null) {
        continue;
      }
      Optional<IEnergyStorage> energyStorageOptional =
          blockEntity.getCapability(CapabilityEnergy.ENERGY, conduitConnection.getDirection().getOpposite()).resolve();
      if (energyStorageOptional.isPresent()) {
        ConnectedEnergyStorage connectedEnergyStorage = new ConnectedEnergyStorage(energyStorageOptional.get());
        if (!connectedEnergyStorage.energyStorage.canReceive()) {
          continue;
        }

        connectedEnergyStorages.add(connectedEnergyStorage);
      }
    }

    if (connectedEnergyStorages.size() == 0) {
      // Nothing to insert into.
      return;
    }

    // TODO: how do we decide how much energy to give each connected block? Here we just divide evenly between them
    //  but maybe we should prioritize the ones with the biggest buffer first? Or maybe it should be proportional to
    //  the size of the buffer? Or maybe we prioritize the *smallest* first, with the idea that they'll fill up and we
    //  can move on to others?
    int numAcceptingEnergy = connectedEnergyStorages.size();
    int energyPerConnection = network.getEnergyStored() / numAcceptingEnergy;
    while (network.getEnergyStored() > 0) {
      int numAcceptedEnergyThisLoop = 0;
      for (ConnectedEnergyStorage storage : connectedEnergyStorages) {
        int amount = storage.energyStorage.receiveEnergy(energyPerConnection, false);
        if (amount > 0) {
          numAcceptedEnergyThisLoop++;
        }
        if (amount != network.extractEnergy(amount, false)) {
          L.atWarn().log("Our network had enough energy, but it didn't when we tried to remove it.");
        }
      }

      // If they're all full, or there's none left to receive anything,
      if (numAcceptedEnergyThisLoop == 0) {
        break;
      }
      numAcceptingEnergy = numAcceptedEnergyThisLoop;
      energyPerConnection = network.getEnergyStored() / numAcceptingEnergy;
    }
  }

  @SuppressWarnings("unchecked")
  @Nullable
  public <T> LazyOptional<T> getCapability(
      @Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitHolder conduitHolder,
      @Nonnull Capability<T> capability, @Nullable Direction direction) {
    if (direction == null) {
      // We don't return a capability for the null direction, which is typically used by The One Probe, etc -- we
      // don't want to display what's in our "buffer" to those guys.
      return null;
    }

    EnergyNetwork energyNetwork = NetworkRegistry.getNetwork(conduitHolder.getNetworkId());
    if (energyNetwork == null) {
      // We haven't been set up yet?
      return null;
    }
    if (capability == CapabilityEnergy.ENERGY) {
      return LazyOptional.of(() -> (T) energyNetwork);
    }

    return null;
  }

  @Override
  @Nonnull
  public IConduitTypeClientStatePacket createClientState(@Nonnull ConduitBlockEntity conduitBlockEntity) {
    return new EnergyConduitClientStatePacket(conduitBlockEntity);
  }

  @Override
  public IConduitToolExternalPacket createConduitToolPacket(
      @NotNull ConduitBlockEntity conduitBlockEntity, @NotNull ConduitHolder conduitHolder) {
    return new EnergyConduitToolExternalConnectionPacket();
  }

  @Override
  public boolean canConnect(@Nonnull BlockEntity blockEntity, @Nonnull BlockPos blockPos, @Nonnull Direction face) {
    LazyOptional<IEnergyStorage> energyStorageOptional = blockEntity.getCapability(CapabilityEnergy.ENERGY, face);
    if (energyStorageOptional.resolve().isPresent()) {
      // TODO: use it? make modifications?
      return true;
    }

    return false;
  }

  private static class ConnectedEnergyStorage {
    public IEnergyStorage energyStorage;

    public ConnectedEnergyStorage(IEnergyStorage energyStorage) {
      this.energyStorage = energyStorage;
    }
  }
}
