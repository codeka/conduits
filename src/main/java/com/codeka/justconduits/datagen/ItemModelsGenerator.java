package com.codeka.justconduits.datagen;

import com.codeka.justconduits.JustConduitsMod;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModelsGenerator extends ItemModelProvider {
  public ItemModelsGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
    super(generator, JustConduitsMod.MODID, existingFileHelper);
  }

  private void buildTransformsForConduitItem(ModelBuilder<ItemModelBuilder>.TransformsBuilder transforms) {
    // These were just eyeballed to look OK.
    transforms
        .transform(ModelBuilder.Perspective.GUI).rotation(30, 135, 0).scale(0.425f).end()
        .transform(ModelBuilder.Perspective.GROUND).translation(0, 3, 0).scale(0.175f).end()
        .transform(ModelBuilder.Perspective.FIXED).scale(0.35f).end()
        .transform(ModelBuilder.Perspective.FIRSTPERSON_LEFT).scale(0.35f).end()
        .transform(ModelBuilder.Perspective.FIRSTPERSON_RIGHT).scale(0.35f).end()
        .transform(ModelBuilder.Perspective.THIRDPERSON_LEFT).scale(0.35f).end()
        .transform(ModelBuilder.Perspective.THIRDPERSON_RIGHT).scale(0.35f).end();
  }

  @Override
  protected void registerModels() {
    var simpleItemConduit = cubeAll("simple_item_conduit", modLoc("blocks/simple_item_conduit"));
    buildTransformsForConduitItem(simpleItemConduit.transforms());

    var itemConduit = cubeAll("item_conduit", modLoc("blocks/item_conduit"));
    buildTransformsForConduitItem(itemConduit.transforms());

    var simpleFluidConduit = cubeAll("simple_fluid_conduit", modLoc("blocks/simple_fluid_conduit"));
    buildTransformsForConduitItem(simpleFluidConduit.transforms());

    var simpleEnergyConduit = cubeAll("simple_energy_conduit", modLoc("blocks/simple_energy_conduit"));
    buildTransformsForConduitItem(simpleEnergyConduit.transforms());

    singleTexture("conduit_tool", mcLoc("item/generated"), "layer0", modLoc("items/conduit_tool"));
  }
}
