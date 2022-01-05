package com.codeka.justconduits.packets;

import com.codeka.justconduits.common.blocks.ConduitBlockPacketHandler;
import com.codeka.justconduits.common.capabilities.network.NetworkType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * This is a packet sent from the client to the server whenever we want to update a conduit.
 */
public class ConduitUpdatePacket {
  /** The {@link BlockPos} of the block we're updating. */
  private BlockPos blockPos;

  /** The {@link Direction} of the connection we're updating. */
  // TODO: this might be nullable if there's settings to update
  private Direction direction;

  /** The {@link NetworkType} this update is for. */
  private NetworkType networkType;

  // Each packet encapsulates a single update of the conduit (or connection). This is the type of the update.
  // TODO: maybe this should be a CompoundTag so it's more extensible?
  public enum UpdateType {
    // We're updating the value of the insert enabled checkbox.
    INSERT_ENABLED,

    // We're updating the value of the extract enabled checkbox.
    EXTRACT_ENABLED,
  }
  private UpdateType updateType;

  /** When updating a boolean value, this is the new value. */
  @Nullable
  private Boolean boolValue;

  public static Builder builder(BlockPos blockPos, NetworkType networkType, Direction direction) {
    return new Builder(blockPos, networkType, direction);
  }

  // Used by the builder.
  private ConduitUpdatePacket() {}

  public ConduitUpdatePacket(FriendlyByteBuf buffer) {
    blockPos = buffer.readBlockPos();
    int len = buffer.readInt();
    networkType = NetworkType.fromName(buffer.readCharSequence(len, StandardCharsets.UTF_8).toString());
    direction = buffer.readEnum(Direction.class);
    updateType = buffer.readEnum(UpdateType.class);
    if (buffer.readBoolean()) {
      boolValue = buffer.readBoolean();
    }
  }

  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBlockPos(blockPos);
    buffer.writeInt(networkType.getName().length());
    buffer.writeCharSequence(networkType.getName(), StandardCharsets.UTF_8);
    buffer.writeEnum(direction);
    buffer.writeEnum(updateType);
    buffer.writeBoolean(boolValue != null);
    if (boolValue != null) {
      buffer.writeBoolean(boolValue);
    }
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ConduitBlockPacketHandler.handle(this, ctx);
    });
    ctx.get().setPacketHandled(true);
  }

  public BlockPos getBlockPos() {
    return blockPos;
  }

  public Direction getDirection() {
    return direction;
  }

  public NetworkType getNetworkType() {
    return networkType;
  }

  public UpdateType getUpdateType() {
    return updateType;
  }

  public boolean getBoolValue() {
    return boolValue != null && boolValue;
  }

  public static class Builder {
    private final ConduitUpdatePacket packet = new ConduitUpdatePacket();

    public Builder(BlockPos blockPos, NetworkType networkType, Direction direction) {
      packet.blockPos = blockPos;
      packet.networkType = networkType;
      packet.direction = direction;
    }

    public Builder withBooleanUpdate(UpdateType updateType, boolean value) {
      packet.updateType = updateType;
      packet.boolValue = value;
      return this;
    }

    public ConduitUpdatePacket build() {
      // TODO: validate?
      return packet;
    }
  }
}
