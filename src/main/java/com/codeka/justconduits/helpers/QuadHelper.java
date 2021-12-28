package com.codeka.justconduits.helpers;

import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
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
   * Creates a list of {@link BakedQuad}s representing a cube.
   *
   * @param transformation The rotation to apply to the quad.
   * @param sprite The sprite to use.
   * @return A list of {@link BakedQuad}s representing a cube with all faces defined.
   */
  public static List<BakedQuad> createCube(Transformation transformation, TextureAtlasSprite sprite) {
    ArrayList<BakedQuad> quads = new ArrayList<>();
    quads.add(QuadHelper.createQuad(Direction.UP, transformation, sprite));
    quads.add(QuadHelper.createQuad(Direction.DOWN, transformation, sprite));
    quads.add(QuadHelper.createQuad(Direction.NORTH, transformation, sprite));
    quads.add(QuadHelper.createQuad(Direction.SOUTH, transformation, sprite));
    quads.add(QuadHelper.createQuad(Direction.EAST, transformation, sprite));
    quads.add(QuadHelper.createQuad(Direction.WEST, transformation, sprite));
    return quads;
  }

  /**
   * Creates a quad aligned to the given face.
   *
   * @param face The face you want to create the quad on.
   * @param transformation The rotation to apply to the quad.
   * @param sprite The sprite to use.
   * @return A {@link BakedQuad}.
   */
  public static BakedQuad createQuad(Direction face, Transformation transformation, TextureAtlasSprite sprite) {
    return switch(face) {
      case DOWN -> createQuad(v(0, 0, 0), v(1, 0, 0), v(1, 0, 1), v(0, 0, 1), transformation, sprite);
      case UP -> createQuad(v(0, 1, 0), v(0, 1, 1), v(1, 1, 1), v(1, 1, 0), transformation, sprite);
      case NORTH -> createQuad(v(0, 1, 0), v(1, 1, 0), v(1, 0, 0), v(0, 0, 0), transformation, sprite);
      case SOUTH -> createQuad(v(0, 1, 1), v(0, 0, 1), v(1, 0, 1), v(1, 1, 1), transformation, sprite);
      case EAST -> createQuad(v(1, 1, 0), v(1, 1, 1), v(1, 0, 1), v(1, 0, 0), transformation, sprite);
      case WEST -> createQuad(v(0, 1, 0), v(0, 0, 0), v(0, 0, 1), v(0, 1, 1), transformation, sprite);
    };
  }

  /**
   * Create a quad with the given four vertices. The vertices should always define a plane.
   *
   * @param v1 The first vertex.
   * @param v2 The second vertex.
   * @param v3 The third vertex.
   * @param v4 The fourth vertex.
   * @param transformation A transformation to apply to the vertices before baking them.
   * @param sprite The texture to apply to the quad.
   * @return A {@link BakedQuad}.
   */
  public static BakedQuad createQuad(Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4, Transformation transformation,
                                     TextureAtlasSprite sprite) {
    Vector3f normal = v3.copy();
    normal.sub(v2);
    Vector3f temp = v1.copy();
    temp.sub(v2);
    normal.cross(temp);
    normal.normalize();

    int tw = sprite.getWidth();
    int th = sprite.getHeight();

    transformation = transformation.blockCenterToCorner();
    transformation.transformNormal(normal);

    Vector4f vv1 = new Vector4f(v1); transformation.transformPosition(vv1);
    Vector4f vv2 = new Vector4f(v2); transformation.transformPosition(vv2);
    Vector4f vv3 = new Vector4f(v3); transformation.transformPosition(vv3);
    Vector4f vv4 = new Vector4f(v4); transformation.transformPosition(vv4);

    var builder = new BakedQuadBuilder(sprite);
    builder.setQuadOrientation(Direction.getNearest(normal.x(), normal.y(), normal.z()));
    putVertex(builder, normal, vv1, 0, 0, sprite);
    putVertex(builder, normal, vv2, 0, th, sprite);
    putVertex(builder, normal, vv3, tw, th, sprite);
    putVertex(builder, normal, vv4, tw, 0, sprite);
    return builder.build();
  }

  private static void putVertex(BakedQuadBuilder builder, Vector3f normal, Vector4f vector, float u, float v,
                                TextureAtlasSprite sprite) {
    var elements = builder.getVertexFormat().getElements().asList();
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

  private static void putVertexUV(BakedQuadBuilder builder, float u, float v, TextureAtlasSprite sprite, int j,
                                  VertexFormatElement e) {
    switch (e.getIndex()) {
      case 0  -> builder.put(j, sprite.getU(u), sprite.getV(v));
      case 2  -> builder.put(j, (short) 0, (short) 0);
      default -> builder.put(j);
    }
  }
}