package com.codeka.justconduits.common;

import com.codeka.justconduits.JustConduitsMod;
import com.codeka.justconduits.common.capabilities.network.ConduitType;
import com.codeka.justconduits.common.items.ConduitItem;
import com.codeka.justconduits.common.items.ConduitTool;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
  private static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(ForgeRegistries.ITEMS, JustConduitsMod.MODID);

  public static final RegistryObject<Item> CONDUIT_TOOL =
      ITEMS.register(
          "conduit_tool",
          () -> new ConduitTool(new Item.Properties().tab(ConduitCreativeModeTab.TAB_CONDUIT)));

  public static final RegistryObject<Item> SIMPLE_ITEM_CONDUIT =
      ITEMS.register(
          "simple_item_conduit",
          () -> new ConduitItem(
              ConduitType.SIMPLE_ITEM, new Item.Properties().tab(ConduitCreativeModeTab.TAB_CONDUIT)));

  public static final RegistryObject<Item> SIMPLE_FLUID_CONDUIT =
      ITEMS.register(
          "simple_fluid_conduit",
          () -> new ConduitItem(
              ConduitType.SIMPLE_FLUID, new Item.Properties().tab(ConduitCreativeModeTab.TAB_CONDUIT)));

  public static final RegistryObject<Item> SIMPLE_ENERGY_CONDUIT =
      ITEMS.register(
          "simple_energy_conduit",
          () -> new ConduitItem(
              ConduitType.SIMPLE_ENERGY, new Item.Properties().tab(ConduitCreativeModeTab.TAB_CONDUIT)));

  public static void register() {
    ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
  }
}
