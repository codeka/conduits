package com.codeka.justconduits.debug;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.helpers.LineHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Color;

/**
 * Helper for highlighting various {@link VoxelShape}s in the world.
 *
 * Modified for 1.18, based on code from https://github.com/TheGreyGhost/MinecraftByExample.
 */

public class DebugVoxelShapeHighlighter {
  private static final Logger L = LogManager.getLogger();

  private static final Color SHAPE_COLOR = Color.RED;
  private static final Color VISUAL_SHAPE_COLOR = Color.BLUE;
  private static final Color COLLISION_SHAPE_COLOR = Color.GREEN;
  private static final Color CONDUIT_SHAPE_COLOR = Color.YELLOW;

  static boolean drawShape = false;
  static boolean drawVisualShape = false;
  static boolean drawCollisionShape = false;
  static boolean drawConduitShapes = false;

  // NOTE: We want this to be higher than the default renderer (which we explicitly allow to proceed).
  @SubscribeEvent(priority = EventPriority.NORMAL)
  public static void onDrawBlockHighlightEvent(DrawSelectionEvent.HighlightBlock event) {
    BlockHitResult blockHitResult = event.getTarget();
    if (blockHitResult.getType() != BlockHitResult.Type.BLOCK) return;
    ClientLevel level = Minecraft.getInstance().level;
    if (level == null) {
      return;
    }

    BlockPos blockPos = blockHitResult.getBlockPos();
    BlockState blockstate = level.getBlockState(blockPos);
    if (blockstate.isAir() || !level.getWorldBorder().isWithinBounds(blockPos)) {
      return;
    }

    // If we're not drawing anything at all, just exit now.
    if (!drawShape && !drawVisualShape && !drawCollisionShape && !drawConduitShapes) {
      return;
    }

    final Camera camera = event.getCamera();
    final CollisionContext collisionContext = CollisionContext.of(camera.getEntity());
    final MultiBufferSource multiBufferSource = event.getMultiBufferSource();
    final PoseStack poseStack = event.getPoseStack();
    boolean cancelDefault = true;
    if (drawShape) {
      VoxelShape shape = blockstate.getShape(level, blockPos, collisionContext);
      LineHelper.drawSelectionBox(multiBufferSource, poseStack, blockPos, camera, shape, SHAPE_COLOR);
    }
    if (drawVisualShape) {
      VoxelShape shape = blockstate.getVisualShape(level, blockPos, collisionContext);
      LineHelper.drawSelectionBox(multiBufferSource, poseStack, blockPos, camera, shape, VISUAL_SHAPE_COLOR);
    }
    if (drawCollisionShape) {
      VoxelShape shape = blockstate.getCollisionShape(level, blockPos, collisionContext);
      LineHelper.drawSelectionBox(multiBufferSource, poseStack, blockPos, camera, shape, COLLISION_SHAPE_COLOR);
    }
    if (drawConduitShapes) {
      if (level.getBlockEntity(blockPos) instanceof ConduitBlockEntity conduitBlockEntity) {
        // TODO: draw the ConduitShape?
        for (ConduitConnection connection : conduitBlockEntity.getConnections()) {
          //LineHelper.drawSelectionBox(
          //    multiBufferSource, poseStack, blockPos, camera, connection.getVoxelShape(), CONDUIT_SHAPE_COLOR);

          // We won't cancel the default here, allowing the normal ConduitBlockHighlighter to run.
          cancelDefault = false;
        }
      } else {
        cancelDefault = false;
      }
    }
    event.setCanceled(cancelDefault);
  }
}
