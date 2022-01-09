package com.codeka.justconduits.client.blocks;

import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.capabilities.network.ConduitType;
import com.codeka.justconduits.helpers.QuadHelper;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
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
import org.lwjgl.system.CallbackI;

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

    ArrayList<ConduitType> conduitTypes = new ArrayList<>();
    List<String> conduitTypeNames = extraData.getData(ConduitModelProps.CONDUIT_TYPES);
    if (conduitTypeNames != null) {
      for (String conduitTypeName : conduitTypeNames) {
        conduitTypes.add(ConduitType.fromName(conduitTypeName));
      }
    }

    ArrayList<BakedQuad> quads = new ArrayList<>();
    for (int i = 0; i < conduitTypes.size(); i++) {
      ConduitType conduitType = conduitTypes.get(i);
      Transformation conduitTransform = getTransformationForConduit(i, conduitTypes.size());

      TextureAtlasSprite texture;
      if (conduitType == ConduitType.SIMPLE_ITEM) {
        texture = spriteGetter.apply(ConduitModelLoader.SIMPLE_ITEM_CONDUIT_MATERIAL);
      } else if (conduitType == ConduitType.SIMPLE_FLUID) {
        texture = spriteGetter.apply(ConduitModelLoader.SIMPLE_FLUID_CONDUIT_MATERIAL);
      } else {
        // Invalid conduit type (or at least, not yet supported)
        texture = spriteGetter.apply(ConduitModelLoader.MISSING_MATERIAL);
      }

      Transformation transformation = new Transformation(Matrix4f.createScaleMatrix(0.25f, 0.25f, 0.25f));
      quads.addAll(QuadHelper.createCube(conduitTransform.compose(transformation), texture));

      List<ConduitConnection> connections = extraData.getData(ConduitModelProps.CONNECTIONS);
      if (connections != null) {
        for (ConduitConnection conn : connections) {
          quads.addAll(QuadHelper.generateQuads(conn.getVoxelShape(), conduitTransform, texture));
        }
      }
    }

    return quads;
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

  private Transformation getTransformationForConduit(int index, int totalConduits) {
    if (totalConduits <= 1) {
      return Transformation.identity();
    }

    if (index == 0) {
      return new Transformation(Matrix4f.createTranslateMatrix(0.25f, 0.0f, 0.0f));
    } else if (index == 1) {
      return new Transformation(Matrix4f.createTranslateMatrix(-0.25f, 0.0f, 0.0f));
    } else {
      // TODO: others
      return Transformation.identity();
    }
  }
}
