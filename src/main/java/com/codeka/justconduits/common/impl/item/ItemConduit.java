package com.codeka.justconduits.common.impl.item;

import com.codeka.justconduits.common.ChannelColor;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.impl.ConduitHolder;
import com.codeka.justconduits.common.impl.ConduitType;
import com.codeka.justconduits.common.impl.ConnectionMode;
import com.codeka.justconduits.common.impl.NetworkRegistry;
import com.codeka.justconduits.common.impl.common.CommonClientStatePacket;
import com.codeka.justconduits.common.impl.common.CommonConduit;
import com.codeka.justconduits.common.impl.common.CommonExternalConnection;
import com.codeka.justconduits.packets.ConduitUpdatePacket;
import com.codeka.justconduits.packets.IConduitToolExternalPacket;
import com.codeka.justconduits.packets.IConduitTypeClientStatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Contains the main "business logic" for the three kinds of item conduits (simple, regular and advanced).
 */
public class ItemConduit extends CommonConduit {
  private static final Logger L = LogManager.getLogger();

  @Override
  public void tickServer(
      @Nonnull Level level, @Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitHolder conduitHolder) {
    // TODO: make sure there is at least one item conduit external connection here before we start ticking
    // so we can avoid the whole tick method entirely.

    // If we haven't been added to a network yet, nothing to tick.
    if (conduitHolder.getNetworkId() <= 0) {
      return;
    }

    int itemsToTransfer = 8;

    for (ConduitConnection conduitConnection : conduitBlockEntity.getConnections()) {
      if (conduitConnection.getConnectionType() != ConduitConnection.ConnectionType.EXTERNAL) {
        continue;
      }

      ItemExternalConnection conn = conduitConnection.getNetworkExternalConnection(conduitHolder.getConduitType());

      if (conn.getExtractMode() != ConnectionMode.ALWAYS_ON) {
        // Item conduits only do something when extracting.
        // TODO: handle redstone modes.
        continue;
      }

      // If it's not time to extract yet, then don't do anything.
      if (conn.ticksUntilNextExtract > 0) {
        conn.ticksUntilNextExtract --;
        continue;
      }

      ItemNetwork network = NetworkRegistry.getNetwork(conduitHolder.getNetworkId());
      if (network == null) {
        L.atError().log("Network {} does not exist.", conduitHolder.getNetworkId());
        continue;
      }

      // find an insert-enabled connection to insert items into.
      ArrayList<InsertCandidate> candidateTargets = new ArrayList<>();
      for (ConduitConnection outputConnection : network.getExternalConnections()) {
        ItemExternalConnection outConn = outputConnection.getNetworkExternalConnection(conduitHolder.getConduitType());
        if (outConn.getInsertMode() != ConnectionMode.ALWAYS_ON) {
          // It doesn't have insert enabled, so we can't insert.
          // TODO: handle the redstone modes.
          continue;
        }

        if (outConn == conn) {
          // TODO: if self-insert is enabled, this is OK.
          continue;
        }

        candidateTargets.add(new InsertCandidate(outputConnection, getItemHandler(level, outputConnection)));
      }

      // TODO: handle speed upgrades.
      int numItems = transferItems(level, conduitConnection, candidateTargets, itemsToTransfer);

      if (network.isConduitToolOpen()) {
        // If the conduit tool is open, we need to record these stats.
        network.getStats(conduitConnection).itemsExtracted += numItems;
        for (InsertCandidate candidate : candidateTargets) {
          if (candidate.numTransferred > 0) {
            network.getStats(candidate.connection).itemsInserted += candidate.numTransferred;
          }
        }
      }

      // TODO: config this, and also handle speed upgrades.
      conn.ticksUntilNextExtract = 20;
    }
  }

  @Override
  public void onClientUpdate(
      @Nonnull IConduitTypeClientStatePacket basePacket, @Nonnull ConduitBlockEntity conduitBlockEntity,
      @Nonnull ConduitHolder conduitHolder) {
    super.onClientUpdate(basePacket, conduitBlockEntity, conduitHolder);
    ItemConduitClientStatePacket packet = (ItemConduitClientStatePacket) basePacket;
    for (Map.Entry<Direction, ItemExternalConnection> entry : packet.getExternalConnections().entrySet()) {
      Direction dir = entry.getKey();

      ConduitConnection conn = conduitBlockEntity.getConnection(dir);
      if (conn == null) {
        // TODO error
        continue;
      }
      ItemExternalConnection existing = conn.getNetworkExternalConnection(conduitHolder.getConduitType());
      existing.setExtractChannelColor(entry.getValue().getExtractChannelColor());
      existing.setInsertChannelColor(entry.getValue().getInsertChannelColor());
    }
  }

  @Override
  public void onServerUpdate(
      @Nonnull ConduitUpdatePacket packet, @Nonnull ConduitBlockEntity conduitBlockEntity,
      @Nonnull ConduitHolder conduitHolder) {
    super.onServerUpdate(packet, conduitBlockEntity, conduitHolder);
    ConduitConnection connection = conduitBlockEntity.getConnection(packet.getDirection());
    if (connection == null) {
      L.atError().log("No connection found when updating from client.");
      return;
    }

    ItemExternalConnection conn = connection.getNetworkExternalConnection(conduitHolder.getConduitType());
    switch (packet.getUpdateType()) {
      case INSERT_CHANNEL_COLOR -> conn.setInsertChannelColor(ChannelColor.fromNumber(packet.getIntValue()));
      case EXTRACT_CHANNEL_COLOR -> conn.setExtractChannelColor(ChannelColor.fromNumber(packet.getIntValue()));
    }
  }

  @Override
  @Nonnull
  public IConduitTypeClientStatePacket createClientState(@Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitType conduitType) {
    return new ItemConduitClientStatePacket(conduitBlockEntity, conduitType);
  }

  @Override
  public void saveAdditional(
      @Nonnull CompoundTag tag, @Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitHolder conduitHolder) {
    super.saveAdditional(tag, conduitBlockEntity, conduitHolder);

    CompoundTag connectionsTag = tag.getCompound("Connections");
    for (ConduitConnection conduitConnection : conduitBlockEntity.getConnections()) {
      if (conduitConnection.getConnectionType() != ConduitConnection.ConnectionType.EXTERNAL) {
        continue;
      }

      CompoundTag connectionTag = connectionsTag.getCompound(conduitConnection.getDirection().getName());
      ItemExternalConnection conn = conduitConnection.getNetworkExternalConnection(conduitHolder.getConduitType());
      connectionTag.putInt("ExtractChannelColor", conn.getExtractChannelColor().getNumber());
      connectionTag.putInt("InsertChannelColor", conn.getInsertChannelColor().getNumber());
    }
  }

  @Override
  public void loadAdditional(
      @Nonnull CompoundTag tag, @Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitHolder conduitHolder) {
    super.loadAdditional(tag, conduitBlockEntity, conduitHolder);

    CompoundTag connectionsTag = tag.getCompound("Connections");
    for (String dirName : connectionsTag.getAllKeys()) {
      Direction dir = Direction.byName(dirName);
      if (dir == null) {
        continue;
      }

      ConduitConnection conduitConnection = conduitBlockEntity.getConnection(dir);
      if (conduitConnection == null) {
        continue;
      }

      CompoundTag connectionTag = connectionsTag.getCompound(dirName);
      ItemExternalConnection conn = conduitConnection.getNetworkExternalConnection(conduitHolder.getConduitType());
      conn.setExtractChannelColor(ChannelColor.fromNumber(connectionTag.getInt("ExtractChannelColor")));
      conn.setInsertChannelColor(ChannelColor.fromNumber(connectionTag.getInt("InsertChannelColor")));
    }
  }

  @Override
  public void onConduitToolOpen(@Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitHolder conduitHolder) {
    ItemNetwork network = NetworkRegistry.getNetwork(conduitHolder.getNetworkId());
    if (network == null) {
      L.atError().log("network is null");
      return;
    }

    network.onConduitToolOpen();
  }

  @Override
  public void onConduitToolClose(@Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitHolder conduitHolder) {
    ItemNetwork network = NetworkRegistry.getNetwork(conduitHolder.getNetworkId());
    if (network == null) {
      L.atError().log("network is null");
      return;
    }

    network.onConduitToolClose();
  }

  @Override
  public IConduitToolExternalPacket createConduitToolPacket(
      @Nonnull ConduitBlockEntity conduitBlockEntity, @Nonnull ConduitHolder conduitHolder) {
    ItemConduitToolExternalConnectionPacket externalConnectionPacket = new ItemConduitToolExternalConnectionPacket();
    ItemNetwork network = NetworkRegistry.getNetwork(conduitHolder.getNetworkId());
    if (network == null) {
      L.atError().log("network is null");
      return null;
    }
    externalConnectionPacket.init(conduitBlockEntity, network, conduitHolder);
    return externalConnectionPacket;
  }

  @Override
  public boolean canConnect(@Nonnull BlockEntity blockEntity, @Nonnull BlockPos blockPos, @Nonnull Direction face) {
    LazyOptional<IItemHandler> itemHandlerOptional =
        blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face);
    if (itemHandlerOptional.resolve().isPresent()) {
      IItemHandler itemHandler = itemHandlerOptional.resolve().get();
      // TODO: use it? make modifications?

      return true;
    }

    return false;
  }

  /**
   * Transfers items between the two given {@link ConduitConnection}s, if possible.
   *
   * @param level The {@link Level}.
   * @param fromConnection The {@link ConduitConnection} to transfer from.
   * @param candidateTargets A list of {@link IItemHandler}s that we'll try to insert into.
   * @param count The maximum number of items to transfer.
   * @return The number of items actually transferred. This could be zero if, for example, the to connection does
   *         not have space, etc.
   */
  private int transferItems(
      @Nonnull Level level, @Nonnull ConduitConnection fromConnection,
      @Nonnull Collection<InsertCandidate> candidateTargets, int count) {
    IItemHandler fromItemHandler = getItemHandler(level, fromConnection);
    if (fromItemHandler == null) {
      return 0;
    }

    int totalTransferred = 0;

    // TODO: limit the number of slots we check per tick.
    for (int fromSlot = 0; fromSlot < fromItemHandler.getSlots(); fromSlot++) {
      ItemStack itemStack = fromItemHandler.extractItem(fromSlot, count, /* simulate = */ true);
      ItemStack remainingItems = itemStack;
      for (InsertCandidate candidate : candidateTargets) {
        ItemStack thisRemainingItems = insertItems(candidate.itemHandler, remainingItems);
        candidate.numTransferred += remainingItems.getCount() - thisRemainingItems.getCount();
        remainingItems = thisRemainingItems;
        if (remainingItems.isEmpty()) {
          break;
        }
      }

      if (itemStack.getCount() != remainingItems.getCount()) {
        // We actually transferred some items. Reduce the count by the number we transferred.
        int numTransferredFromThisSlot = itemStack.getCount() - remainingItems.getCount();
        count -= numTransferredFromThisSlot;
        totalTransferred += numTransferredFromThisSlot;

        // Actually remove the items from the source now.
        ItemStack itemsRemoved =
            fromItemHandler.extractItem(fromSlot, numTransferredFromThisSlot, /* simulate = */ false);
        // Check for duped items. It shouldn't happen, if the source inventory implements its interface correctly, but
        // it's always possible that it does something bad.
        if (itemsRemoved.getCount() != numTransferredFromThisSlot || itemsRemoved.getItem() != itemStack.getItem()) {
          L.atError().log(
              "Items extracted during execute phase different from simulate phase. " +
                  "Simulated extracting {} {}, but executed {} {}",
              numTransferredFromThisSlot, itemStack.getDisplayName(), itemsRemoved.getCount(),
              itemsRemoved.getDisplayName());
        }
      }

      if (count == 0) {
        break;
      }
    }

    return totalTransferred;
  }

  /**
   * Attempts to insert the items from the given {@link ItemStack} into the given inventory.
   *
   * @param toItemHandler The {@link IItemHandler} to insert into.
   * @param itemStack The {@link ItemStack} to insert.
   * @return An {@link ItemStack} of the remaining items that we weren't able to insert.
   */
  private ItemStack insertItems(@Nonnull IItemHandler toItemHandler, @Nonnull ItemStack itemStack) {
    return ItemHandlerHelper.insertItem(toItemHandler, itemStack, /* simulate = */ false);
  }

  /** Helper method to return the item handler for a given conduit connection. */
  @Nullable
  private IItemHandler getItemHandler(Level level, ConduitConnection connection) {
    BlockEntity toBlockEntity = connection.getConnectedBlockEntity(level);
    if (toBlockEntity == null) {
      return null;
    }
    Optional<IItemHandler> itemHandler =
        toBlockEntity.getCapability(
            CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
            connection.getDirection().getOpposite()).resolve();
    if (itemHandler.isEmpty()) {
      return null;
    }
    return itemHandler.get();
  }

  private static final class InsertCandidate {
    public final IItemHandler itemHandler;
    public final ConduitConnection connection;
    public int numTransferred;

    public InsertCandidate(ConduitConnection conn, IItemHandler itemHandler) {
      this.connection = conn;
      this.itemHandler = itemHandler;
    }
  }
}
