package com.codeka.justconduits.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * All conduits are represented by a {@link ConduitBlock} when placed in the world. Each block can be made up of up
 * to 9 different "wires". We then use a custom renderer to actually render the conduit so you can tell what wires are
 * running through it.
 */
public class ConduitBlock extends Block implements EntityBlock {

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
  @Nonnull
  @Override
  public VoxelShape getOcclusionShape(@Nonnull BlockState state, @Nonnull BlockGetter reader, @Nonnull BlockPos pos) {
    return RENDER_SHAPE;
  }
}
