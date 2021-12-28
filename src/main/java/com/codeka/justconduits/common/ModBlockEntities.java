package com.codeka.justconduits.common;

import com.codeka.justconduits.JustConduitsMod;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
  private static final DeferredRegister<BlockEntityType<?>> REGISTER =
      DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, JustConduitsMod.MODID);

  public static RegistryObject<BlockEntityType<ConduitBlockEntity>> CONDUIT =
      REGISTER.register(
          "conduit",
          () -> BlockEntityType.Builder.of(ConduitBlockEntity::new, ModBlocks.CONDUIT.get()).build(null));

  public static void register() {
    REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
  }
}
