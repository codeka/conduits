package com.codeka.justconduits.datagen;

import com.codeka.justconduits.JustConduitsMod;
import com.codeka.justconduits.common.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class BlockTagsGenerator extends BlockTagsProvider {
  public BlockTagsGenerator(DataGenerator pGenerator, @Nullable ExistingFileHelper existingFileHelper) {
    super(pGenerator, JustConduitsMod.MODID, existingFileHelper);
  }

  @Override
  protected void addTags() {
    tag(BlockTags.MINEABLE_WITH_PICKAXE)
        .add(ModBlocks.CONDUIT.get());
  }
}
