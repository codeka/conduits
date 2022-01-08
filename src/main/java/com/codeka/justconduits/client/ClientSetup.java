package com.codeka.justconduits.client;

import com.codeka.justconduits.JustConduitsMod;
import com.codeka.justconduits.client.blocks.ConduitModelLoader;
import com.codeka.justconduits.client.gui.ConduitScreen;
import com.codeka.justconduits.common.ModBlocks;
import com.codeka.justconduits.common.ModContainers;
import com.codeka.justconduits.common.blocks.ConduitBlockHighlighter;
import com.codeka.justconduits.debug.DebugVoxelShapeHighlighter;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = JustConduitsMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
  public static void init(FMLClientSetupEvent event) {
    event.enqueueWork(() -> {
      MenuScreens.register(ModContainers.CONDUIT_CONTAINER_MENU.get(), ConduitScreen::new);
      ItemBlockRenderTypes.setRenderLayer(ModBlocks.CONDUIT.get(), RenderType.translucent());
    });

    MinecraftForge.EVENT_BUS.register(DebugVoxelShapeHighlighter.class);
    MinecraftForge.EVENT_BUS.register(ConduitBlockHighlighter.class);
  }

  @SubscribeEvent
  public static void onModelRegistryEvent(ModelRegistryEvent event) {
    ModelLoaderRegistry.registerLoader(ConduitModelLoader.ID, new ConduitModelLoader());
  }
}
