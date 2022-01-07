package com.codeka.justconduits.common.items;

import com.codeka.justconduits.common.ModBlocks;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.capabilities.network.ConduitType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.minecraft.world.item.BlockItem.getBlockEntityData;

/** Base class for conduit items. */
public abstract class BaseConduitItem extends Item {
  private static final Logger L = LogManager.getLogger();

  public BaseConduitItem(Properties props) {
    super(props);
  }

  protected abstract ConduitType getConduitType();

  @Nonnull
  @Override
  public InteractionResult useOn(@Nonnull UseOnContext useOnContext) {
    // TODO: handle using a conduit on a block that already has a conduit?
    BlockPlaceContext context = new BlockPlaceContext(useOnContext);
    if (!context.canPlace()) {
      return InteractionResult.FAIL;
    } else {
      // TODO: the block should be given to us somehow.
      Block block = ModBlocks.CONDUIT.get();

      BlockState blockState = block.getStateForPlacement(context);
      if (blockState == null) {
        return InteractionResult.FAIL;
      }

      if (!context.getLevel().setBlock(context.getClickedPos(), blockState, /* TODO: what is 11? */ 11)) {
        return InteractionResult.FAIL;
      }
      final BlockPos blockPos = context.getClickedPos();
      final Level level = context.getLevel();
      final Player player = context.getPlayer();
      final ItemStack itemStack = context.getItemInHand();

      BlockState placedBlockState = level.getBlockState(blockPos);
      if (placedBlockState.is(blockState.getBlock())) {
        placedBlockState = updateBlockStateFromTag(blockPos, level, itemStack, placedBlockState);
        updateCustomBlockEntityTag(blockPos, level, player, itemStack);
        placedBlockState.getBlock().setPlacedBy(level, blockPos, placedBlockState, player, itemStack);
        if (player instanceof ServerPlayer) {
          CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, blockPos, itemStack);
        }
      }

      if (level.getBlockEntity(blockPos) instanceof ConduitBlockEntity placedBlockEntity) {
        placedBlockEntity.addConduit(getConduitType());
      }

      level.gameEvent(player, GameEvent.BLOCK_PLACE, blockPos);
      SoundType soundType = placedBlockState.getSoundType(level, blockPos, context.getPlayer());
      level.playSound(
          player, blockPos, soundType.getPlaceSound(), SoundSource.BLOCKS,
          (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f);
      if (player == null || !player.getAbilities().instabuild) {
        itemStack.shrink(1);
      }

      return InteractionResult.sidedSuccess(level.isClientSide);
    }
  }

  // TODO: is this needed?
  private BlockState updateBlockStateFromTag(
      BlockPos blockPos, Level level, ItemStack itemStack, BlockState existingBlockState) {
    BlockState blockState = existingBlockState;
    CompoundTag compoundTag = itemStack.getTag();
    if (compoundTag != null) {
      CompoundTag blockStateTag = compoundTag.getCompound("BlockStateTag");
      StateDefinition<Block, BlockState> stateDefinition = existingBlockState.getBlock().getStateDefinition();

      for(String s : blockStateTag.getAllKeys()) {
        Property<?> property = stateDefinition.getProperty(s);
        if (property != null) {
          String s1 = blockStateTag.get(s).getAsString();
          blockState = updateState(blockState, property, s1);
        }
      }
    }

    if (blockState != existingBlockState) {
      level.setBlock(blockPos, blockState, 2);
    }

    return blockState;
  }

  private static <T extends Comparable<T>> BlockState updateState(BlockState blockState, Property<T> property, String value) {
    return property.getValue(value).map((val) -> blockState.setValue(property, val)).orElse(blockState);
  }

  // TODO: is this needed?
  public static boolean updateCustomBlockEntityTag(BlockPos blockPos, Level level, @Nullable Player player, ItemStack itemStack) {
    MinecraftServer server = level.getServer();
    if (server != null) {
      CompoundTag compoundtag = getBlockEntityData(itemStack);
      if (compoundtag != null) {
        BlockEntity blockentity = level.getBlockEntity(blockPos);
        if (blockentity != null) {
          if (!level.isClientSide && blockentity.onlyOpCanSetNbt() && (player == null || !player.canUseGameMasterBlocks())) {
            return false;
          }

          CompoundTag compoundtag1 = blockentity.saveWithoutMetadata();
          CompoundTag compoundtag2 = compoundtag1.copy();
          compoundtag1.merge(compoundtag);
          if (!compoundtag1.equals(compoundtag2)) {
            blockentity.load(compoundtag1);
            blockentity.setChanged();
            return true;
          }
        }
      }
    }

    return false;
  }
}
