package com.codeka.justconduits.client.blocks;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.resources.model.Material;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * This is the baked model for our conduits. It is created by {@link ConduitModelLoader}.
 */
public class ConduitBakedModel implements IDynamicBakedModel {
  private static final Logger L = LogManager.getLogger();

  private final ModelState modelState;
  private final Function<Material, TextureAtlasSprite> spriteGetter;
  private final ItemOverrides overrides;
  private final ItemTransforms itemTransforms;

  public  ConduitBakedModel(ModelState modelState, Function<Material, TextureAtlasSprite> spriteGetter,
                            ItemOverrides overrides, ItemTransforms itemTransforms) {
    this.modelState = modelState;
    this.spriteGetter = spriteGetter;
    this.overrides = overrides;
    this.itemTransforms = itemTransforms;
  }

  @Nonnull
  @Override
  public List<BakedQuad> getQuads(
      @Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
    // We'll only render the whole side, and also make sure we're on the translucent layer.
    RenderType renderType = MinecraftForgeClient.getRenderType();
    if (side != null || (renderType != null && !renderType.equals(RenderType.translucent()))) {
      return Collections.emptyList();
    }

    ConduitBlockEntity conduitBlockEntity = extraData.getData(ConduitModelProps.CONDUIT_BLOCK_ENTITY);
    if (conduitBlockEntity == null) {
      return new ArrayList<>();
    }

    return conduitBlockEntity.getShapeManager().getBakedQuads(spriteGetter);
  }

  @Override
  public boolean useAmbientOcclusion() {
    return false;
  }

  @Override
  public boolean isGui3d() {
    return false;
  }

  @Override
  public boolean usesBlockLight() {
    return false;
  }

  @Override
  public boolean isCustomRenderer() {
    return false;
  }

  @Nonnull
  @Override
  public TextureAtlasSprite getParticleIcon() {
    return spriteGetter.apply(ConduitModelLoader.SIMPLE_ITEM_CONDUIT_MATERIAL);
  }

  @SuppressWarnings("deprecation")
  @Nonnull
  @Override
  public ItemTransforms getTransforms() {
    return itemTransforms;
  }

  @Nonnull
  @Override
  public ItemOverrides getOverrides() {
    return overrides;
  }
}
