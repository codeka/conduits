package com.codeka.justconduits.common.shape;

import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.impl.ConduitType;
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

  public void addExternalConnectionShape(ConduitConnection connection, Vector3f min, Vector3f max) {
    externalConnectionShapes.add(new ExternalConnectionShape(connection, min, max));
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

    private final HashMap<Direction, ConduitConnectionShape> connectionShapes = new HashMap<>();

    public SingleConduitShape(Vec3 center) {
      this.center = center;
    }

    public void addConnectionShape(ConduitConnectionShape shape) {
      connectionShapes.put(shape.getDirection(), shape);
    }

    public Vec3 getCenter() {
      return center;
    }

    public HashMap<Direction, ConduitConnectionShape> getConnectionShapes() {
      return connectionShapes;
    }
  }

  /** The "shape" of a single conduit connection. */
  public static final class ConduitConnectionShape {
    private final Direction direction;
    private final double length;

    public ConduitConnectionShape(Direction direction, double length) {
      this.direction = direction;
      this.length = length;
    }

    public Direction getDirection() {
      return direction;
    }

    /**
     * The length of this connection. Usually this is 1.0, but sometimes around corners and stuff it could be a little
     * longer or shorter.
     */
    public double getLength() {
      return length;
    }
  }

  /** The shape of an external connection. */
  public static final class ExternalConnectionShape {
    private final ConduitConnection connection;
    private final Vector3f min;
    private final Vector3f max;

    public ExternalConnectionShape(ConduitConnection connection, Vector3f min, Vector3f max) {
      this.connection = connection;
      this.min = min;
      this.max = max;
    }

    public ConduitConnection getConnection() {
      return connection;
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
