package com.codeka.justconduits.common.items;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;

/** The {@link ConduitTool} item allows you to debug your conduit networks. */
public class ConduitTool extends Item {

  public static final String SCREEN_CONDUIT_TOOL = "screen.conduit_tool";


  public ConduitTool(Properties properties) {
    super(properties);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    final Level level = context.getLevel();
    final BlockPos blockPos = context.getClickedPos();

    if (level.getBlockEntity(blockPos) instanceof ConduitBlockEntity conduitBlockEntity
        && context.getPlayer() instanceof ServerPlayer serverPlayer) {
      ConduitToolContainerMenu.MenuExtras menuExtras = new ConduitToolContainerMenu.MenuExtras(conduitBlockEntity);
      MenuProvider menuProvider = new MenuProvider() {
        @Nonnull
        @Override
        public Component getDisplayName() {
          return new TranslatableComponent(SCREEN_CONDUIT_TOOL);
        }

        @Override
        public AbstractContainerMenu createMenu(int containerId, @Nonnull Inventory inventory, @Nonnull Player player) {
          return new ConduitToolContainerMenu(containerId, player.getInventory(), player, menuExtras);
        }
      };
      NetworkHooks.openGui(serverPlayer, menuProvider, menuExtras);

      return InteractionResult.SUCCESS;
    } else {
      return super.useOn(context);
    }
  }
}
