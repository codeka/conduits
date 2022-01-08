package com.codeka.justconduits.common.capabilities.network.fluid;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.capabilities.network.AbstractConduit;
import com.codeka.justconduits.common.capabilities.network.ConduitHolder;
import com.codeka.justconduits.common.capabilities.network.ConduitType;
import com.codeka.justconduits.common.capabilities.network.NetworkType;
import com.codeka.justconduits.common.capabilities.network.item.ItemConduitClientStatePacket;
import com.codeka.justconduits.common.capabilities.network.item.ItemExternalConnection;
import com.codeka.justconduits.packets.ConduitUpdatePacket;
import com.codeka.justconduits.packets.IConduitTypeClientStatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Map;

public class FluidConduit extends AbstractConduit {
  private static final Logger L = LogManager.getLogger();

  @Override
  public void tickServer(
      @Nonnull Level level, @Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitHolder conduitHolder) {
  }

  @Override
  public void onClientUpdate(
      @Nonnull IConduitTypeClientStatePacket basePacket, @Nonnull ConduitBlockEntity conduitBlockEntity,
      @Nonnull ConduitHolder conduitHolder) {
    FluidConduitClientStatePacket packet = (FluidConduitClientStatePacket) basePacket;
    for (Map.Entry<Direction, FluidExternalConnection> entry : packet.getExternalConnections().entrySet()) {
      Direction dir = entry.getKey();

      ConduitConnection conn = conduitBlockEntity.getConnection(dir);
      if (conn == null) {
        // TODO error
        continue;
      }
      FluidExternalConnection existing = conn.getNetworkExternalConnection(conduitHolder.getConduitType());
      existing.setExtractEnabled(entry.getValue().isExtractEnabled());
      existing.setInsertEnabled(entry.getValue().isInsertEnabled());
    }
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
    ConduitConnection connection = conduitBlockEntity.getConnection(packet.getDirection());
    if (connection == null) {
      L.atError().log("No connection found when updating from client.");
      return;
    }

    FluidExternalConnection conn =
        connection.getNetworkExternalConnection(conduitHolder.getConduitType());

    switch (packet.getUpdateType()) {
      case INSERT_ENABLED -> conn.setInsertEnabled(packet.getBoolValue());
      case EXTRACT_ENABLED -> conn.setExtractEnabled(packet.getBoolValue());
      default -> L.atError().log("Unexpected update type: {}", packet.getUpdateType());
    }
  }

  @Override
  public void saveAdditional(
      @Nonnull CompoundTag tag, @Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitHolder conduitHolder) {
    CompoundTag connectionsTag = new CompoundTag();
    for (ConduitConnection conduitConnection : conduitBlockEntity.getConnections()) {
      if (conduitConnection.getConnectionType() != ConduitConnection.ConnectionType.EXTERNAL) {
        continue;
      }

      CompoundTag connectionTag = new CompoundTag();
      FluidExternalConnection conn =
          conduitConnection.getNetworkExternalConnection(conduitHolder.getConduitType());
      connectionTag.putBoolean("ExtractEnabled", conn.isExtractEnabled());
      connectionTag.putBoolean("InsertEnabled", conn.isInsertEnabled());
      connectionsTag.put(conduitConnection.getDirection().getName(), connectionTag);
    }

    tag.put("Connections", connectionsTag);
  }

  @Override
  public void loadAdditional(
      @Nonnull CompoundTag tag, @Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitHolder conduitHolder) {
    CompoundTag connectionsTag = tag.getCompound("Connections");
    for (String dirName : connectionsTag.getAllKeys()) {
      Direction dir = Direction.byName(dirName);
      if (dir == null) {
        L.atWarn().log("Unknown direction: {}", dirName);
        continue;
      }

      ConduitConnection conduitConnection = conduitBlockEntity.getConnection(dir);
      if (conduitConnection == null) {
        continue;
      }

      CompoundTag connectionTag = connectionsTag.getCompound(dirName);
      FluidExternalConnection conn =
          conduitConnection.getNetworkExternalConnection(conduitHolder.getConduitType());
      conn.setExtractEnabled(connectionTag.getBoolean("ExtractEnabled"));
      conn.setInsertEnabled(connectionTag.getBoolean("InsertEnabled"));
    }
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
