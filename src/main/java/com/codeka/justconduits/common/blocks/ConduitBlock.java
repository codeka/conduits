package com.codeka.justconduits.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
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

  private static final VoxelShape RENDER_SHAPE = Shapes.box(0.1, 0.1, 0.1, 0.9, 0.9, 0.9);

  public ConduitBlock() {
    super(Properties.of(Material.STONE, MaterialColor.STONE)
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
  public VoxelShape getOcclusionShape(@Nonnull BlockState state, @Nonnull BlockGetter reader, @Nonnull BlockPos pos) {
    return RENDER_SHAPE;
  }

  @Nonnull
  @Override
  public InteractionResult use(@Nonnull BlockState blockState, @Nonnull Level level, @Nonnull BlockPos blockPos,
                               @Nonnull Player player, @Nonnull InteractionHand hand,
                               @Nonnull BlockHitResult blockHitResult) {
    return InteractionResult.PASS;
  }
}
