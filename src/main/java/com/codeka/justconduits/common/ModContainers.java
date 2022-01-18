package com.codeka.justconduits.common;

import com.codeka.justconduits.JustConduitsMod;
import com.codeka.justconduits.common.blocks.ConduitContainerMenu;
import com.codeka.justconduits.common.items.ConduitToolContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModContainers {
  private static final DeferredRegister<MenuType<?>> CONTAINERS =
      DeferredRegister.create(ForgeRegistries.CONTAINERS, JustConduitsMod.MODID);

  public static final RegistryObject<MenuType<ConduitContainerMenu>> CONDUIT_CONTAINER_MENU =
      CONTAINERS.register("conduit", () -> IForgeMenuType.create(
          (windowId, inv, data) ->
              new ConduitContainerMenu(
                  windowId, inv, inv.player, ConduitContainerMenu.MenuExtras.create(data))));

  public static final RegistryObject<MenuType<ConduitToolContainerMenu>> CONDUIT_TOOL_CONTAINER_MENU =
      CONTAINERS.register("conduit_tool", () -> IForgeMenuType.create(
          (windowId, inv, data) ->
              new ConduitToolContainerMenu(
                  windowId, inv, inv.player, ConduitToolContainerMenu.MenuExtras.create(data))));

  public static void register() {
    CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
  }
}
