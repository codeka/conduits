package com.codeka.justconduits.debug;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DebugPacket {
  public enum Action {
    DRAW_SHAPE,
    DRAW_VISUAL_SHAPE,
    DRAW_COLLISION_SHAPE,
    DRAW_CONDUIT_SHAPE,
  }

  private final Action action;

  public DebugPacket(Action action) {
    this.action = action;
  }

  public DebugPacket(FriendlyByteBuf buffer) {
    this.action = buffer.readEnum(Action.class);
  }

  public void encode(FriendlyByteBuf buffer) {
    buffer.writeEnum(action);
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    switch(action) {
      case DRAW_SHAPE -> DebugVoxelShapeHighlighter.drawShape = !DebugVoxelShapeHighlighter.drawShape;
      case DRAW_VISUAL_SHAPE ->
          DebugVoxelShapeHighlighter.drawVisualShape = !DebugVoxelShapeHighlighter.drawVisualShape;
      case DRAW_COLLISION_SHAPE ->
          DebugVoxelShapeHighlighter.drawCollisionShape = !DebugVoxelShapeHighlighter.drawCollisionShape;
      case DRAW_CONDUIT_SHAPE ->
          DebugVoxelShapeHighlighter.drawConduitShapes = !DebugVoxelShapeHighlighter.drawConduitShapes;
    }
  }
}
