package com.codeka.justconduits.common.shape;

import com.codeka.justconduits.common.capabilities.network.ConduitType;
import com.mojang.math.Vector3f;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains the "overall" shape of the conduit, which we use to build the visual shape, the collision shape
 * and so on. This is where we have all of the logic for working out where to put the center of the conduits, when to
 * join conduits and so on.
 */
public class ConduitShape {
  // The shapes for each conduit type.
  private final HashMap<ConduitType, SingleConduitShape> shapes = new HashMap<>();
  private final ArrayList<ExternalConnectionShape> externalConnectionShapes = new ArrayList<>();

  public SingleConduitShape addConduit(ConduitType conduitType, Vec3 center) {
    SingleConduitShape singleConduitShape = new SingleConduitShape(center);
    shapes.put(conduitType, singleConduitShape);
    return singleConduitShape;
  }

  public void addExternalConnectionShape(Vector3f min, Vector3f max) {
    externalConnectionShapes.add(new ExternalConnectionShape(min, max));
  }

  public Map<ConduitType, SingleConduitShape> getShapes() {
    return shapes;
  }

  public List<ExternalConnectionShape> getExternalConnectionShapes() {
    return externalConnectionShapes;
  }

  /** The shape of a single conduit. A center and a bunch of directions for each of the pipes. */
  public static final class SingleConduitShape {
    private final Vec3 center;

    private final ArrayList<Direction> directions = new ArrayList<>();

    public SingleConduitShape(Vec3 center) {
      this.center = center;
    }

    public void addDirection(Direction dir) {
      directions.add(dir);
    }

    public Vec3 getCenter() {
      return center;
    }

    public ArrayList<Direction> getDirections() {
      return directions;
    }
  }

  /** The shape of an external connection. */
  public static final class ExternalConnectionShape {
    private final Vector3f min;
    private final Vector3f max;

    public ExternalConnectionShape(Vector3f min, Vector3f max) {
      this.min = min;
      this.max = max;
    }

    public Vector3f getMin() {
      return min;
    }

    public Vector3f getMax() {
      return max;
    }

    public VoxelShape getVoxelShape() {
      return Shapes.box(min.x(), min.y(), min.z(), max.x(), max.y(), max.z());
    }
  }
}
