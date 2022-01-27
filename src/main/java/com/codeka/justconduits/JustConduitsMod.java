package com.codeka.justconduits;

import com.codeka.justconduits.client.ClientSetup;
import com.codeka.justconduits.client.blocks.ConduitBlockHighlighter;
import com.codeka.justconduits.common.ModBlockEntities;
import com.codeka.justconduits.common.ModBlocks;
import com.codeka.justconduits.common.ModContainers;
import com.codeka.justconduits.common.ModItems;
import com.codeka.justconduits.common.blocks.ConduitBlockBreakEventHandler;
import com.codeka.justconduits.debug.DebugCommands;
import com.codeka.justconduits.packets.JustConduitsPacketHandler;
import net.minecraft.commands.Commands;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

@Mod(JustConduitsMod.MODID)
public class JustConduitsMod {
  private static final Logger L = LogManager.getLogger();

  public static final String MODID = "justconduits";

  public JustConduitsMod() {
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

    MinecraftForge.EVENT_BUS.register(ConduitBlockBreakEventHandler.class);
    MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
    ModItems.register();
    ModBlocks.register();
    ModBlockEntities.register();
    ModContainers.register();
  }

  private void setup(final FMLCommonSetupEvent event) {
    JustConduitsPacketHandler.init();
  }

  private void registerCommands(final RegisterCommandsEvent event) {
    event.getDispatcher().register(
        Commands.literal("jc")
            .then(DebugCommands.register(event.getDispatcher())));
  }

  private void enqueueIMC(final InterModEnqueueEvent event) {
    // some example code to dispatch IMC to another mod
    InterModComms.sendTo("justconduits", "helloworld", () -> {
      L.info("Hello world from the MDK");
      return "Hello world";
    });
  }

  private void processIMC(final InterModProcessEvent event) {
    // some example code to receive and process InterModComms from other mods
    L.info("Got IMC {}", event.getIMCStream().
        map(m -> m.messageSupplier().get()).
        collect(Collectors.toList()));
  }
}
