package com.codeka.justconduits.common.blocks;

import com.codeka.justconduits.helpers.LineHelper;
import com.codeka.justconduits.helpers.SelectionHelper;
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

  @SubscribeEvent(priority = EventPriority.LOW)
  public static void onDrawBlockHighlightEvent(DrawSelectionEvent.HighlightBlock event) {
    ConduitBlockEntity conduitBlockEntity = getConduitBlockEntity(event);
    if (conduitBlockEntity == null) {
      return;
    }

    SelectionHelper.SelectionResult selection = SelectionHelper.raycast(conduitBlockEntity, event.getCamera());
    if (selection != null) {
      LineHelper.drawSelectionBox(event.getMultiBufferSource(), event.getPoseStack(), conduitBlockEntity.getBlockPos(),
          event.getCamera(), selection.shape(), Color.BLACK);
      event.setCanceled(true);
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
