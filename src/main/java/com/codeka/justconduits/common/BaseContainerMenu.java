package com.codeka.justconduits.common;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BaseContainerMenu extends AbstractContainerMenu {
  private static final int SLOT_SIZE = 18;

  private final IItemHandler playerInventory;

  public BaseContainerMenu(@Nullable MenuType<? extends BaseContainerMenu> menuType, int containerId, Inventory inventory) {
    super(menuType, containerId);

    this.playerInventory = new InvWrapper(inventory);
  }

  protected void layoutPlayerInventorySlots(int x, int y) {
    addPlayerInventory(playerInventory, x, y);

    // The hotbar is just another inventory row, the first 9 slots in the inventory.
    y += 58;
    addInventoryRow(playerInventory, 0, x, y);
  }

  protected boolean isStillValid(@Nonnull Player player, @Nullable BlockEntity blockEntity) {
    if (blockEntity == null) {
      return false;
    }

    Level level = blockEntity.getLevel();
    if (level == null) {
      return false;
    }
    return stillValid(
        ContainerLevelAccess.create(
            level, blockEntity.getBlockPos()), player, ModBlocks.CONDUIT.get());
  }

  private int addInventoryRow(IItemHandler handler, int index, int x, int y) {
    for (int i = 0 ; i < 9 ; i++) {
      addSlot(new SlotItemHandler(handler, index, x, y));
      x += SLOT_SIZE;
      index++;
    }
    return index;
  }

  private void addPlayerInventory(IItemHandler handler, int x, int y) {
    // Start on index 9, index 0 is the hotbar.
    int index = 9;
    for (int j = 0 ; j < 3 ; j++) {
      index = addInventoryRow(handler, index, x, y);
      y += SLOT_SIZE;
    }
  }

}
