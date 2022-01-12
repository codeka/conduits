package com.codeka.justconduits.common.shape;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This class manages all the center offsets for all the configurations of conduits.
 *
 * <p>Depending what directions the connections go, and how many conduits there are in this block, we'll to offset the
 * center of the conduits so that they don't interfere with each other.
 */
public class CenterOffsets {

  private static final Vec3 CENTER = new Vec3(0.5f, 0.5f, 0.5f);

  // TODO: should not be used.
  private static final List<Vec3> DEFAULT_OFFSETS = Lists.newArrayList(
      new Vec3(0.25f, 0.5f, 0.5f), new Vec3(0.75f, 0.5f, 0.5f), new Vec3(0.0f, 0.75f, 0.5f),
      new Vec3(0.0f, 0.25f, 0.5f), new Vec3(0.25f, 0.75f, 0.5f), new Vec3(0.75f, 0.75f, 0.5f),
      new Vec3(0.25f, 0.25f, 0.5f), new Vec3(0.75f, 0.25f, 0.5f));

  private static final Map<Key, List<Vec3>> CENTER_OFFSETS_FOR_DIRECTIONS = ImmutableMap.<Key, List<Vec3>>builder()
      .put(
          new Key(Direction.SOUTH),
          Lists.newArrayList(
              new Vec3(0.25f, 0.5f, 0.5f), new Vec3(0.75f, 0.5f, 0.5f), new Vec3(0.0f, 0.75f, 0.5f),
              new Vec3(0.0f, 0.25f, 0.5f), new Vec3(0.25f, 0.75f, 0.5f), new Vec3(0.75f, 0.75f, 0.5f),
              new Vec3(0.25f, 0.25f, 0.5f), new Vec3(0.75f, 0.25f, 0.5f)))
      .put(
          new Key(Direction.EAST),
          Lists.newArrayList(
              new Vec3(0.5f, 0.5f, 0.25f), new Vec3(0.5f, 0.5f, 0.75f), new Vec3(0.5f, 0.75f, 0.0f),
              new Vec3(0.5f, 0.25f, 0.0f), new Vec3(0.5f, 0.75f, 0.25f), new Vec3(0.5f, 0.75f, 0.75f),
              new Vec3(0.5f, 0.25f, 0.25f), new Vec3(0.5f, 0.25f, 0.75f)))
      .build();

  /**
   * Gets the center offset for the index'th conduit out of a total of totalConduits.
   *
   * @param index The index of this conduit we want the center offset for.
   * @param totalConduits The total number of conduits in this block.
   * @param connections The directions of all the connections.
   * @return The center offset we should use.
   */
  public static Vec3 getCenterOffset(int index, int totalConduits, Set<Direction> connections) {
    if (totalConduits == 1) {
      return CENTER;
    }

    List<Vec3> offsets = CENTER_OFFSETS_FOR_DIRECTIONS.get(new Key(connections));
    if (offsets == null) {
      // TODO: we should cover all of the possibilities.
      offsets = DEFAULT_OFFSETS;
    }

    if (offsets.size() > index) {
      return offsets.get(index);
    }

    // TODO: what to do if there's too many conduits?
    return CENTER;
  }

  private static final class Key {
    private final HashSet<Direction> directions = new HashSet<>();

    public Key(Collection<Direction> directions) {
      for (Direction dir : directions) {
        // Note: only positive directions count.
        if (dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
          dir = dir.getOpposite();
        }
        this.directions.add(dir);
      }
    }

    public Key(Direction... directions) {
      this(Set.of(directions));
    }

    @Override
    public int hashCode() {
      return Objects.hash(directions);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Key other) {
        return Objects.equals(other.directions, directions);
      }
      return false;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      for (Direction dir : directions) {
        sb.append(" ");
        sb.append(dir);
      }
      sb.append(" }");
      return sb.toString();
    }
  }
}
