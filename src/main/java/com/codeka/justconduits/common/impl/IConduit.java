package com.codeka.justconduits.common.impl;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.packets.ConduitUpdatePacket;
import com.codeka.justconduits.packets.IConduitTypeClientStatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

/**
 * This is the interface that each conduit implements, and it provides the main logic for the conduit (e.g. transferring
 * items for an item conduit, transferring fluid for a fluid conduit, etc).
 */
public interface IConduit {
  /**
   * Called every tick (on the server), once for every loaded {@link ConduitBlockEntity}.
   */
  void tickServer(
      @Nonnull Level level, @Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitHolder conduitHolder);

  /**
   * Called on the client when some update comes in from the server.
   */
  void onClientUpdate(
      @Nonnull IConduitTypeClientStatePacket packet, @Nonnull ConduitBlockEntity conduitBlockEntity,
      @Nonnull ConduitHolder conduitHolder);

  /**
   * Creates a new {@link IConduitTypeClientStatePacket} for this conduit based on the current state of the
   * {@link ConduitBlockEntity}.
   */
  @Nonnull
  IConduitTypeClientStatePacket createClientState(@Nonnull ConduitBlockEntity conduitBlockEntity);

  /** Called on the server when the client sends us some update. */
  void onServerUpdate(
      @Nonnull ConduitUpdatePacket packet, @Nonnull ConduitBlockEntity conduitBlockEntity,
      @Nonnull ConduitHolder conduitHolder);

  /**
   * Called when the chunk is being saved. Write out all the stuff we need to remember between chunk load/unlocd.
   */
  void saveAdditional(
      @Nonnull CompoundTag tag, @Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitHolder conduitHolder);

  /**
   * Called when the chunk is being loaded.
   */
  void loadAdditional(
      @Nonnull CompoundTag tag, @Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitHolder conduitHolder);

  /**
   * Returns true if we can connect to the given block entity. For example, item conduits can connect if the
   * {@link BlockEntity} has an IItemHandler capability.
   *
   * @param blockEntity The {@link BlockEntity} we are trying to connect to.
   * @param blockPos The {@link BlockPos} of the block we are trying to connect to.
   * @param face The face we are connecting to.
   */
  boolean canConnect(@Nonnull BlockEntity blockEntity, @Nonnull BlockPos blockPos, @Nonnull Direction face);
}
