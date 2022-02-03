package com.codeka.justconduits.common.shape;

import com.mojang.math.Vector3f;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.Map;

/**
 * Wrapper class to hold the visual shape of a conduit, before we turn it into a list of BakedQuads.
 */
public class VisualShape {
  private final ArrayList<Box> boxes = new ArrayList<>();
  private final ArrayList<InternalConnection> internalConnections = new ArrayList<>();
  private final ArrayList<ExternalConnection> externalConnections = new ArrayList<>();

  public void addBox(Box box) {
    boxes.add(box);
  }

  public void addInternalConnection(InternalConnection internalConnection) {
    internalConnections.add(internalConnection);
  }

  public void addExternalConnection(ExternalConnection externalConnection) {
    this.externalConnections.add(externalConnection);
  }

  public ArrayList<Box> getBoxes() {
    return boxes;
  }

  public ArrayList<InternalConnection> getInternalConnections() {
    return internalConnections;
  }

  public ArrayList<ExternalConnection> getExternalConnections() {
    return externalConnections;
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

  public record InternalConnection(
      Vector3f min, Vector3f max, boolean rotateUv, Material material) {
  }

  public record ExternalConnection(
      Vector3f min, Vector3f max, boolean rotateUv, Map<Direction, Material> materials, Material defaultMaterial) {
  }
}
