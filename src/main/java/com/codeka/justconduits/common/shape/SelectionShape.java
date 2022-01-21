package com.codeka.justconduits.common.shape;

import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.impl.ConduitType;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

  public void addShape(ConduitType conduitType, VoxelShape voxelShape) {
    shapes.add(new Shape(conduitType, voxelShape));
  }

  public List<Shape> getShapes() {
    return shapes;
  }

  public static class Shape {
    private final VoxelShape voxelShape;

    @Nullable
    private final ConduitConnection connection;

    @Nullable
    private final ConduitType conduitType;

    public Shape(@Nonnull ConduitConnection connection, VoxelShape voxelShape) {
      this.connection = connection;
      this.voxelShape = voxelShape;
      this.conduitType = null;
    }

    public Shape(@Nonnull ConduitType conduitType, VoxelShape voxelShape) {
      this.voxelShape = voxelShape;
      this.conduitType = conduitType;
      this.connection = null;
    }

    public VoxelShape getVoxelShape() {
      return voxelShape;
    }

    @Nullable
    public ConduitConnection getConnection() {
      return connection;
    }

    @Nullable
    public ConduitType getConduitType() {
      return conduitType;
    }
  }
}
