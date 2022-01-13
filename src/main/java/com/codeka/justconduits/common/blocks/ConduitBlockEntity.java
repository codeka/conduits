package com.codeka.justconduits.common.blocks;

import com.codeka.justconduits.client.blocks.ConduitModelProps;
import com.codeka.justconduits.common.ModBlockEntities;
import com.codeka.justconduits.common.ModCapabilities;
import com.codeka.justconduits.common.capabilities.network.ConduitHolder;
import com.codeka.justconduits.common.capabilities.network.ConduitNetworkManager;
import com.codeka.justconduits.common.capabilities.network.ConduitType;
import com.codeka.justconduits.common.capabilities.network.IConduitNetworkManager;
import com.codeka.justconduits.common.capabilities.network.NetworkType;
import com.codeka.justconduits.common.items.ConduitItem;
import com.codeka.justconduits.common.shape.ShapeManager;
import com.codeka.justconduits.helpers.SelectionHelper;
import com.codeka.justconduits.packets.ConduitClientStatePacket;
import com.codeka.justconduits.packets.ConduitUpdatePacket;
import com.codeka.justconduits.packets.IConduitTypeClientStatePacket;
import com.codeka.justconduits.packets.JustConduitsPacketHandler;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ConduitBlockEntity extends BlockEntity {
  private static final Logger L = LogManager.getLogger();

  public static final String SCREEN_CONDUIT_CONNECTION = "screen.conduit_connection";

  private final ConduitNetworkManager conduitNetworkManager = new ConduitNetworkManager();
  private final LazyOptional<IConduitNetworkManager> conduitNetworkManagerLazyOptional =
      LazyOptional.of(() -> this.conduitNetworkManager);

  private boolean firstTick = true;
  private final HashMap<Direction, ConduitConnection> connections = new HashMap<>();

  // A collection of the conduits in this block. We map NetworkType to ConduitHolder to ensure that we cannot have more
  // than one conduit of the same network type in our blockspace.
  private final HashMap<NetworkType, ConduitHolder> conduits = new HashMap<>();

  // We also keep a mapping of conduit types to conduit holder. This should be kept in sync with conduits.
  private final HashMap<ConduitType, ConduitHolder> conduitsByType = new HashMap<>();

  // The ShapeBuilder used to create the various shapes needed to render, collide and select this block.
  private final ShapeManager shapeManager = new ShapeManager(this);

  public ConduitBlockEntity(BlockPos blockPos, BlockState blockState) {
    super(ModBlockEntities.CONDUIT.get(), blockPos, blockState);
  }

  public Collection<ConduitConnection> getConnections() {
    return connections.values();
  }

  /** Gets the {@link ConduitConnection} in the given {@link Direction}.
   *
   * @param dir The {@link Direction} to look at.
   * @return The {@link ConduitConnection} in the given direction, or null if there's no connection in that direction.
   */
  @Nullable
  public ConduitConnection getConnection(Direction dir) {
    return connections.get(dir);
  }

  @Nullable
  public ConduitHolder getConduitHolder(NetworkType networkType) {
    return conduits.get(networkType);
  }

  @Nullable
  public ConduitHolder getConduitHolder(ConduitType conduitType) {
    return conduitsByType.get(conduitType);
  }

  /** Gets all of the {@link ConduitHolder}s we have in us. */
  public Collection<ConduitHolder> getConduitHolders() {
    return conduits.values();
  }

  /** Gets a collection of the {@link ConduitType}s in this block. */
  public Collection<ConduitType> getConduitTypes() {
    return conduitsByType.keySet();
  }

  @Nonnull
  public ShapeManager getShapeManager() {
    return shapeManager;
  }

  /** Gets the name of the block that this connection is connected to. */
  public Component getConnectionName(ConduitConnection connection) {
    BlockPos blockPos = getBlockPos().relative(connection.getDirection());
    if (requireLevel().getBlockEntity(blockPos) instanceof Nameable nameable) {
      return nameable.hasCustomName() ? nameable.getCustomName() : nameable.getDisplayName();
    }

    BlockState blockState = requireLevel().getBlockState(getBlockPos().relative(connection.getDirection()));
    return blockState.getBlock().getName();
  }

  /**
   * Adds a new conduit of the given type to this block.
   *
   * @return The {@link ConduitHolder} that was added, or null if we cannot add this conduit type (e.g. if it already
   *         exists in the block).
   */
  @Nullable
  public ConduitHolder addConduit(ConduitType conduitType) {
    if (conduitsByType.containsKey(conduitType)) {
      return null;
    }

    ConduitHolder existingOfDifferentType = conduits.get(conduitType.getNetworkType());
    if (existingOfDifferentType != null) {
      // TODO: remove the existing one and add this new one.
      return null;
    }

    ConduitHolder conduitHolder = new ConduitHolder(conduitType);
    conduits.put(conduitType.getNetworkType(), conduitHolder);
    conduitsByType.put(conduitType, conduitHolder);
    return conduitHolder;
  }

  /**
   * Called when a neighbor is updated.
   *
   * If the neighbor is another conduit, we'll need to join with it (e.g. if a new conduit was added to it). If it's
   * nothing, then we might need to remove it from our network or split the network. If it's an inventory, we'll want
   * to make sure to attach to it, etc etc.
   */
  public void onNeighborChanged(@Nonnull BlockState blockState, @Nonnull BlockPos neighborBlockPos) {
    int dx = neighborBlockPos.getX() - worldPosition.getX();
    int dy = neighborBlockPos.getY() - worldPosition.getY();
    int dz = neighborBlockPos.getZ() - worldPosition.getZ();
    Direction dir = Direction.fromNormal(dx, dy, dz);
    if (dir == null) {
      return;
    }

    updateNeighbor(dir, neighborBlockPos);
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
    return ConduitModelProps.getModelData(this);
  }

  /**
   * This is called when you right-click the block. We'll need to do a sub-hit detection to know which connection you
   * clicked on, and then handle it appropriately.
   *
   * @param player The {@link Player} that performed the right-click.
   * @param hand The {@link InteractionHand} that they used to click.
   * @param blockHitResult The {@link BlockHitResult} that caused us to be activated.
   * @param isClientSide If true, we're on the client side and shouldn't try to open any menus etc. But we'll still want
   *                     to return {@link InteractionResult#SUCCESS} so we play the animation.
   */
  public InteractionResult use(Player player, InteractionHand hand, BlockHitResult blockHitResult,
                               boolean isClientSide) {
    SelectionHelper.SelectionResult selectionResult = SelectionHelper.raycast(this, player);
    if (selectionResult == null) {
      return InteractionResult.PASS;
    }

    if (hand != InteractionHand.MAIN_HAND) {
      // If you clicked with your off hand, nothing to do.
      // TODO: clicking with a wrench in your offhand might do something?
      return InteractionResult.PASS;
    }

    if (selectionResult.connection() == null
        || selectionResult.connection().getConnectionType() != ConduitConnection.ConnectionType.EXTERNAL) {
      // If you're holding a conduit item that we don't currently have a conduit for, add that conduit to our block.
      ItemStack holdingStack = player.getItemInHand(hand);
      // TODO: check for a capability or something, rather than just being a subclass of our own implementation.
      if (holdingStack.getItem() instanceof ConduitItem conduitItem) {
        if (isClientSide) {
          // On the client-side, just check if we think it's possible to place this conduit.
          boolean canAddConduit = !conduitsByType.containsKey(conduitItem.getConduitType());
          if (canAddConduit) {
            conduitItem.onPlacedOrAdded(
                requireLevel(), player, holdingStack, getBlockState(), getBlockPos(), true);
          }
          return canAddConduit ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        // On the server-side, actually try to add the conduit.
        ConduitHolder conduitHolder = addConduit(conduitItem.getConduitType());
        if (conduitHolder == null) {
          // We weren't able to add it (e.g. because you're holding a conduit type that we already have), pass through
          // so the normal handling can happen (e.g. adding to the next block over).
          return InteractionResult.PASS;
        }

        // We added the conduit, so we're done.
        conduitItem.onPlacedOrAdded(requireLevel(), player, holdingStack, getBlockState(), getBlockPos(), false);
        setChanged();
        sendClientUpdate();
        return InteractionResult.SUCCESS;
      }

      // TODO: If you're holding a wrench, we should handle that by, e.g. disconnecting the connection.
      return InteractionResult.PASS;
    }

    // You've right-clicked an external conduit, so if we're on the client, just return success so we get the
    // animation. On the server we'll show the connection menu.
    if (isClientSide) {
      return InteractionResult.SUCCESS;
    }

    ConduitContainerMenu.MenuExtras menuExtras =
        new ConduitContainerMenu.MenuExtras(this, selectionResult.connection());
    MenuProvider menuProvider = new MenuProvider() {
      @Nonnull
      @Override
      public Component getDisplayName() {
        return new TranslatableComponent(SCREEN_CONDUIT_CONNECTION);
      }

      @Override
      public AbstractContainerMenu createMenu(int containerId, @Nonnull Inventory inventory, @Nonnull Player player) {
        return new ConduitContainerMenu(containerId, player.getInventory(), player, menuExtras);
      }
    };
    NetworkHooks.openGui((ServerPlayer) player, menuProvider, menuExtras);

    return InteractionResult.SUCCESS;
  }

  /**
   * This is our ticker, called every tick on the server.
   */
  public void tickServer() {
    if (firstTick) {
      // Set up the initial connections. There could be some already created as we loaded up, so we'll try to keep
      // what's already there.
      for (Direction dir : Direction.values()) {
        updateNeighbor(dir, /* blockPos = */ null);
      }
      if (!connections.isEmpty()) {
        sendClientUpdate();
        requireLevel().sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
      }

      conduitNetworkManager.init(this);
      firstTick = false;
    }

    if (conduits.isEmpty()) {
      return;
    }

    // TODO: keep a flag to see if we need to tick, if there's no external connections we can skip the loop
    for (ConduitHolder conduit : conduits.values()) {
      conduit.getConduitImpl().tickServer(requireLevel(), this, conduit);
    }
  }

  /**
   * This is called when a chunk is loaded. We need to save our custom properties and so on, so that we can load them
   * up when the chunk is re-loaded.
   */
  @Override
  public void load(@Nonnull CompoundTag tag) {
    super.load(tag);

    CompoundTag connectionsTag = tag.getCompound("Connections");
    for (String dirName : connectionsTag.getAllKeys()) {
      Direction dir = Direction.byName(dirName);
      if (dir == null) {
        L.atWarn().log("Skipping unknown direction: {}", dirName);
        continue;
      }

      CompoundTag connectionTag = connectionsTag.getCompound(dirName);
      // TODO: load stuff from the connection?

      ConduitConnection conn = getConnection(dir);
      if (conn == null) {
        // If we don't have a connection yet, we'll want to add one.
        conn = new ConduitConnection(getBlockPos(), dir, ConduitConnection.ConnectionType.EXTERNAL);
        connections.put(dir, conn);
      }
    }

    CompoundTag conduitsTag = tag.getCompound("Conduits");
    for (String conduitTypeName : conduitsTag.getAllKeys()) {
      ConduitType conduitType = ConduitType.fromName(conduitTypeName);
      if (conduitType == null) {
        L.atWarn().log("Skipping unknown conduit type: {}", conduitTypeName);
        continue;
      }

      ConduitHolder conduitHolder = conduitsByType.get(conduitType);
      if (conduitHolder == null) {
        conduitHolder = addConduit(conduitType);
      }
      if (conduitHolder != null) {
        conduitType.getConduitImpl().loadAdditional(conduitsTag.getCompound(conduitTypeName), this, conduitHolder);
      }
    }
  }

  /**
   * Unlike {@link #getUpdateTag()}, we use all the features of {@link CompoundTag} because we need to be compatible
   * with different versions of the mod.
   */
  @Override
  protected void saveAdditional(@Nonnull CompoundTag tag) {
    super.saveAdditional(tag);

    CompoundTag conduitTypes = new CompoundTag();
    for (var entry : conduitsByType.entrySet()) {
      ConduitType conduitType = entry.getKey();
      ConduitHolder conduitHolder = entry.getValue();

      CompoundTag conduitTag = new CompoundTag();
      conduitType.getConduitImpl().saveAdditional(conduitTag, this, conduitHolder);
      conduitTypes.put(conduitType.getName(), conduitTag);
    }
    tag.put("Conduits", conduitTypes);

    CompoundTag connectionsTag = new CompoundTag();
    for (ConduitConnection conn : connections.values()) {
      // No need to save non-external connections.
      if (conn.getConnectionType() != ConduitConnection.ConnectionType.EXTERNAL) {
        continue;
      }
      CompoundTag connectionTag = new CompoundTag();
      connectionTag.putString("Direction", conn.getDirection().getName());
      connectionsTag.put(conn.getDirection().getName(), connectionTag);
    }
    tag.put("Connections", connectionsTag);
  }

  /**
   * When the chunk is loaded, we need to synchronize the client. We'll send the contents of our normal state packet
   * here.
   */
  @Nonnull
  @Override
  public CompoundTag getUpdateTag() {
    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
    new ConduitClientStatePacket(this).encode(buffer);
    buffer.resetReaderIndex();

    CompoundTag tag = new CompoundTag();
    tag.putByteArray("state", buffer.array());
    return tag;
  }

  @Override
  public void handleUpdateTag(CompoundTag tag) {
    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(tag.getByteArray("state")));
    onClientUpdate(new ConduitClientStatePacket(buffer));
  }

  /** Updated any clients tracking this {@link ConduitBlockEntity} that something has changed. */
  public void sendClientUpdate() {
    JustConduitsPacketHandler.CHANNEL.send(
        PacketDistributor.TRACKING_CHUNK.with(() -> requireLevel().getChunkAt(getBlockPos())),
        new ConduitClientStatePacket(this));
  }

  /** Called on the client when we receive an update packet from the server. */
  public void onClientUpdate(ConduitClientStatePacket packet) {
    HashMap<Direction, ConduitConnection> newConnections = packet.getConnections();

    boolean updated = false;
    for (Direction dir : new ArrayList<>(connections.keySet())) {
      if (!newConnections.containsKey(dir)) {
        connections.remove(dir);
        updated = true;
      }
    }
    for (var entry : newConnections.entrySet()) {
      Direction dir = entry.getKey();
      ConduitConnection conn = entry.getValue();

      if (!connections.containsKey(dir)) {
        connections.put(dir, conn);
        updated = true;
      } else {
        updated = connections.get(dir).updateFrom(conn) || updated;
      }
    }

    for (Map.Entry<ConduitType, IConduitTypeClientStatePacket> entry : packet.getConduits().entrySet()) {
      ConduitType conduitType = entry.getKey();
      ConduitHolder conduitHolder = conduits.get(conduitType.getNetworkType());
      if (conduitHolder == null) {
        conduitHolder = new ConduitHolder(conduitType);
        conduits.put(conduitType.getNetworkType(), conduitHolder);
        conduitsByType.put(conduitType, conduitHolder);
        updated = true;
      }
      conduitHolder.getConduitImpl().onClientUpdate(entry.getValue(), this, conduitHolder);
    }

    if (updated) {
      // If something has actually changed, we'll need to update the model.
      ModelDataManager.requestModelDataRefresh(this);
      requireLevel().sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
      shapeManager.markDirty();
    }
  }

  /** Called on the server when a client wants to update us in some way. */
  public void onServerUpdate(ConduitUpdatePacket packet) {
    ConduitHolder conduitHolder = conduits.get(packet.getNetworkType());
    conduitHolder.getConduitImpl().onServerUpdate(packet, this, conduitHolder);

    // Mark ourselves as dirty as we've just updated ourselves.
    setChanged();

    // And send an update to the client so it can reflect any changes, too.
    sendClientUpdate();
  }

  /** Gets the {@link Level}, throws an exception if it's null. */
  @Nonnull
  private Level requireLevel() {
    Level l = level;
    if (l == null) {
      throw new RuntimeException("Unexpected null level");
    }
    return l;
  }

  // Note: passing null for blockPos just means we'll calculate it ourselves. You can save the calculation if you
  // already know it.
  private void updateNeighbor(Direction dir, @Nullable BlockPos blockPos) {
    if (blockPos == null) {
      blockPos = getBlockPos().relative(dir);
    }

    boolean needUpdate = false;
    BlockEntity neighbor = requireLevel().getBlockEntity(blockPos);
    if (neighbor == null && connections.containsKey(dir)) {
      connections.remove(dir);
      needUpdate = true;
    } else if (neighbor instanceof ConduitBlockEntity) {
      ConduitConnection conn = connections.get(dir);
      if (conn == null || conn.getConnectionType() != ConduitConnection.ConnectionType.CONDUIT) {
        conn = new ConduitConnection(getBlockPos(), dir, ConduitConnection.ConnectionType.CONDUIT);
        connections.put(dir, conn);
        needUpdate = true;
      }
    } else if (neighbor != null) {
      ConduitConnection conn = connections.get(dir);
      if (conn == null || conn.getConnectionType() != ConduitConnection.ConnectionType.EXTERNAL) {
        // Ask each of our conduits if it can connect to this block. If none of them can, we won't make a connection.
        boolean canConnect = false;
        for (ConduitHolder conduitHolder : conduits.values()) {
          canConnect = canConnect || conduitHolder.getConduitImpl().canConnect(neighbor, blockPos, dir.getOpposite());
        }

        if (canConnect) {
          conn = new ConduitConnection(getBlockPos(), dir, ConduitConnection.ConnectionType.EXTERNAL);
          connections.put(dir, conn);
          needUpdate = true;
        } else if (conn != null) {
          // We can't connect, but there's already a connection, we need to remove it.
          connections.remove(dir);
          needUpdate = true;
        }
      }

      // TODO: it might be something else that we want to connect to.
    }

    if (needUpdate) {
      sendClientUpdate();
      shapeManager.markDirty();
      requireLevel().sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }
  }
}
