package com.codeka.justconduits.datagen;

import com.codeka.justconduits.JustConduitsMod;
import com.codeka.justconduits.client.gui.conduittool.ConduitToolScreen;
import com.codeka.justconduits.common.ModBlocks;
import com.codeka.justconduits.common.ModItems;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.items.ConduitTool;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class LangGenerator extends LanguageProvider {
  public LangGenerator(DataGenerator gen, String locale) {
    super(gen, JustConduitsMod.MODID, locale);
  }

  @Override
  protected void addTranslations() {
    add(ModItems.CONDUIT_TOOL.get(), "Conduit Tool");
    add(ModItems.SIMPLE_ITEM_CONDUIT.get(), "Simple Item Conduit");
    add(ModItems.ITEM_CONDUIT.get(), "Item Conduit");
    add(ModItems.SIMPLE_FLUID_CONDUIT.get(), "Simple Fluid Conduit");
    add(ModItems.SIMPLE_ENERGY_CONDUIT.get(), "Simple Energy Conduit");
    add(ModBlocks.CONDUIT.get(), "Conduit");

    add(ConduitBlockEntity.SCREEN_CONDUIT_CONNECTION, "Conduit Connection");
    add(ConduitTool.SCREEN_CONDUIT_TOOL, "Conduit Tool");

    add(ConduitToolScreen.NETWORK_INFO_STRING, "ID:%1$s");
  }
}
