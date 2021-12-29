package com.codeka.justconduits.common.blocks;

import com.codeka.justconduits.client.blocks.ConduitModelProps;
import com.codeka.justconduits.common.ModBlockEntities;
import com.codeka.justconduits.common.ModCapabilities;
import com.codeka.justconduits.common.capabilities.network.ConduitNetworkManager;
import com.codeka.justconduits.common.capabilities.network.IConduitNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;

public class ConduitBlockEntity extends BlockEntity {
  private static final Logger L = LogManager.getLogger();

  private final ConduitNetworkManager conduitNetworkManager = new ConduitNetworkManager();
  private final LazyOptional<IConduitNetworkManager> conduitNetworkManagerLazyOptional =
      LazyOptional.of(() -> this.conduitNetworkManager);

  private boolean firstTick = true;
  private HashSet<Direction> connections = new HashSet<>();

  public ConduitBlockEntity(BlockPos blockPos, BlockState blockState) {
    super(ModBlockEntities.CONDUIT.get(), blockPos, blockState);
  }

  /**
   * Called when a neighbor is updated.
   *
   * If the neighbor is another conduit, we'll need to join with it (e.g. if a new conduit was added to it). If it's
   * nothing, then we might need to remove it from our network or split the network. If it's an inventory, we'll want
   * to make sure to attach to it, etc etc.
   */
  public void onNeighborChanged(@Nonnull BlockState blockState, @Nonnull Level level, @Nonnull BlockPos blockPos,
                                @Nonnull BlockPos neighborBlockPos) {
    if (level.getBlockEntity(neighborBlockPos) instanceof ConduitBlockEntity neighbor) {
      // The other block is a conduit as well, make sure we're connected to it.
      int dx = neighborBlockPos.getX() - blockPos.getX();
      int dy = neighborBlockPos.getY() - blockPos.getY();
      int dz = neighborBlockPos.getZ() - blockPos.getZ();
      Direction dir = Direction.fromNormal(dx, dy, dz);
      if (dir != null && !connections.contains(dir)) {
        connections.add(dir);
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
      }

      //ModelDataManager.requestModelDataRefresh(this);
    }
    // TODO: if it's an inventory or accepts/produces power etc etc...
  }

  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction direction) {
    if (capability == ModCapabilities.CONDUIT_NETWORK_MANAGER && direction == null) {
      return this.conduitNetworkManagerLazyOptional.cast();
    }

    return super.getCapability(capability, direction);
  }

  @Override
  public void invalidateCaps() {
    super.invalidateCaps();
    this.conduitNetworkManagerLazyOptional.invalidate();
  }

  @Nonnull
  @Override
  public IModelData getModelData() {
    return new ModelDataMap.Builder()
        // TODO: actual model data.
        .withInitial(ConduitModelProps.CONNECTIONS, new ArrayList<>(connections))
        .build();
  }

  /**
   * This is our ticker, called every tick on the server.
   */
  public void tickServer() {
    if (firstTick) {
      // Set up the initial connections.
      connections.clear();
      for (Direction dir : Direction.values()) {
        if (level.getBlockEntity(getBlockPos().relative(dir)) instanceof ConduitBlockEntity) {
          connections.add(dir);
        }
      }
      if (!connections.isEmpty()) {
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
      }

      firstTick = false;
    }
  }

  /**
   * Called when the client needs to save a chunk. We'll save our stuff so we can display it correctly.
   */
  // TODO: maybe we just want for the server to tell us?
  @Override
  public CompoundTag getUpdateTag() {
    CompoundTag tag = super.getUpdateTag();
    saveClientData(tag);
    return tag;
  }

  /**
   * Called when the client loads a chunk. We'll get the values we saved in {@link #getUpdateTag()}.
   */
  // TODO: maybe we just want for the server to tell us?
  @Override
  public void handleUpdateTag(CompoundTag tag) {
    if (tag != null) {
      loadClientData(tag);
    }
  }

  /**
   * This is called on the server when we need to update the client. We'll use the update tag for our packet. For now.
   */
  // TODO: use a custom packet, the update packet is too inefficient.
  @Nullable
  @Override
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  @Override
  public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
    // This is called client side: remember the current state of the values that we're interested in
    HashSet<Direction> oldConnections = new HashSet<>(connections);

    CompoundTag tag = pkt.getTag();
    // This will call loadClientData()
    handleUpdateTag(tag);

    // If any of the connections are different, we request a refresh of our model data and send a block update (this
    // will cause the baked model to be recreated)
    if (!oldConnections.equals(connections)) {
      ModelDataManager.requestModelDataRefresh(this);
      level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }
  }

  // TODO: use a custom packet, this is waaaay too inefficient.
  private void saveClientData(CompoundTag tag) {
    CompoundTag infoTag = new CompoundTag();
    tag.put("Info", infoTag);
    Direction[] connectionValues = new Direction[connections.size()];
    connections.toArray(connectionValues);
    int[] connectionOrdinals = new int[connectionValues.length];
    for (int i = 0; i < connectionValues.length; i++) {
      connectionOrdinals[i] = connectionValues[i].ordinal();
    }
    infoTag.putIntArray("connections", connectionOrdinals);
  }

  private void loadClientData(CompoundTag tag) {
    if (tag.contains("Info")) {
      CompoundTag infoTag = tag.getCompound("Info");
      int[] ordinals = infoTag.getIntArray("connections");
      connections.clear();
      for (int i = 0; i < ordinals.length; i++) {
        Direction dir = Direction.values()[ordinals[i]];
        connections.add(dir);
      }
    }
  }

}
