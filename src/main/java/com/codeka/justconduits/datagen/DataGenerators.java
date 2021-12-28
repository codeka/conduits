package com.codeka.justconduits.datagen;

import com.codeka.justconduits.JustConduitsMod;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = JustConduitsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

  @SubscribeEvent
  public static void onGatherData(GatherDataEvent event) {
    DataGenerator generator = event.getGenerator();
    if (event.includeServer()) {
      BlockTagsGenerator blockTagsGenerator = new BlockTagsGenerator(generator, event.getExistingFileHelper());
      generator.addProvider(blockTagsGenerator);
    }
    if (event.includeClient()) {
      generator.addProvider(new BlockStatesGenerator(generator, event.getExistingFileHelper()));
      generator.addProvider(new ItemModelsGenerator(generator, event.getExistingFileHelper()));
      generator.addProvider(new LangGenerator(generator, "en_us"));
    }
  }
}
