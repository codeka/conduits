package com.codeka.justconduits.common.blocks;

import com.codeka.justconduits.helpers.LineHelper;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.awt.Color;

/**
 * This class takes over highlighting the conduit block so that we can highlight the individual parts of the block
 * separately from each other (so that you know what will happen when you right-click, etc).
 */
public class ConduitBlockHighlighter {
  private static final Logger L = LogManager.getLogger();

  private static final VoxelShape CENTER = Shapes.box(0.375f, 0.375f, 0.375f, 0.625f, 0.625f, 0.625f);

  @SubscribeEvent(priority = EventPriority.LOW)
  public static void onDrawBlockHighlightEvent(DrawSelectionEvent.HighlightBlock event) {
    ConduitBlockEntity conduitBlockEntity = getConduitBlockEntity(event);
    if (conduitBlockEntity == null) {
      return;
    }

    final Vec3 startPos = event.getCamera().getPosition();
    final Vector3f lookVector = event.getCamera().getLookVector();
    final double pickRange = Minecraft.getInstance().gameMode.getPickRange();
    lookVector.mul((float) pickRange);
    final Vec3 endPos = startPos.add(lookVector.x(), lookVector.y(), lookVector.z());

    // Check the center piece.
    BlockHitResult centerHitResult = CENTER.clip(startPos, endPos, conduitBlockEntity.getBlockPos());
    if (centerHitResult != null) {
      LineHelper.drawSelectionBox(event.getMultiBufferSource(), event.getPoseStack(), conduitBlockEntity.getBlockPos(),
          event.getCamera(), CENTER, Color.CYAN);
      event.setCanceled(true);
      return;
    }

    // TODO: it should be the closest one that you click on.
    for (ConduitConnection connection : conduitBlockEntity.getConnections()) {
      BlockHitResult subHitResult = connection.getVoxelShape().clip(startPos, endPos, conduitBlockEntity.getBlockPos());
      if (subHitResult == null) {
        continue;
      }

      LineHelper.drawSelectionBox(event.getMultiBufferSource(), event.getPoseStack(), conduitBlockEntity.getBlockPos(),
          event.getCamera(), connection.getVoxelShape(), Color.CYAN);
      event.setCanceled(true);
      return;
    }
  }

  /**
   * Gets the {@link ConduitBlockEntity} that this selection event is highlighting. Returns null if you're not looking
   * at a {@link ConduitBlockEntity}.
   */
  @Nullable
  private static ConduitBlockEntity getConduitBlockEntity(DrawSelectionEvent.HighlightBlock event) {
    BlockHitResult blockHitResult = event.getTarget();
    if (blockHitResult.getType() != BlockHitResult.Type.BLOCK) {
      return null;
    }

    ClientLevel level = Minecraft.getInstance().level;
    if (level == null) {
      return null;
    }

    BlockPos blockPos = blockHitResult.getBlockPos();
    if (level.getBlockEntity(blockPos) instanceof ConduitBlockEntity conduitBlockEntity) {
      return conduitBlockEntity;
    }

    return null;
  }
}
