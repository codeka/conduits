package com.codeka.justconduits.common.blocks;

import com.codeka.justconduits.common.ModBlockEntities;
import com.codeka.justconduits.common.ModBlocks;
import com.codeka.justconduits.common.ModContainers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConduitContainerMenu extends AbstractContainerMenu {
  @Nullable // Shouldn't be null, except if the block is destroyed or something before we show up.
  private final ConduitBlockEntity conduitBlockEntity;
  private Player player;

  public ConduitContainerMenu(int containerId, @Nonnull BlockPos blockPos, @Nonnull Inventory playerInventory,
                                 @Nonnull Player player) {
    super(ModContainers.CONDUIT_CONTAINER_MENU.get(), containerId);

    if (player.getCommandSenderWorld().getBlockEntity(blockPos) instanceof ConduitBlockEntity conduitBlockEntity) {
      this.conduitBlockEntity = conduitBlockEntity;
    } else {
      conduitBlockEntity = null;
    }
    this.player = player;
  }

  @Override
  public boolean stillValid(@Nonnull Player player) {
    if (conduitBlockEntity == null) {
      return false;
    }

    return stillValid(
        ContainerLevelAccess.create(
            conduitBlockEntity.getLevel(), conduitBlockEntity.getBlockPos()), player, ModBlocks.CONDUIT.get());
  }
}
