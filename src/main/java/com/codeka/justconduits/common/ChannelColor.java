package com.codeka.justconduits.common;

import net.minecraft.world.item.DyeColor;

public enum ChannelColor {
  WHITE(DyeColor.WHITE),
  ORANGE(DyeColor.ORANGE),
  MAGENTA(DyeColor.MAGENTA),
  LIGHT_BLUE(DyeColor.LIGHT_BLUE),
  YELLOW(DyeColor.YELLOW),
  LIME(DyeColor.LIME),
  PINK(DyeColor.PINK),
  GRAY(DyeColor.GRAY),
  LIGHT_GRAY(DyeColor.LIGHT_GRAY),
  CYAN(DyeColor.CYAN),
  PURPLE(DyeColor.PURPLE),
  BLUE(DyeColor.BLUE),
  BROWN(DyeColor.BROWN),
  GREEN(DyeColor.GREEN),
  RED(DyeColor.RED),
  BLACK(DyeColor.BLACK);

  public static ChannelColor fromNumber(int number) {
    if (number == 0) {
      return PINK;
    }
    return ChannelColor.values()[number - 1];
  }

  private final DyeColor dyeColor;

  ChannelColor(DyeColor dyeColor) {
    this.dyeColor = dyeColor;
  }

  public DyeColor getDyeColor() {
    return dyeColor;
  }

  public int getNumber() {
    return ordinal() + 1;
  }

  public ChannelColor nextColor() {
    int nextNumber = getNumber() + 1;
    if (nextNumber > 16) {
      nextNumber = 1;
    }
    return fromNumber(nextNumber);
  }

  public ChannelColor prevColor() {
    int prevNumber = getNumber() - 1;
    if (prevNumber <= 0) {
      prevNumber = 16;
    }
    return fromNumber(prevNumber);
  }
}
