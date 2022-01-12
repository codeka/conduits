package com.codeka.justconduits.common.shape;

import com.codeka.justconduits.common.blocks.ConduitConnection;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link SelectionShape} contains all the info we need to perform selection of individual parts of the conduit.
 */
public class SelectionShape {
  private final ArrayList<Shape> shapes = new ArrayList<>();

  public void addShape(ConduitConnection connection, VoxelShape voxelShape) {
    shapes.add(new Shape(connection, voxelShape));
  }

  public List<Shape> getShapes() {
    return shapes;
  }

  public static class Shape {
    private final VoxelShape voxelShape;
    private final ConduitConnection connection;

    public Shape(ConduitConnection connection, VoxelShape voxelShape) {
      this.connection = connection;
      this.voxelShape = voxelShape;
    }

    public VoxelShape getVoxelShape() {
      return voxelShape;
    }

    public ConduitConnection getConnection() {
      return connection;
    }
  }
}
