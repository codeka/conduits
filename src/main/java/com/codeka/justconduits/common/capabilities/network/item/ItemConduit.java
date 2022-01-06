package com.codeka.justconduits.common.capabilities.network.item;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.capabilities.network.AbstractConduit;
import com.codeka.justconduits.common.capabilities.network.ConduitHolder;
import com.codeka.justconduits.common.capabilities.network.ConduitType;
import com.codeka.justconduits.common.capabilities.network.NetworkRegistry;
import com.codeka.justconduits.common.capabilities.network.NetworkType;
import com.codeka.justconduits.packets.ConduitUpdatePacket;
import com.codeka.justconduits.packets.IConduitTypeClientStatePacket;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
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
public class ItemConduit extends AbstractConduit {
  private static final Logger L = LogManager.getLogger();

  @Override
  public void tickServer(Level level, ConduitBlockEntity conduitBlockEntity, ConduitHolder conduitHolder) {
    // TODO: make sure there is at least one item conduit external connection here before we start ticking
    // so we can avoid the whole tick method entirely.

    // If we haven't been added to a network yet, nothing to tick.
    if (conduitHolder.getNetworkRef() == null) {
      return;
    }

    int itemsToTransfer = 8;

    for (ConduitConnection conduitConnection : conduitBlockEntity.getConnections()) {
      if (conduitConnection.getConnectionType() != ConduitConnection.ConnectionType.EXTERNAL) {
        continue;
      }

      ItemExternalConnection conn =
          conduitConnection.getNetworkExternalConnection(NetworkType.ITEM, conduitHolder.getConduitType());

      if (!conn.isExtractEnabled()) {
        // Item conduits only do something when extracting.
        continue;
      }

      // If it's not time to extract yet, then don't do anything.
      if (conn.ticksUntilNextExtract > 0) {
        conn.ticksUntilNextExtract --;
        return;
      }

      ItemNetwork network = NetworkRegistry.getNetwork(conduitHolder.getNetworkRef().getId());
      if (network == null) {
        L.atError().log("Network {} does not exist.", conduitHolder.getNetworkRef());
        continue;
      }

      // find an insert-enabled connection to insert items into.
      ArrayList<IItemHandler> candidateTargets = new ArrayList<>();
      for (ConduitConnection outputConnection : network.getExternalConnections()) {
        ItemExternalConnection outConn =
            outputConnection.getNetworkExternalConnection(NetworkType.ITEM, conduitHolder.getConduitType());
        if (!outConn.isInsertEnabled()) {
          // It doesn't have insert enabled, so we can't insert.
          continue;
        }

        if (outConn == conn) {
          // TODO: if self-insert is enabled, this is OK.
          continue;
        }

        candidateTargets.add(getItemHandler(level, outputConnection));
      }

      // TODO: handle speed upgrades.
      transferItems(level, conduitConnection, candidateTargets, itemsToTransfer);

      // TODO: config this, and also handle speed upgrades.
      conn.ticksUntilNextExtract = 20;
    }
  }

  @Override
  public void onClientUpdate(
      IConduitTypeClientStatePacket basePacket, ConduitBlockEntity conduitBlockEntity, ConduitHolder conduitHolder) {
    ItemConduitClientStatePacket packet = (ItemConduitClientStatePacket) basePacket;
    for (Map.Entry<Direction, ItemExternalConnection> entry : packet.getExternalConnections().entrySet()) {
      Direction dir = entry.getKey();

      ConduitConnection conn = conduitBlockEntity.getConnection(dir);
      if (conn == null) {
        // TODO error
        continue;
      }
      ItemExternalConnection existing = conn.getNetworkExternalConnection(NetworkType.ITEM, ConduitType.SIMPLE_ITEM);
      existing.setExtractEnabled(entry.getValue().isExtractEnabled());
      existing.setInsertEnabled(entry.getValue().isInsertEnabled());
    }
  }

  @Override
  public IConduitTypeClientStatePacket createClientState(ConduitBlockEntity conduitBlockEntity) {
    return new ItemConduitClientStatePacket(conduitBlockEntity);
  }

  @Override
  public void onServerUpdate(
      ConduitUpdatePacket packet, ConduitBlockEntity conduitBlockEntity, ConduitHolder conduitHolder) {
    ConduitConnection connection = conduitBlockEntity.getConnection(packet.getDirection());
    if (connection == null) {
      L.atError().log("No connection found when updating from client.");
      return;
    }

    ItemExternalConnection conn =
        connection.getNetworkExternalConnection(NetworkType.ITEM, conduitHolder.getConduitType());

    switch (packet.getUpdateType()) {
      case INSERT_ENABLED -> conn.setInsertEnabled(packet.getBoolValue());
      case EXTRACT_ENABLED -> conn.setExtractEnabled(packet.getBoolValue());
      default -> L.atError().log("Unexpected update type: {}", packet.getUpdateType());
    }
  }

  @Override
  public void saveAdditional(CompoundTag tag, ConduitBlockEntity conduitBlockEntity, ConduitHolder conduitHolder) {
    CompoundTag connectionsTag = new CompoundTag();
    for (ConduitConnection conduitConnection : conduitBlockEntity.getConnections()) {
      if (conduitConnection.getConnectionType() != ConduitConnection.ConnectionType.EXTERNAL) {
        continue;
      }

      CompoundTag connectionTag = new CompoundTag();
      ItemExternalConnection conn =
          conduitConnection.getNetworkExternalConnection(NetworkType.ITEM, conduitHolder.getConduitType());
      connectionTag.putBoolean("ExtractEnabled", conn.isExtractEnabled());
      connectionTag.putBoolean("InsertEnabled", conn.isInsertEnabled());
      connectionsTag.put(conduitConnection.getDirection().getName(), connectionTag);
    }

    tag.put("Connections", connectionsTag);
  }

  @Override
  public void loadAdditional(CompoundTag tag, ConduitBlockEntity conduitBlockEntity, ConduitHolder conduitHolder) {
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
      ItemExternalConnection conn =
          conduitConnection.getNetworkExternalConnection(NetworkType.ITEM, conduitHolder.getConduitType());
      conn.setExtractEnabled(connectionTag.getBoolean("ExtractEnabled"));
      conn.setInsertEnabled(connectionTag.getBoolean("InsertEnabled"));
    }
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
      @Nonnull Collection<IItemHandler> candidateTargets, int count) {
    IItemHandler fromItemHandler = getItemHandler(level, fromConnection);
    if (fromItemHandler == null) {
      return 0;
    }

    int totalTransferred = 0;

    // TODO: limit the number of slots we check per tick.
    for (int fromSlot = 0; fromSlot < fromItemHandler.getSlots(); fromSlot++) {
      ItemStack itemStack = fromItemHandler.extractItem(fromSlot, count, /* simulate = */ true);
      ItemStack remainingItems = itemStack;
      for (IItemHandler toInventory : candidateTargets) {
        remainingItems = insertItems(toInventory, remainingItems);
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
}
