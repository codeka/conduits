package com.codeka.justconduits.client.gui;

public enum Icon {
  /** A checkmark, used by {@link CheckButton}. */
  CHECKMARK(0, 80);

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
