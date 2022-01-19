package com.codeka.justconduits.common.items;

import com.codeka.justconduits.common.BaseContainerMenu;
import com.codeka.justconduits.common.ModContainers;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConduitToolContainerMenu extends BaseContainerMenu {
  private static final Logger L = LogManager.getLogger();

  @Nullable // Shouldn't be null, except if the block is destroyed or something before we show up.
  private final ConduitBlockEntity conduitBlockEntity;
  private final Player player;
  private final IItemHandler playerInventory;

  private final BlockPos blockPos;

  public ConduitToolContainerMenu(int containerId, @Nonnull Inventory playerInventory, @Nonnull Player player,
                                  @Nonnull MenuExtras extras) {
    super(ModContainers.CONDUIT_TOOL_CONTAINER_MENU.get(), containerId, playerInventory);

    this.blockPos = extras.blockPos;
    if (player.getCommandSenderWorld().getBlockEntity(blockPos) instanceof ConduitBlockEntity conduitBlockEntity) {
      this.conduitBlockEntity = conduitBlockEntity;
    } else {
      conduitBlockEntity = null;
    }
    this.player = player;
    this.playerInventory = new InvWrapper(playerInventory);
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

    public MenuExtras(ConduitBlockEntity conduitBlockEntity) {
      blockPos = conduitBlockEntity.getBlockPos();
    }

    private MenuExtras(BlockPos blockPos) {
      this.blockPos = blockPos;
    }

    @Override
    public void accept(FriendlyByteBuf buffer) {
      buffer.writeBlockPos(blockPos);
    }

    public static MenuExtras create(FriendlyByteBuf buffer) {
      BlockPos blockPos = buffer.readBlockPos();
      return new MenuExtras(blockPos);
    }
  }
}
