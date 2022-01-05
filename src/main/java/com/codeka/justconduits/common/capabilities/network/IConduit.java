package com.codeka.justconduits.common.capabilities.network;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.packets.ConduitUpdatePacket;
import com.codeka.justconduits.packets.IConduitTypeClientStatePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

/**
 * This is the interface that each conduit implements, and it provides the main logic for the conduit (e.g. transferring
 * items for an item conduit, transferring fluid for a fluid conduit, etc).
 */
public interface IConduit {
  /**
   * Called every tick (on the server), once for every loaded {@link ConduitBlockEntity}.
   */
  void tickServer(Level level, ConduitBlockEntity conduitBlockEntity, ConduitHolder conduitHolder);

  /**
   * Called on the client when some update comes in from the server.
   */
  void onClientUpdate(
      IConduitTypeClientStatePacket packet, ConduitBlockEntity conduitBlockEntity, ConduitHolder conduitHolder);

  /**
   * Creates a new {@link IConduitTypeClientStatePacket} for this conduit based on the current state of the
   * {@link ConduitBlockEntity}.
   */
  IConduitTypeClientStatePacket createClientState(ConduitBlockEntity conduitBlockEntity);

  /** Called on the server when the client sends us some update. */
  void onServerUpdate(ConduitUpdatePacket packet, ConduitBlockEntity conduitBlockEntity, ConduitHolder conduitHolder);

  /**
   * Called when the chunk is being saved. Write out all the stuff we need to remember between chunk load/unlocd.
   */
  void saveAdditional(CompoundTag tag, ConduitBlockEntity conduitBlockEntity);

  /**
   * Called when the chunk is being loaded.
   */
  void loadAdditional(CompoundTag tag, ConduitBlockEntity conduitBlockEntity);
}
