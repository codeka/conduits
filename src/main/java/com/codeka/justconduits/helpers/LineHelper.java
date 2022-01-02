package com.codeka.justconduits.helpers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.awt.Color;

public class LineHelper {

  public static void drawSelectionBox(MultiBufferSource multiBufferSource, PoseStack poseStack, BlockPos blockPos,
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
