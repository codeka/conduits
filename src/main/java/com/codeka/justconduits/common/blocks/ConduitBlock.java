package com.codeka.justconduits.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * All conduits are represented by a {@link ConduitBlock} when placed in the world. Each block can be made up of up
 * to 9 different "wires". We then use a custom renderer to actually render the conduit so you can tell what wires are
 * running through it.
 */
public class ConduitBlock extends Block implements EntityBlock {
  private static final Logger L = LogManager.getLogger();

  public ConduitBlock() {
    super(Properties.of(Material.STONE, MaterialColor.STONE)
        .noOcclusion()
        .isSuffocating((blockState, level, blockPos) -> false)
        .isValidSpawn((blockState, level, blockPos, entityType) -> false)
        .isRedstoneConductor((blockState, level, blockPos) -> false)
        .isViewBlocking((blockState, level, blockPos) -> false)
        .strength(0.6f, 6.0f));
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(@Nonnull BlockPos blockPos, @Nonnull BlockState blockState) {
    return new ConduitBlockEntity(blockPos, blockState);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void neighborChanged(@Nonnull BlockState blockState, @Nonnull Level level, @Nonnull BlockPos blockPos,
                              @Nonnull Block neighborBlock, @Nonnull BlockPos neighborBlockPos, boolean isMoving) {
    super.neighborChanged(blockState, level, blockPos, neighborBlock, neighborBlockPos, isMoving);

    // Update our block entity.
    if (level.getBlockEntity(blockPos) instanceof ConduitBlockEntity conduitBlockEntity) {
      conduitBlockEntity.onNeighborChanged(blockState, neighborBlockPos);
    }
  }

  @Override
  public void playerDestroy(
      @Nonnull Level level, @Nonnull Player player, @Nonnull BlockPos pos, @Nonnull BlockState blockState,
      @Nullable BlockEntity blockEntity, @Nonnull ItemStack tool) {
    // TODO: if the block is destroyed, you probably did e.g. TNT or something. We'll want to drop all our conduits.
    L.atInfo().log("playerDestroy");
    super.playerDestroy(level, player, pos, blockState, blockEntity, tool);
  }

  // TODO: why is there neighborChanged and onNeighborChanged? They both seem to do similar, but slightly different
  //   things.
  @Override
  public void onNeighborChange(
      BlockState blockState, LevelReader level, BlockPos blockPos, BlockPos neighborBlockPos) {
    super.onNeighborChange(blockState, level, blockPos, neighborBlockPos);

    // Update our block entity as well.
    if (level.getBlockEntity(blockPos) instanceof ConduitBlockEntity conduitBlockEntity) {
      conduitBlockEntity.onNeighborChanged(blockState, neighborBlockPos);
    }
  }

  @Nullable
  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @Nonnull BlockState state,
                                                                @Nonnull BlockEntityType<T> type) {
    if (!level.isClientSide()) {
      return (lvl, pos, stt, te) -> {
        if (te instanceof ConduitBlockEntity conduitBlockEntity) conduitBlockEntity.tickServer();
      };
    }
    return null;
  }

  @SuppressWarnings("deprecation")
  @Nonnull
  @Override
  public VoxelShape getShape(@Nonnull BlockState blockState, @Nonnull BlockGetter reader, @Nonnull BlockPos blockPos,
                             @Nonnull CollisionContext context) {
    if (reader.getBlockEntity(blockPos) instanceof ConduitBlockEntity conduitBlockEntity) {
      return conduitBlockEntity.getShapeManager().getCollisionShape();
    }

    return super.getShape(blockState, reader, blockPos, context);
  }

  @SuppressWarnings("deprecation")
  @Nonnull
  @Override
  public InteractionResult use(@Nonnull BlockState blockState, @Nonnull Level level, @Nonnull BlockPos blockPos,
                               @Nonnull Player player, @Nonnull InteractionHand hand,
                               @Nonnull BlockHitResult blockHitResult) {
    if (level.getBlockEntity(blockPos) instanceof ConduitBlockEntity conduitBlockEntity) {
      return conduitBlockEntity.use(player, hand, blockHitResult, level.isClientSide);
    }

    return InteractionResult.PASS;
  }
}
