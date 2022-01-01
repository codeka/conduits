package com.codeka.justconduits.debug;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;

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
  static boolean drawConduitShapes = true; // TODO: turn off

  @SubscribeEvent
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
      drawSelectionBox(multiBufferSource, poseStack, blockPos, camera, shape, SHAPE_COLOR);
    }
    if (drawVisualShape) {
      VoxelShape shape = blockstate.getVisualShape(level, blockPos, collisionContext);
      drawSelectionBox(multiBufferSource, poseStack, blockPos, camera, shape, VISUAL_SHAPE_COLOR);
    }
    if (drawCollisionShape) {
      VoxelShape shape = blockstate.getCollisionShape(level, blockPos, collisionContext);
      drawSelectionBox(multiBufferSource, poseStack, blockPos, camera, shape, COLLISION_SHAPE_COLOR);
    }
    if (drawConduitShapes) {
      if (level.getBlockEntity(blockPos) instanceof ConduitBlockEntity conduitBlockEntity) {
        for (ConduitConnection connection : conduitBlockEntity.getConnections()) {
          drawSelectionBox(
              multiBufferSource, poseStack, blockPos, camera, connection.getVoxelShape(), CONDUIT_SHAPE_COLOR);
        }
      } else {
        cancelDefault = false;
      }
    }
    event.setCanceled(cancelDefault);
  }

  private static void drawSelectionBox(MultiBufferSource multiBufferSource, PoseStack poseStack, BlockPos blockPos,
                                       Camera camera, VoxelShape shape, Color color) {
    VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());

    double eyeX = camera.getPosition().x;
    double eyeY = camera.getPosition().y;
    double eyeZ = camera.getPosition().z;
    drawShapeOutline(poseStack, vertexConsumer, shape,
        blockPos.getX() - eyeX, blockPos.getY() - eyeY, blockPos.getZ() - eyeZ,
        color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);

  }

  private static void drawShapeOutline(PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape voxelShape,
                                       double originX, double originY, double originZ, float red, float green,
                                       float blue) {

    Matrix4f matrix4f = poseStack.last().pose();
    voxelShape.forAllEdges((x0, y0, z0, x1, y1, z1) -> {
      vertexConsumer
          .vertex(matrix4f, (float)(x0 + originX), (float)(y0 + originY), (float)(z0 + originZ))
          .color(red, green, blue, 0.5f)
          .normal(0, 1, 0)
          .endVertex();
      vertexConsumer
          .vertex(matrix4f, (float)(x1 + originX), (float)(y1 + originY), (float)(z1 + originZ))
          .color(red, green, blue, 0.5f)
          .normal(0, 1, 0)
          .endVertex();
    });
  }
}
