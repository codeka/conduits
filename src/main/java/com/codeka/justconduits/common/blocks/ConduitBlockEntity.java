package com.codeka.justconduits.common.blocks;

import com.codeka.justconduits.client.blocks.ConduitModelProps;
import com.codeka.justconduits.common.ModBlockEntities;
import com.codeka.justconduits.common.ModCapabilities;
import com.codeka.justconduits.common.capabilities.network.ConduitNetworkManager;
import com.codeka.justconduits.common.capabilities.network.IConduitNetworkManager;
import com.codeka.justconduits.helpers.SelectionHelper;
import com.codeka.justconduits.packets.ConduitClientStatePacket;
import com.codeka.justconduits.packets.JustConduitsPacketHandler;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ConduitBlockEntity extends BlockEntity {
  private static final Logger L = LogManager.getLogger();

  private final ConduitNetworkManager conduitNetworkManager = new ConduitNetworkManager();
  private final LazyOptional<IConduitNetworkManager> conduitNetworkManagerLazyOptional =
      LazyOptional.of(() -> this.conduitNetworkManager);

  private boolean firstTick = true;
  private HashMap<Direction, ConduitConnection> connections = new HashMap<>();

  // We make the overall shape of the block the combined shape of all the connections, etc. That way, you can access
  // stuff behind us easily. But re-calculating that over and over is expensive, so we cache it here and only
  // re-calculate it when a connection actually changes.
  private VoxelShape shape;

  public ConduitBlockEntity(BlockPos blockPos, BlockState blockState) {
    super(ModBlockEntities.CONDUIT.get(), blockPos, blockState);
  }

  public Collection<ConduitConnection> getConnections() {
    return connections.values();
  }

  public VoxelShape getShape() {
    if (shape == null) {
      updateShape();
    }
    return shape;
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
    return new ModelDataMap.Builder()
        .withInitial(ConduitModelProps.CONNECTIONS, new ArrayList<>(connections.values()))
        .build();
  }

  public InteractionResult use(Player player, InteractionHand hand, BlockHitResult blockHitResult,
                               boolean isClientSide) {
    SelectionHelper.SelectionResult selectionResult = SelectionHelper.raycast(this, player);
    if (selectionResult == null) {
      return InteractionResult.PASS;
    }

    if (selectionResult.connection() == null
        || selectionResult.connection().getConnectionType() != ConduitConnection.ConnectionType.EXTERNAL) {
      // Ignore conduit connections, you can only use external connections.
      // TODO: if you're holding a wrench, disconnect the connectiuon.
      return InteractionResult.PASS;
    }

    // OK, this is something we can right-click on. If we're on the client, just return success so we get the animation.
    if (isClientSide) {
      return InteractionResult.SUCCESS;
    }

    L.atInfo().log("got a selection result: {}", selectionResult.connection());
    return InteractionResult.SUCCESS;
  }

  /**
   * This is our ticker, called every tick on the server.
   */
  public void tickServer() {
    if (firstTick) {
      // Set up the initial connections.
      connections.clear();
      for (Direction dir : Direction.values()) {
        updateNeighbor(dir, /* blockPos = */ null);
      }
      if (!connections.isEmpty()) {
        sendClientUpdate();
        requireLevel().sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
      }

      firstTick = false;
    }
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
    if (!connections.equals(newConnections)) {
      connections = newConnections;

      // If the connections have changed, we'll need to update the model.
      ModelDataManager.requestModelDataRefresh(this);
      requireLevel().sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    updateShape();
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
      // The other block is a conduit as well, make sure we're connected to it.
      if (!connections.containsKey(dir)) {
        connections.put(dir, new ConduitConnection(dir, ConduitConnection.ConnectionType.CONDUIT));
        needUpdate = true;
      }
    } else if (neighbor != null) {
      // TODO: check the types of conduits we have in our bundle, we'll only connect if it's one we support.
      LazyOptional<IItemHandler> itemHandlerOptional =
          neighbor.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite());
      if (itemHandlerOptional.resolve().isPresent()) {
        IItemHandler itemHandler = itemHandlerOptional.resolve().get();
        // TODO: use it?

        connections.put(dir, new ConduitConnection(dir, ConduitConnection.ConnectionType.EXTERNAL));
        needUpdate = true;
      }

      // TODO: it might be something else that we want to connect to.
    }

    if (needUpdate) {
      sendClientUpdate();
      updateShape();
      requireLevel().sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }
  }

  private void updateShape() {
    // TODO: start with the middle bit.
    VoxelShape shape = Shapes.box(0.375f, 0.375f, 0.375f, 0.625f, 0.625f, 0.625f);
    for (ConduitConnection conn : connections.values()) {
      shape = Shapes.or(shape, conn.getVoxelShape());
    }

    this.shape = shape;
  }
}
