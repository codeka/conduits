package com.codeka.justconduits.helpers;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nullable;

/**
 * Helper for performing sub-block selection and highlighting of the selection.
 */
public class SelectionHelper {
  public record SelectionResult(ConduitConnection connection,
                                BlockHitResult subHitResult,
                                VoxelShape shape) {
  }

  private static final VoxelShape CENTER = Shapes.box(0.375f, 0.375f, 0.375f, 0.625f, 0.625f, 0.625f);

  /**
   * Performs a raycast (what the game calls a "clip") to determine which connection (if any) the camera is looking at.
   *
   * @param conduitBlockEntity The {@link ConduitBlockEntity} we're raycasting against.
   * @param camera The {@link Camera} that is doing the looking.
   * @return A {@link SelectionResult} if you're looking at part of the conduit, or null if you're looking "through" the
   *         conduit.
   */
  @Nullable
  public static SelectionResult raycast(ConduitBlockEntity conduitBlockEntity, Camera camera) {
    final Vec3 startPos = camera.getPosition();
    final Vector3f lookVector = camera.getLookVector();
    final double pickRange = Minecraft.getInstance().gameMode.getPickRange();
    lookVector.mul((float) pickRange);
    final Vec3 endPos = startPos.add(lookVector.x(), lookVector.y(), lookVector.z());

    return raycast(conduitBlockEntity, startPos, endPos);
  }

  /**
   * Performs a raycast (what the game calls a "clip") to determine which connection (if any) the player is looking at.
   *
   * @param conduitBlockEntity The {@link ConduitBlockEntity} we're raycasting against.
   * @param player The {@link Player} that is doing the looking.
   * @return A {@link SelectionResult} if you're looking at part of the conduit, or null if you're looking "through" the
   *         conduit.
   */
  @Nullable
  public static SelectionResult raycast(ConduitBlockEntity conduitBlockEntity, Player player) {
    final Vec3 startPos = player.getEyePosition();
    Vec3 lookVector = player.getLookAngle();
    final double pickRange = player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
    final Vec3 endPos = startPos.add(lookVector.multiply(pickRange, pickRange, pickRange));

    return raycast(conduitBlockEntity, startPos, endPos);
  }

  @Nullable
  public static SelectionResult raycast(ConduitBlockEntity conduitBlockEntity, Vec3 startPos, Vec3 endPos) {
    // Check the piece in the center.
    BlockHitResult centerHitResult = CENTER.clip(startPos, endPos, conduitBlockEntity.getBlockPos());
    if (centerHitResult != null) {
      return new SelectionResult(/* connection = */ null, centerHitResult, CENTER);
    }

    // TODO: it should be the closest one that you click on.
    for (ConduitConnection connection : conduitBlockEntity.getConnections()) {
      BlockHitResult subHitResult = connection.getVoxelShape().clip(startPos, endPos, conduitBlockEntity.getBlockPos());
      if (subHitResult == null) {
        continue;
      }

      return new SelectionResult(connection, subHitResult, connection.getVoxelShape());
    }

    return null;
  }
}
