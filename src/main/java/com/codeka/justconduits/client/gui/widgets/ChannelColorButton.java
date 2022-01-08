package com.codeka.justconduits.client.gui.widgets;

import com.codeka.justconduits.common.ChannelColor;
import net.minecraft.network.chat.Component;

/** A button that lets you cycle between the {@link ChannelColor}s by clicking the button. */
public class ChannelColorButton extends SimpleButton {
  private ChannelColor color;

  public ChannelColorButton(int x, int y, int width, int height, Component message, OnPress onPress, OnTooltip onTooltip) {
    super(x, y, width, height, message, onPress, onTooltip);
  }

  public static class Builder extends SimpleButton.Builder<ChannelColorButton.Builder> {
    private ChannelColor color;

    public Builder(int x, int y) {
      super(x, y);
    }

    public Builder withColor(ChannelColor color) {
      this.color = color;
      return this;
    }

    @Override
    protected void prepare() {
      super.prepare();
      if (color == null) {
        color = ChannelColor.BLUE;
      }
    }

    public ChannelColorButton build() {
      prepare();
      ChannelColorButton btn = new ChannelColorButton(x, y, width, height, message, onPress, onTooltip);
      btn.color = color;
      return btn;
    }
  }
}
