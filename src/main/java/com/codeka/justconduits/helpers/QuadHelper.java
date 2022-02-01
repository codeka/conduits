package com.codeka.justconduits.helpers;

import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import java.util.ArrayList;
import java.util.List;

/** Helper for building baked quads. */
// TODO: A lot of this can be optimized quite a bit by reducing memory allocations.
public class QuadHelper {
  public static Vector3f v(float x, float y, float z) {
    return new Vector3f(x, y, z);
  }

  /**
   * Creates a simple cube with all four sides having the same texture.
   */
  public static List<BakedQuad> createCube(Vector3f min, Vector3f max, TextureAtlasSprite sprite) {
    ArrayList<BakedQuad> quads = new ArrayList<>();
    quads.add(createQuad(Direction.DOWN, min, max, sprite));
    quads.add(createQuad(Direction.UP, min, max, sprite));
    quads.add(createQuad(Direction.NORTH, min, max, sprite));
    quads.add(createQuad(Direction.SOUTH, min, max, sprite));
    quads.add(createQuad(Direction.EAST, min, max, sprite));
    quads.add(createQuad(Direction.WEST, min, max, sprite));
    return quads;
  }

  /**
   * Creates a quad aligned to the given face. We assume the quad is one face of a cube with the given {@code min} and
   * {@code max} coordinates.
   *
   * @param face The face you want to create the quad on.
   * @param min The minimum coordinate of a cube this quad is assumed to be a face of.
   * @param max The maximum coordinate of a cube this quad is assumed to be a face of.
   * @param sprite The sprite to use.
   * @return A {@link BakedQuad}.
   */
  public static BakedQuad createQuad(Direction face, Vector3f min, Vector3f max, TextureAtlasSprite sprite) {
    float sw = sprite.getWidth();
    float sh = sprite.getHeight();

    return switch(face) {
      case DOWN -> createQuad(
          v(min.x(), min.y(), min.z()), v(max.x(), min.y(), min.z()),
          v(max.x(), min.y(), max.z()), v(min.x(), min.y(), max.z()),
          new Vec2(0.0f, 0.0f), new Vec2(sw, sh), false,
          face, sprite);
      case UP -> createQuad(
          v(min.x(), max.y(), min.z()), v(min.x(), max.y(), max.z()),
          v(max.x(), max.y(), max.z()), v(max.x(), max.y(), min.z()),
          new Vec2(0.0f, 0.0f), new Vec2(sw, sh), false,
          face, sprite);
      case NORTH -> createQuad(
          v(min.x(), max.y(), min.z()), v(max.x(), max.y(), min.z()),
          v(max.x(), min.y(), min.z()), v(min.x(), min.y(), min.z()),
          new Vec2(0.0f, 0.0f), new Vec2(sw, sh), false,
          face, sprite);
      case SOUTH -> createQuad(
          v(min.x(), max.y(), max.z()), v(min.x(), min.y(), max.z()),
          v(max.x(), min.y(), max.z()), v(max.x(), max.y(), max.z()),
          new Vec2(0.0f, 0.0f), new Vec2(sw, sh), false,
          face, sprite);
      case EAST -> createQuad(
          v(max.x(), max.y(), min.z()), v(max.x(), max.y(), max.z()),
          v(max.x(), min.y(), max.z()), v(max.x(), min.y(), min.z()),
          new Vec2(0.0f, 0.0f), new Vec2(sw, sh), false,
          face, sprite);
      case WEST -> createQuad(
          v(min.x(), max.y(), min.z()), v(min.x(), min.y(), min.z()),
          v(min.x(), min.y(), max.z()), v(min.x(), max.y(), max.z()),
          new Vec2(0.0f, 0.0f), new Vec2(sw, sh), false,
          face, sprite);
    };
  }

  /**
   * Creates a quad aligned to the given face. We assume the quad is one face of a cube with the given {@code min} and
   * {@code max} coordinates.
   *
   * @param face The face you want to create the quad on.
   * @param min The minimum coordinate of a cube this quad is assumed to be a face of.
   * @param max The maximum coordinate of a cube this quad is assumed to be a face of.
   * @param sprite The sprite to use.
   * @return A {@link BakedQuad}.
   */
  public static BakedQuad createQuad(
      Direction face, Vector3f min, Vector3f max, Vec2 uvMin, Vec2 uvMax, boolean rotateUv, TextureAtlasSprite sprite) {
    return switch(face) {
      case DOWN -> createQuad(
          v(min.x(), min.y(), min.z()), v(max.x(), min.y(), min.z()),
          v(max.x(), min.y(), max.z()), v(min.x(), min.y(), max.z()),
          uvMin, uvMax, rotateUv, face, sprite);
      case UP -> createQuad(
          v(min.x(), max.y(), min.z()), v(min.x(), max.y(), max.z()),
          v(max.x(), max.y(), max.z()), v(max.x(), max.y(), min.z()),
          uvMin, uvMax, rotateUv, face, sprite);
      case NORTH -> createQuad(
          v(min.x(), max.y(), min.z()), v(max.x(), max.y(), min.z()),
          v(max.x(), min.y(), min.z()), v(min.x(), min.y(), min.z()),
          uvMin, uvMax, rotateUv, face, sprite);
      case SOUTH -> createQuad(
          v(min.x(), max.y(), max.z()), v(min.x(), min.y(), max.z()),
          v(max.x(), min.y(), max.z()), v(max.x(), max.y(), max.z()),
          uvMin, uvMax, rotateUv, face, sprite);
      case EAST -> createQuad(
          v(max.x(), max.y(), min.z()), v(max.x(), max.y(), max.z()),
          v(max.x(), min.y(), max.z()), v(max.x(), min.y(), min.z()),
          uvMin, uvMax, rotateUv, face, sprite);
      case WEST -> createQuad(
          v(min.x(), max.y(), min.z()), v(min.x(), min.y(), min.z()),
          v(min.x(), min.y(), max.z()), v(min.x(), max.y(), max.z()),
          uvMin, uvMax, rotateUv, face, sprite);
    };
  }

  private static BakedQuad createQuad(
      Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4, Vec2 uvMin, Vec2 uvMax, boolean rotateUv, Direction dir,
      TextureAtlasSprite sprite) {
    var builder = new BakedQuadBuilder(sprite);
    Vector3f normal = dir.step();
    builder.setQuadOrientation(dir);
    if ((dir.getAxisDirection() == Direction.AxisDirection.POSITIVE && !rotateUv) ||
        (dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE && rotateUv)) {
      putVertex(builder, normal, v1, uvMin.x, uvMin.y, sprite);
      putVertex(builder, normal, v2, uvMin.x, uvMax.y, sprite);
      putVertex(builder, normal, v3, uvMax.x, uvMax.y, sprite);
      putVertex(builder, normal, v4, uvMax.x, uvMin.y, sprite);
    } else {
      putVertex(builder, normal, v1, uvMin.x, uvMax.y, sprite);
      putVertex(builder, normal, v2, uvMax.x, uvMax.y, sprite);
      putVertex(builder, normal, v3, uvMax.x, uvMin.y, sprite);
      putVertex(builder, normal, v4, uvMin.x, uvMin.y, sprite);
    }
    return builder.build();
  }

  private static void putVertex(BakedQuadBuilder builder, Vector3f normal, Vector3f vector, float u, float v,
                                TextureAtlasSprite sprite) {
    var elements = builder.getVertexFormat().getElements();
    for (int j = 0 ; j < elements.size() ; j++) {
      var e = elements.get(j);
      switch (e.getUsage()) {
        case POSITION -> builder.put(j, vector.x(), vector.y(), vector.z(), 1.0f);
        case COLOR    -> builder.put(j, 1.0f, 1.0f, 1.0f, 1.0f);
        case UV       -> putVertexUV(builder, u, v, sprite, j, e);
        case NORMAL   -> builder.put(j, normal.x(), normal.y(), normal.z());
        default       -> builder.put(j);
      }
    }
  }

  private static void putVertexUV(
      BakedQuadBuilder builder, float u, float v, TextureAtlasSprite sprite, int j, VertexFormatElement e) {
    switch (e.getIndex()) {
      case 0  -> builder.put(j, sprite.getU(u), sprite.getV(v));
      case 2  -> builder.put(j, (short) 0, (short) 0);
      default -> builder.put(j);
    }
  }
}