package com.codeka.justconduits.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * A button that shows an icon, rather than a text message.
 */
public class IconButton extends SimpleButton {
  @Nullable
  private Icon icon;

  public IconButton(int x, int y, int width, int height, Component message, OnPress onPress, OnTooltip onTooltip) {
    super(x, y, width, height, message, onPress, onTooltip);
  }

  public void setIcon(Icon icon) {
    this.icon = icon;
  }

  @Override
  protected void renderIconOrText(@NotNull PoseStack poseStack) {
    if (icon != null) {
      // TODO: center it.
      blit(
          poseStack,
          x + (width / 2 - Icon.WIDTH / 2), y + (height / 2 - Icon.HEIGHT / 2),
          icon.getX(), icon.getY(), Icon.WIDTH, Icon.HEIGHT);
    }
  }

  public static class Builder<T extends IconButton.Builder<?>> extends SimpleButton.Builder<T> {
    private Icon icon;

    public Builder(int x, int y) {
      super(x, y);
    }

    public Builder(int x, int y, int width, int height) {
      super(x, y, width, height);
    }

    public T withIcon(Icon icon) {
      this.icon = icon;
      return (T) this;
    }

    public IconButton build() {
      prepare();
      IconButton btn = new IconButton(x, y, width, height, message, onPress, onTooltip);
      if (icon != null) {
        btn.setIcon(icon);
      }
      return btn;
    }
  }
}
