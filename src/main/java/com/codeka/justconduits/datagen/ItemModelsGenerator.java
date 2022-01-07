package com.codeka.justconduits.datagen;

import com.codeka.justconduits.JustConduitsMod;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModelsGenerator extends ItemModelProvider {
  public ItemModelsGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
    super(generator, JustConduitsMod.MODID, existingFileHelper);
  }

  @Override
  protected void registerModels() {
    cubeAll("simple_item_conduit", modLoc("blocks/simple_item_conduit"))
        // These were just eyeballed to look OK.
        .transforms()
            .transform(ModelBuilder.Perspective.GUI).rotation(30, 135, 0).scale(0.425f).end()
            .transform(ModelBuilder.Perspective.GROUND).translation(0, 3, 0).scale(0.175f).end()
            .transform(ModelBuilder.Perspective.FIXED).scale(0.35f).end()
            .transform(ModelBuilder.Perspective.FIRSTPERSON_LEFT).scale(0.35f).end()
            .transform(ModelBuilder.Perspective.FIRSTPERSON_RIGHT).scale(0.35f).end()
            .transform(ModelBuilder.Perspective.THIRDPERSON_LEFT).scale(0.35f).end()
            .transform(ModelBuilder.Perspective.THIRDPERSON_RIGHT).scale(0.35f).end();

    cubeAll("simple_fluid_conduit", modLoc("blocks/simple_fluid_conduit"))
        // These were just eyeballed to look OK.
        .transforms()
        .transform(ModelBuilder.Perspective.GUI).rotation(30, 135, 0).scale(0.425f).end()
        .transform(ModelBuilder.Perspective.GROUND).translation(0, 3, 0).scale(0.175f).end()
        .transform(ModelBuilder.Perspective.FIXED).scale(0.35f).end()
        .transform(ModelBuilder.Perspective.FIRSTPERSON_LEFT).scale(0.35f).end()
        .transform(ModelBuilder.Perspective.FIRSTPERSON_RIGHT).scale(0.35f).end()
        .transform(ModelBuilder.Perspective.THIRDPERSON_LEFT).scale(0.35f).end()
        .transform(ModelBuilder.Perspective.THIRDPERSON_RIGHT).scale(0.35f).end();
  }
}
