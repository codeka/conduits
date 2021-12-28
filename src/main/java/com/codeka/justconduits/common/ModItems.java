package com.codeka.justconduits.common;

import com.codeka.justconduits.JustConduitsMod;
import com.codeka.justconduits.common.items.SimpleItemConduitItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
  private static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(ForgeRegistries.ITEMS, JustConduitsMod.MODID);

  public static final RegistryObject<Item> SIMPLE_ITEM_CONDUIT =
      ITEMS.register("simple_item_conduit", SimpleItemConduitItem::new);

  public static void register() {
    ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
  }
}
