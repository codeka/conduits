package com.codeka.justconduits.common.blocks;

import com.codeka.justconduits.common.BaseContainerMenu;
import com.codeka.justconduits.common.ModBlocks;
import com.codeka.justconduits.common.ModContainers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConduitContainerMenu extends BaseContainerMenu {
  private static final Logger L = LogManager.getLogger();

  @Nullable // Shouldn't be null, except if the block is destroyed or something before we show up.
  private final ConduitBlockEntity conduitBlockEntity;
  private final Player player;
  private final IItemHandler playerInventory;

  private final BlockPos blockPos;
  private final Direction direction;

  public ConduitContainerMenu(int containerId, @Nonnull Inventory playerInventory, @Nonnull Player player,
                              @Nonnull MenuExtras extras) {
    super(ModContainers.CONDUIT_CONTAINER_MENU.get(), containerId, playerInventory);

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

  @Nonnull
  public ConduitBlockEntity getConduitBlockEntity() {
    return checkNotNull(conduitBlockEntity);
  }

  @Override
  public boolean stillValid(@Nonnull Player player) {
    return isStillValid(player, conduitBlockEntity);
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
}
