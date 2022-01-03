package com.codeka.justconduits.packets;

import com.codeka.justconduits.common.blocks.ConduitBlockPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
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

  // Each packet encapsulates a single update of the conduit (or connection). This is the type of the update.
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

  public static Builder builder(BlockPos blockPos, Direction direction) {
    return new Builder(blockPos, direction);
  }

  // Used by the builder.
  private ConduitUpdatePacket() {}

  public ConduitUpdatePacket(FriendlyByteBuf buffer) {
    blockPos = buffer.readBlockPos();
    direction = buffer.readEnum(Direction.class);
    updateType = buffer.readEnum(UpdateType.class);
    if (buffer.readBoolean()) {
      boolValue = buffer.readBoolean();
    }
  }

  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBlockPos(blockPos);
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

  public UpdateType getUpdateType() {
    return updateType;
  }

  public boolean getBoolValue() {
    return boolValue != null && boolValue;
  }

  public static class Builder {
    private final ConduitUpdatePacket packet = new ConduitUpdatePacket();

    public Builder(BlockPos blockPos, Direction direction) {
      packet.blockPos = blockPos;
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
