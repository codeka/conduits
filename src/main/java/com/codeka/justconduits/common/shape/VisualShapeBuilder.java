package com.codeka.justconduits.common.shape;

import com.codeka.justconduits.JustConduitsMod;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.impl.ConduitType;
import com.codeka.justconduits.helpers.QuadHelper;
import com.google.common.collect.ImmutableMap;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class VisualShapeBuilder {

  private static final ResourceLocation SIMPLE_ITEM_CONDUIT_TEXTURE =
      new ResourceLocation(JustConduitsMod.MODID, "blocks/simple_item_conduit");
  private static final ResourceLocation ITEM_CONDUIT_TEXTURE =
      new ResourceLocation(JustConduitsMod.MODID, "blocks/item_conduit");
  private static final ResourceLocation SIMPLE_FLUID_CONDUIT_TEXTURE =
      new ResourceLocation(JustConduitsMod.MODID, "blocks/simple_fluid_conduit");
  private static final ResourceLocation SIMPLE_ENERGY_CONDUIT_TEXTURE =
      new ResourceLocation(JustConduitsMod.MODID, "blocks/simple_energy_conduit");

  private static final ResourceLocation CONDUIT_PIPE_TEXTURE =
      new ResourceLocation(JustConduitsMod.MODID, "blocks/conduit_pipe");

  private static final ResourceLocation CONNECTOR_FRONT =
      new ResourceLocation(JustConduitsMod.MODID, "blocks/conduit_connector_front");
  private static final ResourceLocation CONNECTOR_BACK =
      new ResourceLocation(JustConduitsMod.MODID, "blocks/conduit_connector_back");
  private static final ResourceLocation CONNECTOR_SIDE =
      new ResourceLocation(JustConduitsMod.MODID, "blocks/conduit_connector_side");

  // A missing material for if we make a mistake with our coding and miss a texture.
  public static final Material MISSING_MATERIAL =
      ForgeHooksClient.getBlockMaterial(new ResourceLocation(JustConduitsMod.MODID, "error"));
  public static final Material CONDUIT_PIPE_MATERIAL = ForgeHooksClient.getBlockMaterial(CONDUIT_PIPE_TEXTURE);
  public static final Material CONNECTOR_FRONT_MATERIAL = ForgeHooksClient.getBlockMaterial(CONNECTOR_FRONT);
  public static final Material CONNECTOR_BACK_MATERIAL = ForgeHooksClient.getBlockMaterial(CONNECTOR_BACK);
  public static final Material CONNECTOR_SIDE_MATERIAL = ForgeHooksClient.getBlockMaterial(CONNECTOR_SIDE);
  public static final Material ITEM_CONDUIT_MATERIAL =
      ForgeHooksClient.getBlockMaterial(ITEM_CONDUIT_TEXTURE);
  public static final Material SIMPLE_ITEM_CONDUIT_MATERIAL =
      ForgeHooksClient.getBlockMaterial(SIMPLE_ITEM_CONDUIT_TEXTURE);
  public static final Material SIMPLE_FLUID_CONDUIT_MATERIAL =
      ForgeHooksClient.getBlockMaterial(SIMPLE_FLUID_CONDUIT_TEXTURE);
  public static final Material SIMPLE_ENERGY_CONDUIT_MATERIAL =
      ForgeHooksClient.getBlockMaterial(SIMPLE_ENERGY_CONDUIT_TEXTURE);

  public static List<Material> getTextures() {
    return List.of(SIMPLE_ITEM_CONDUIT_MATERIAL, ITEM_CONDUIT_MATERIAL, SIMPLE_FLUID_CONDUIT_MATERIAL,
        SIMPLE_ENERGY_CONDUIT_MATERIAL, CONDUIT_PIPE_MATERIAL, CONNECTOR_BACK_MATERIAL, CONNECTOR_FRONT_MATERIAL,
        CONNECTOR_SIDE_MATERIAL);
  }

  public static VisualShape createVisualShape(ConduitShape mainShape) {
    VisualShape visualShape = new VisualShape();
    for (var entry : mainShape.getShapes().entrySet()) {
      ConduitType conduitType = entry.getKey();
      ConduitShape.SingleConduitShape conduitShape = entry.getValue();

      // TODO: make this generic.
      Material material = MISSING_MATERIAL;
      if (conduitType == ConduitType.SIMPLE_ITEM) {
        material = SIMPLE_ITEM_CONDUIT_MATERIAL;
      } else if (conduitType == ConduitType.REGULAR_ITEM) {
        material = ITEM_CONDUIT_MATERIAL;
      } else if (conduitType == ConduitType.SIMPLE_FLUID) {
        material = SIMPLE_FLUID_CONDUIT_MATERIAL;
      } else if (conduitType == ConduitType.SIMPLE_ENERGY) {
        material = SIMPLE_ENERGY_CONDUIT_MATERIAL;
      }

      final Vec3 c = conduitShape.getCenter();
      if (conduitShape.needComplexConduit()) {
        // The complex conduit shape always uses the SIMPLE_ITEM material (TODO: use a custom material)
        visualShape.addBox(
            new VisualShape.Box(
                new Vector3f(
                    (float) conduitShape.getMin().x() - 0.125f,
                    (float) conduitShape.getMin().y() - 0.125f,
                    (float) conduitShape.getMin().z() - 0.125f),
                new Vector3f(
                    (float) conduitShape.getMax().x() + 0.125f,
                    (float) conduitShape.getMax().y() + 0.125f,
                    (float) conduitShape.getMax().z() + 0.125f),
                SIMPLE_ITEM_CONDUIT_MATERIAL));
      } else {
        visualShape.addBox(
            new VisualShape.Box(
                new Vector3f((float) c.x - 0.125f, (float) c.y - 0.125f, (float) c.z - 0.125f),
                new Vector3f((float) c.x + 0.125f, (float) c.y + 0.125f, (float) c.z + 0.125f),
                material));
      }

      for (ConduitShape.ConduitConnectionShape shape : conduitShape.getConnectionShapes().values()) {
        // We only do the visual for the positive axis direction, the conduit in the negative direction will draw
        // the other connection for us. Except for external connections, which don't have a conduit in the negative
        // direction.
        if (shape.direction().getAxisDirection() != Direction.AxisDirection.POSITIVE
            && shape.connectionType() != ConduitConnection.ConnectionType.EXTERNAL) {
          continue;
        }

        Vector3f normal = shape.direction().step();
        float length = (float) shape.length();
        Vector3f plane =
            new Vector3f(1.0f - Math.abs(normal.x()), 1.0f - Math.abs(normal.y()), 1.0f - Math.abs(normal.z()));
        Vector3f min = new Vector3f(
            (float) c.x + normal.x() * 0.125f - plane.x() * 0.09375f,
            (float) c.y + normal.y() * 0.125f - plane.y() * 0.09375f,
            (float) c.z + normal.z() * 0.125f - plane.z() * 0.09375f);
        Vector3f max = new Vector3f(
            (float) c.x + normal.x() * (length - 0.125f) + plane.x() * 0.09375f,
            (float) c.y + normal.y() * (length - 0.125f) + plane.y() * 0.09375f,
            (float) c.z + normal.z() * (length - 0.125f) + plane.z() * 0.09375f);
        if (shape.direction().getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
          // If the normal is negative (which can be the case for external connections), then min & max will be swapped.
          // So wap them back again.
          Vector3f tmp = min;
          min = max;
          max = tmp;
        }
        boolean rotateUv = shape.direction() == Direction.NORTH || shape.direction() == Direction.SOUTH;
        visualShape.addInternalConnection(
            new VisualShape.InternalConnection(min, max, rotateUv, CONDUIT_PIPE_MATERIAL));
      }
    }

    for (ConduitShape.ExternalConnectionShape externalConnectionShape : mainShape.getExternalConnectionShapes()) {
      final Vector3f min =
          new Vector3f(
              externalConnectionShape.min().x(), externalConnectionShape.min().y(), externalConnectionShape.min().z());
      min.add(
          0.0625f * (1.0f - Math.abs(externalConnectionShape.connection().getDirection().getStepX())),
          0.0625f * (1.0f - Math.abs(externalConnectionShape.connection().getDirection().getStepY())),
          0.0625f * (1.0f - Math.abs(externalConnectionShape.connection().getDirection().getStepZ())));
      final Vector3f max =
          new Vector3f(
              externalConnectionShape.max().x(), externalConnectionShape.max().y(), externalConnectionShape.max().z());
      max.add(
          -0.0625f * (1.0f - Math.abs(externalConnectionShape.connection().getDirection().getStepX())),
          -0.0625f * (1.0f - Math.abs(externalConnectionShape.connection().getDirection().getStepY())),
          -0.0625f * (1.0f - Math.abs(externalConnectionShape.connection().getDirection().getStepZ())));
      visualShape.addExternalConnection(
          new VisualShape.ExternalConnection(
              min, max,
              externalConnectionShape.connection().getDirection() == Direction.EAST ||
                  externalConnectionShape.connection().getDirection() == Direction.WEST,
              ImmutableMap.of(
                  externalConnectionShape.connection().getDirection(), CONNECTOR_FRONT_MATERIAL,
                  externalConnectionShape.connection().getDirection().getOpposite(), CONNECTOR_BACK_MATERIAL),
              CONNECTOR_SIDE_MATERIAL));
    }

    return visualShape;
  }

  public static ArrayList<BakedQuad> createBakedQuads(
      Function<Material, TextureAtlasSprite> spriteGetter, VisualShape visualShape) {
    ArrayList<BakedQuad> quads = new ArrayList<>();

    for (var box : visualShape.getBoxes()) {
      TextureAtlasSprite texture = spriteGetter.apply(box.getMaterial());
      quads.addAll(QuadHelper.createCube(box.getMin(), box.getMax(), texture));
    }

    for (var internalConnection : visualShape.getInternalConnections()) {
      TextureAtlasSprite texture = spriteGetter.apply(internalConnection.material());
      quads.addAll(
          QuadHelper.createCube(
              internalConnection.min(), internalConnection.max(), new Vec2(0.0f, 8.0f), new Vec2(16.0f, 12.0f),
              internalConnection.rotateUv(), texture));
    }

    // TODO: move the UV stuff to the VisualShape.
    Vec2 minUv = new Vec2(0.0f, 0.0f);
    Vec2 frontBackMaxUv = new Vec2(16.0f, 16.0f);
    for (var externalConnection : visualShape.getExternalConnections()) {
      final Vec2 sideMaxUv = new Vec2(16.0f, 4.0f);

      for (Direction dir : Direction.values()) {
        Material material = externalConnection.materials().get(dir);
        Vec2 maxUv = frontBackMaxUv;
        if (material == null) {
          material = externalConnection.defaultMaterial();
          maxUv = sideMaxUv;
        }
        quads.add(
            QuadHelper.createQuad(
                dir, externalConnection.min(), externalConnection.max(), minUv, maxUv, externalConnection.rotateUv(),
                spriteGetter.apply(material)));
      }
    }

    return quads;
  }
}
