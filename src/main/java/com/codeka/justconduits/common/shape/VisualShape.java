package com.codeka.justconduits.common.shape;

import com.mojang.math.Vector3f;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper class to hold the visual shape of a conduit, before we turn it into a list of BakedQuads.
 */
public class VisualShape {
  private final ArrayList<Box> boxes = new ArrayList<>();
  private final ArrayList<MultiTextureBox> multiTextureBoxes = new ArrayList<>();

  public void addBox(Box box) {
    boxes.add(box);
  }

  public void addMultiTextureBox(MultiTextureBox multiTextureBox) {
    this.multiTextureBoxes.add(multiTextureBox);
  }

  public ArrayList<Box> getBoxes() {
    return boxes;
  }

  public ArrayList<MultiTextureBox> getMultiTextureBoxes() {
    return multiTextureBoxes;
  }

  public record Box(Vector3f min, Vector3f max, Material material) {
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

  public record MultiTextureBox(
      Vector3f min, Vector3f max, Map<Direction, Material> materials, Material defaultMaterial) {
  }
}
