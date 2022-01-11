package com.codeka.justconduits.common.shape;

import com.codeka.justconduits.common.capabilities.network.ConduitType;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains the "overall" shape of the conduit, which we use to build the visual shape, the collision shape
 * and so on. This is where we have all of the logic for working out where to put the center of the conduits, when to
 * join conduits and so on.
 */
public class ConduitShape {
  // The shapes for each conduit type.
  private final HashMap<ConduitType, SingleConduitShape> shapes = new HashMap<>();

  public SingleConduitShape addConduit(ConduitType conduitType, Vec3 center) {
    SingleConduitShape singleConduitShape = new SingleConduitShape(center);
    shapes.put(conduitType, singleConduitShape);
    return singleConduitShape;
  }

  public Map<ConduitType, SingleConduitShape> getShapes() {
    return shapes;
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
}
