package com.codeka.justconduits.client.gui.widgets;

public enum Icon {
  ALWAYS_ON(0, 80),
  REDSTONE_ON(16, 80),
  REDSTONE_OFF(32, 80),
  ALWAYS_OFF(48, 80),
  ITEMS(64, 80),
  FLUID(80, 80),
  ENERGY(96, 80);

  // Icon sizes are hard-coded.
  public static final int WIDTH = 16;
  public static final int HEIGHT = 16;

  private final int x;
  private final int y;

  Icon(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }
}
