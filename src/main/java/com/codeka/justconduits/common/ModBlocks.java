package com.codeka.justconduits.common;

import com.codeka.justconduits.JustConduitsMod;
import com.codeka.justconduits.common.blocks.ConduitBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
  private static final DeferredRegister<Block> BLOCKS =
      DeferredRegister.create(ForgeRegistries.BLOCKS, JustConduitsMod.MODID);

  public static final RegistryObject<Block> CONDUIT =
      BLOCKS.register("conduit", ConduitBlock::new);

  public static void register() {
    BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
  }
}
