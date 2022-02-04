package com.codeka.justconduits.common.impl.item;

public record ItemConduitConfig(boolean supportColors, boolean supportFilter, boolean supportUpgrade) {
  public static ItemConduitConfig SIMPLE = new ItemConduitConfig(false, false, false);
  public static ItemConduitConfig REGULAR = new ItemConduitConfig(true, false, false);
  public static ItemConduitConfig ADVANCED = new ItemConduitConfig(true, true, true);
}
