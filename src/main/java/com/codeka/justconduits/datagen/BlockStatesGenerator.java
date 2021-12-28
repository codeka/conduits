package com.codeka.justconduits.datagen;

import com.codeka.justconduits.JustConduitsMod;
import com.codeka.justconduits.client.blocks.ConduitModelLoader;
import com.codeka.justconduits.common.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockStatesGenerator extends BlockStateProvider {
  public BlockStatesGenerator(DataGenerator gen, ExistingFileHelper exFileHelper) {
    super(gen, JustConduitsMod.MODID, exFileHelper);
  }

  @Override
  protected void registerStatesAndModels() {
    registerConduit();
  }

  private void registerConduit() {
    BlockModelBuilder generatorModel =
        models().getBuilder(ModBlocks.CONDUIT.get().getRegistryName().getPath())
            .parent(models().getExistingFile(mcLoc("cube")))
            .customLoader(
                (builder, helper) ->
                    new CustomLoaderBuilder<BlockModelBuilder>(ConduitModelLoader.ID, builder, helper) {})
            .end();
    simpleBlock(ModBlocks.CONDUIT.get(), generatorModel);
  }
}
