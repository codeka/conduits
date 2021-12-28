package com.codeka.justconduits.client.blocks;

import com.codeka.justconduits.JustConduitsMod;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ConduitModelLoader implements IModelLoader<ConduitModelLoader.Geometry> {
  public static final ResourceLocation ID = new ResourceLocation(JustConduitsMod.MODID, "conduit_loader");

  private static final ResourceLocation SIMPLE_CONDUIT_TEXTURE =
      new ResourceLocation(JustConduitsMod.MODID, "simple_item_conduit");

  static final Material SIMPLE_CONDUIT_MATERIAL = ForgeHooksClient.getBlockMaterial(SIMPLE_CONDUIT_TEXTURE);

  @Nonnull
  @Override
  public Geometry read(@Nonnull JsonDeserializationContext deserializationContext, @Nonnull JsonObject modelContents) {
    return new Geometry();
  }

  @Override
  public void onResourceManagerReload(@Nonnull ResourceManager resourceManager) {

  }

  public static class Geometry implements IModelGeometry<Geometry> {
    @Override
    public BakedModel bake(
        IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter,
        ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
      return new ConduitBakedModel(modelTransform, spriteGetter, overrides, owner.getCameraTransforms());
    }

    @Override
    public Collection<Material> getTextures(
        IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter,
        Set<Pair<String, String>> missingTextureErrors) {
      return List.of(SIMPLE_CONDUIT_MATERIAL);
    }
  }
}
