package com.codeka.justconduits.helpers;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.shape.SelectionShape;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Helper for performing sub-block selection and highlighting of the selection.
 */
public class SelectionHelper {
  private static final Logger L = LogManager.getLogger();

  public record SelectionResult(ConduitConnection connection,
                                BlockHitResult subHitResult,
                                VoxelShape shape) {
  }

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
    SelectionResult closestResult = null;
    double closestDistance = Double.POSITIVE_INFINITY;

    for (SelectionShape.Shape shape : conduitBlockEntity.getShapeManager().getSelectionShape().getShapes()) {
      BlockHitResult subHitResult = shape.getVoxelShape().clip(startPos, endPos, conduitBlockEntity.getBlockPos());
      if (subHitResult == null) {
        continue;
      }

      // We'll go through them all and choose the one that hit closest to the startPos. That'll be the one "in front"
      // and the one that the player is actually looking at.
      double distanceToEye = subHitResult.getLocation().distanceTo(startPos);
      if (distanceToEye < closestDistance) {
        closestDistance = distanceToEye;
        closestResult = new SelectionResult(shape.getConnection(), subHitResult, shape.getVoxelShape());
      } else {
        L.atInfo().log("  not replacing");
      }
    }

    return closestResult;
  }
}
