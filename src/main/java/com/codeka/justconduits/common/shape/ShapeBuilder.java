package com.codeka.justconduits.common.shape;

import com.codeka.justconduits.client.blocks.ConduitModelLoader;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.impl.ConduitType;
import com.mojang.math.Vector3f;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Used by the {@link ShapeManager} to actually build the shapes.
 */
public class ShapeBuilder {
  /**
   * Generates the "main" shape of the conduit. The main shape is the center of each of the nobbly bits and a list of
   * directions that the "pipes" come out of.
   */
  public static ConduitShape generateMainShape(ConduitBlockEntity conduitBlockEntity) {
    ConduitShape mainShape = new ConduitShape();

    HashSet<Direction> directions = new HashSet<>();
    for (ConduitConnection conn : conduitBlockEntity.getConnections()) {
      directions.add(conn.getDirection());
    }

    ArrayList<ConduitType> conduitTypes = new ArrayList<>(conduitBlockEntity.getConduitTypes());
    for (int i = 0; i < conduitTypes.size(); i++) {
      Vec3 centerOffset = CenterOffsets.getCenterOffset(i, conduitTypes.size(), directions);
      var shape = mainShape.addConduit(conduitTypes.get(i), centerOffset);
      populateSingleShape(conduitBlockEntity, shape, conduitTypes.get(i), centerOffset);
    }

    // Add shapes for the external connections. For the external connections, we record the complete shape of the box
    // as the calculation is kind of complicated.
    for (ConduitConnection conn : conduitBlockEntity.getConnections()) {
      if (conn.getConnectionType() != ConduitConnection.ConnectionType.EXTERNAL) {
        continue;
      }

      // TODO: this is actually a lot simpler than I have it here.
      var dir = conn.getDirection();
      Vector3f normal = new Vector3f(dir.getStepX(), dir.getStepY(), dir.getStepZ());
      Vector3f min = new Vector3f(
          0.5f - (0.5f * (1.0f - Math.abs(normal.x()))) - 0.125f * Math.abs(normal.x()) + 0.375f * normal.x(),
          0.5f - (0.5f * (1.0f - Math.abs(normal.y()))) - 0.125f * Math.abs(normal.y()) + 0.375f * normal.y(),
          0.5f - (0.5f * (1.0f - Math.abs(normal.z()))) - 0.125f * Math.abs(normal.z()) + 0.375f * normal.z());
      Vector3f max = new Vector3f(
          0.5f + (0.5f * (1.0f - Math.abs(normal.x()))) + 0.125f * Math.abs(normal.x()) + 0.375f * normal.x(),
          0.5f + (0.5f * (1.0f - Math.abs(normal.y()))) + 0.125f * Math.abs(normal.y()) + 0.375f * normal.y(),
          0.5f + (0.5f * (1.0f - Math.abs(normal.z()))) + 0.125f * Math.abs(normal.z()) + 0.375f * normal.z());
      mainShape.addExternalConnectionShape(conn, min, max);
    }

    return mainShape;
  }

  private static void populateSingleShape(
      ConduitBlockEntity conduitBlockEntity, ConduitShape.SingleConduitShape shape, ConduitType conduitType,
      Vec3 centerOffset) {
    Level level = conduitBlockEntity.getLevel();
    if (level == null) {
      return;
    }

    for (ConduitConnection conn : conduitBlockEntity.getConnections()) {
      if (conn.getConduitTypes().contains(conduitType)) {
        Direction dir = conn.getDirection();
        double length = 1.0;
        if (conn.getConnectedBlockEntity(level) instanceof ConduitBlockEntity connectedBlockEntity) {
          Vec3 otherCenterOffset =
              connectedBlockEntity.getShapeManager().getCenterOffset(connectedBlockEntity, conduitType);
          // TODO: if it's null what do we do?

          // We only look in the negative direction to see if we had mis-matched centers, because in the positive
          // direction, we always draw the connections from the actual center.
          if (dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
            if (conduitBlockEntity.getConnections().size() == 1) {
              // If this is the only connection, then we'll actually change the center to be this, because it looks
              // way better than having a complex connector with only one connection. But only change the position in
              // the plane of the connection, not the distance. Because the other conduit will be drawing a connection
              // to us, expecting it to be at that particular distance.
              double sx = Math.abs(dir.getStepX());
              double sy = Math.abs(dir.getStepY());
              double sz = Math.abs(dir.getStepZ());
              shape.updateCenter(new Vec3(
                  shape.getCenter().x() * sx + otherCenterOffset.x() * (1.0 - sx),
                  shape.getCenter().y() * sy + otherCenterOffset.y() * (1.0 - sy),
                  shape.getCenter().z() * sz + otherCenterOffset.z() * (1.0 - sz)));
            } else {
              shape.addCenter(otherCenterOffset, dir);
            }
          }

          Vec3 diff = otherCenterOffset.subtract(centerOffset);
          length += diff.x() * dir.getStepX() + diff.y() * dir.getStepY() + diff.z() * dir.getStepZ();
        } else if (conn.getConnectionType() == ConduitConnection.ConnectionType.EXTERNAL) {
          // We want the length to go almost to the edge of this block, and no further: there will be an external
          // connection at the edge, we don't want to go past that.
          double dx = Math.abs(dir.getStepX());
          double dy = Math.abs(dir.getStepY());
          double dz = Math.abs(dir.getStepZ());
          double centerLength = centerOffset.x() * dx + centerOffset.y() * dy + centerOffset.z() * dz;
          centerLength = centerLength - Math.floor(centerLength);
          if (dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
            centerLength = 1.0 - centerLength;
          }
          length = centerLength;
        }
        shape.addConnectionShape(new ConduitShape.ConduitConnectionShape(dir, length, conn.getConnectionType()));
      }
    }
  }

  public static VoxelShape createCollisionShape(ConduitShape mainShape) {
    VoxelShape collisionShape = Shapes.empty();
    for (var shape : mainShape.getShapes().values()) {
      Vec3 c = shape.getCenter();
      VoxelShape voxelShape =
          Shapes.box(c.x - 0.125f, c.y - 0.125f, c.z - 0.125f, c.x + 0.125f, c.y + 0.125f, c.z + 0.125f);
      collisionShape = Shapes.joinUnoptimized(collisionShape, voxelShape, BooleanOp.OR);

      for (var conn : shape.getConnectionShapes().values()) {
        if (conn.getDirection().getAxisDirection() != Direction.AxisDirection.POSITIVE) {
          continue;
        }

        Vector3f normal = conn.getDirection().step();
        float length = (float) conn.getLength();
        Vector3f plane =
            new Vector3f(1.0f - Math.abs(normal.x()), 1.0f - Math.abs(normal.y()), 1.0f - Math.abs(normal.z()));
        voxelShape = Shapes.box(
            (float) c.x + normal.x() * 0.125f - plane.x() * 0.125f,
            (float) c.y + normal.y() * 0.125f - plane.y() * 0.125f,
            (float) c.z + normal.z() * 0.125f - plane.z() * 0.125f,
            (float) c.x + normal.x() * (length - 0.125f) + plane.x() * 0.125f,
            (float) c.y + normal.y() * (length - 0.125f) + plane.y() * 0.125f,
            (float) c.z + normal.z() * (length - 0.125f) + plane.z() * 0.125f);
        collisionShape = Shapes.joinUnoptimized(collisionShape, voxelShape, BooleanOp.OR);
      }
    }

    for (var shape : mainShape.getExternalConnectionShapes()) {
      collisionShape = Shapes.joinUnoptimized(collisionShape, shape.getVoxelShape(), BooleanOp.OR);
    }

    return collisionShape.optimize();
  }

  public static SelectionShape createSelectionShape(ConduitShape mainShape) {
    SelectionShape selectionShape = new SelectionShape();
    for (ConduitShape.ExternalConnectionShape shape : mainShape.getExternalConnectionShapes()) {
      VoxelShape voxelShape = Shapes.box(
          shape.getMin().x(), shape.getMin().y(), shape.getMin().z(),
          shape.getMax().x(), shape.getMax().y(), shape.getMax().z());
      selectionShape.addShape(shape.getConnection(), voxelShape);
    }

    for (var entry : mainShape.getShapes().entrySet()) {
      ConduitType conduitType = entry.getKey();
      ConduitShape.SingleConduitShape shape = entry.getValue();

      Vec3 c = shape.getCenter();
      VoxelShape voxelShape =
          Shapes.box(c.x - 0.125f, c.y - 0.125f, c.z - 0.125f, c.x + 0.125f, c.y + 0.125f, c.z + 0.125f);
      // TODO: add the connections as well
      selectionShape.addShape(conduitType, voxelShape);
    }

    return selectionShape;
  }

  public static VisualShape createVisualShape(ConduitShape mainShape) {
    VisualShape visualShape = new VisualShape();
    for (var entry : mainShape.getShapes().entrySet()) {
      ConduitType conduitType = entry.getKey();
      ConduitShape.SingleConduitShape conduitShape = entry.getValue();

      // TODO: make this generic.
      Material material = ConduitModelLoader.MISSING_MATERIAL;
      if (conduitType == ConduitType.SIMPLE_ITEM) {
        material = ConduitModelLoader.SIMPLE_ITEM_CONDUIT_MATERIAL;
      } else if (conduitType == ConduitType.SIMPLE_FLUID) {
        material = ConduitModelLoader.SIMPLE_FLUID_CONDUIT_MATERIAL;
      } else if (conduitType == ConduitType.SIMPLE_ENERGY) {
        material = ConduitModelLoader.SIMPLE_ENERGY_CONDUIT_MATERIAL;
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
                ConduitModelLoader.SIMPLE_ITEM_CONDUIT_MATERIAL));
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
        if (shape.getDirection().getAxisDirection() != Direction.AxisDirection.POSITIVE
            && shape.getConnectionType() != ConduitConnection.ConnectionType.EXTERNAL) {
          continue;
        }

        Vector3f normal = shape.getDirection().step();
        float length = (float) shape.getLength();
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
        if (shape.getDirection().getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
          // If the normal is negative (which can be the case for external connections), then min & max will be swapped.
          // So wap them back again.
          Vector3f tmp = min;
          min = max;
          max = tmp;
        }
        visualShape.addBox(new VisualShape.Box(min, max, material));
      }
    }

    for (ConduitShape.ExternalConnectionShape externalConnectionShape : mainShape.getExternalConnectionShapes()) {
      visualShape.addBox(
          new VisualShape.Box(
              externalConnectionShape.getMin(),
              externalConnectionShape.getMax(),
              ConduitModelLoader.CONNECTOR_MATERIAL));
    }

    return visualShape;
  }
}
