package com.codeka.justconduits.common.blocks;

import com.codeka.justconduits.common.ModBlocks;
import com.codeka.justconduits.common.ModContainers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ConduitContainerMenu extends AbstractContainerMenu {
  private static final Logger L = LogManager.getLogger();

  @Nullable // Shouldn't be null, except if the block is destroyed or something before we show up.
  private final ConduitBlockEntity conduitBlockEntity;
  private final Player player;
  private final IItemHandler playerInventory;

  private final BlockPos blockPos;
  private final Direction direction;

  public ConduitContainerMenu(int containerId, @Nonnull Inventory playerInventory, @Nonnull Player player,
                              @Nonnull MenuExtras extras) {
    super(ModContainers.CONDUIT_CONTAINER_MENU.get(), containerId);

    this.blockPos = extras.blockPos;
    if (player.getCommandSenderWorld().getBlockEntity(blockPos) instanceof ConduitBlockEntity conduitBlockEntity) {
      this.conduitBlockEntity = conduitBlockEntity;
    } else {
      conduitBlockEntity = null;
    }
    this.player = player;
    this.playerInventory = new InvWrapper(playerInventory);
    this.direction = extras.direction;

    layoutPlayerInventorySlots(10, 108);
  }

  @Nullable
  public ConduitConnection getConnection() {
    ConduitBlockEntity be = conduitBlockEntity;
    if (be == null) {
      return null;
    }

    return be.getConnection(direction);
  }

  @Nullable
  public ConduitBlockEntity getConduitBlockEntity() {
    return conduitBlockEntity;
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

  public static final class MenuExtras implements Consumer<FriendlyByteBuf> {
    private final BlockPos blockPos;
    private final Direction direction;

    public MenuExtras(ConduitBlockEntity conduitBlockEntity, ConduitConnection conn) {
      blockPos = conduitBlockEntity.getBlockPos();
      direction = conn.getDirection();
    }

    private MenuExtras(BlockPos blockPos, Direction dir) {
      this.blockPos = blockPos;
      this.direction = dir;
    }

    @Override
    public void accept(FriendlyByteBuf buffer) {
      buffer.writeBlockPos(blockPos);
      buffer.writeEnum(direction);
    }

    public static MenuExtras create(FriendlyByteBuf buffer) {
      BlockPos blockPos = buffer.readBlockPos();
      Direction dir = buffer.readEnum(Direction.class);
      return new MenuExtras(blockPos, dir);
    }
  }

  // TODO: these should probably be in a base class or a helper or something.

  private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
    for (int i = 0 ; i < amount ; i++) {
      addSlot(new SlotItemHandler(handler, index, x, y));
      x += dx;
      index++;
    }
    return index;
  }

  private int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
    for (int j = 0 ; j < verAmount ; j++) {
      index = addSlotRange(handler, index, x, y, horAmount, dx);
      y += dy;
    }
    return index;
  }

  private void layoutPlayerInventorySlots(int leftCol, int topRow) {
    // Player inventory
    addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

    // Hotbar
    topRow += 58;
    addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
  }
}
