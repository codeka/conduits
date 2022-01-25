package com.codeka.justconduits.common.shape;

import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.impl.ConduitType;
import com.mojang.math.Vector3f;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
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

  /**
   * The shape of a single conduit. A center and a bunch of directions for each of the pipes.
   *
   * <p>If a neighbour has a different configuration of conduits, it could result in the center of it's conduit not
   * matching with ours. In that case, {@code needComplexConduit} will be true, and you can use the min and max to
   * get the shape of the "combined" conduit connector instead.
   */
  public static final class SingleConduitShape {
    private Vec3 center;
    private boolean needComplexConduit;
    private Vec3 min;
    private Vec3 max;

    private final HashMap<Direction, ConduitConnectionShape> connectionShapes = new HashMap<>();

    public SingleConduitShape(Vec3 center) {
      this.center = center;
    }

    /** In some cases, we'll want to adjust the center after it's been updated. */
    public void updateCenter(Vec3 newCenter) {
      center = newCenter;
    }

    /**
     * Adds the center shape from our neighbours. If it's different to our center, then we'll mark ourselves as needing
     * a complex shape.
     */
    public void addCenter(Vec3 center, Direction dir) {
      // We only care if the centers are different in plane perpendicular to the direction the connection is in. If
      // it's just further or closer, that's OK because we'll just lengthen/shorten the pipe.
      double px = 1.0 - Math.abs(dir.getStepX());
      double py = 1.0 - Math.abs(dir.getStepY());
      double pz = 1.0 - Math.abs(dir.getStepZ());
      double dx = this.center.x() * px - center.x() * px;
      double dy = this.center.y() * py - center.y() * py;
      double dz = this.center.z() * pz - center.z() * pz;
      if (new Vec3(dx, dy, dz).length() > 0.01) {
        if (!needComplexConduit) {
          needComplexConduit = true;
          min = new Vec3(this.center.x(), this.center.y(), this.center.z());
          max = new Vec3(this.center.x(), this.center.y(), this.center.z());
        }
        min = new Vec3(
            Math.min(min.x(), center.x() * px + (min.x() * (1.0 - px))),
            Math.min(min.y(), center.y() * py + (min.y() * (1.0 - py))),
            Math.min(min.z(), center.z() * pz + (min.z() * (1.0 - pz))));
        max = new Vec3(
            Math.max(max.x(), center.x() * px + (max.x() * (1.0 - px))),
            Math.max(max.y(), center.y() * py + (max.y() * (1.0 - py))),
            Math.max(max.z(), center.z() * pz + (max.z() * (1.0 - pz))));
      }
    }

    public void addConnectionShape(ConduitConnectionShape shape) {
      connectionShapes.put(shape.getDirection(), shape);
    }

    public Vec3 getCenter() {
      return center;
    }

    public boolean needComplexConduit() {
      return needComplexConduit;
    }

    public Vec3 getMin() {
      return min;
    }

    public Vec3 getMax() {
      return max;
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
