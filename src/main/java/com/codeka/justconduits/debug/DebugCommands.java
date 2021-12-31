package com.codeka.justconduits.debug;

import com.codeka.justconduits.packets.ConduitClientStatePacket;
import com.codeka.justconduits.packets.JustConduitsPacketHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DebugCommands {
  private static final Logger L = LogManager.getLogger();

  public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
    return Commands.literal("debug")
        .then(
            Commands.literal("highlight")
                .then(Commands.literal("shape").executes(DebugCommands::highlightShape))
                .then(Commands.literal("visual-shape").executes(DebugCommands::highlightVisualShape))
                .then(Commands.literal("collision-shape").executes(DebugCommands::highlightCollisionShape)));
  }

  public static int highlightShape(CommandContext<CommandSourceStack> context) {
    if (context.getSource().getEntity() instanceof ServerPlayer player) {
      sendDebugPacket(player, new DebugPacket(DebugPacket.Action.DRAW_SHAPE));
    }
    return 0;
  }

  public static int highlightVisualShape(CommandContext<CommandSourceStack> context) {
    if (context.getSource().getEntity() instanceof ServerPlayer player) {
      sendDebugPacket(player, new DebugPacket(DebugPacket.Action.DRAW_VISUAL_SHAPE));
    }
    return 0;
  }

  public static int highlightCollisionShape(CommandContext<CommandSourceStack> context) {
    if (context.getSource().getEntity() instanceof ServerPlayer player) {
      sendDebugPacket(player, new DebugPacket(DebugPacket.Action.DRAW_COLLISION_SHAPE));
    }
    return 0;
  }

  private static void sendDebugPacket(ServerPlayer player,DebugPacket packet) {
    JustConduitsPacketHandler.CHANNEL.send(
        PacketDistributor.PLAYER.with(() -> player),
        packet);
  }
}
