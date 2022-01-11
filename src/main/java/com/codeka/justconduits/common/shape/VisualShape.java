package com.codeka.justconduits.common.shape;

import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;

import java.util.ArrayList;

/**
 * Wrapper class to hold the visual shape of a conduit, before we turn it into a list of BakedQuads.
 */
public class VisualShape {
  private final ArrayList<Box> boxes = new ArrayList<>();
  // TODO: include external connection locations.

  public void addBox(Box box) {
    boxes.add(box);
  }

  public ArrayList<Box> getBoxes() {
    return boxes;
  }

  public static final class Box {
    private final Vector3f min;
    private final Vector3f max;
    private final Material material;

    public Box(Vector3f min, Vector3f max, Material material) {
      this.min = min;
      this.max = max;
      this.material = material;
    }

    public Vector3f getMin() {
      return min;
    }

    public Vector3f getMax() {
      return max;
    }

    public Material getMaterial() {
      return material;
    }
  }
}
