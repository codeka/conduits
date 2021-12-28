package com.codeka.justconduits.datagen;

import com.codeka.justconduits.JustConduitsMod;
import com.codeka.justconduits.common.ModBlocks;
import com.codeka.justconduits.common.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class LangGenerator extends LanguageProvider {
  public LangGenerator(DataGenerator gen, String locale) {
    super(gen, JustConduitsMod.MODID, locale);
  }

  @Override
  protected void addTranslations() {
    add(ModItems.SIMPLE_ITEM_CONDUIT.get(), "Simple Item Conduit");
    add(ModBlocks.CONDUIT.get(), "Conduit");
  }
}
