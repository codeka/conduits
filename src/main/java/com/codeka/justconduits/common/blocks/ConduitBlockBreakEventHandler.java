package com.codeka.justconduits.common.blocks;

import com.codeka.justconduits.helpers.SelectionHelper;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ConduitBlockBreakEventHandler {
  @SubscribeEvent
  public static void onBlockBreak(BlockEvent.BreakEvent event) {
    if (event.getWorld().isClientSide()) {
      // Don't do anything client-side.
      return;
    }

    if (event.getWorld().getBlockEntity(event.getPos()) instanceof ConduitBlockEntity conduitBlockEntity) {
      SelectionHelper.SelectionResult selection = SelectionHelper.raycast(conduitBlockEntity, event.getPlayer());
      if (selection != null && selection.conduitType() != null) {
        // You were looking at a conduit, so instead of breaking the whole block, just remove that conduit.
        conduitBlockEntity.removeConduit(selection.conduitType(), !event.getPlayer().isCreative());

        // If there's still conduits in the block, cancel the event. Otherwise just let it destroy us.
        if (!conduitBlockEntity.getConduitTypes().isEmpty()) {
          event.setCanceled(true);
        }
      }
    }
  }
}
