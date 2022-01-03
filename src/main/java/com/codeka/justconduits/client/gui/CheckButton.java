package com.codeka.justconduits.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * An {@link IconButton} that hard codes itself to act like a checkbox.
 *
 * There is a vanilla <code>Checkbox</code> widget, but I don't really like the look of it, so here's our own.
 */
public class CheckButton extends IconButton {
  private boolean isChecked;

  public CheckButton(int x, int y, int width, int height, Component message, OnTooltip onTooltip) {
    super(x, y, width, height, message, (btn) -> ((CheckButton) btn).handlePress() ,onTooltip);
  }

  public void setChecked(boolean checked) {
    isChecked = checked;
    if (checked) {
      setIcon(Icon.CHECKMARK);
      setPressed(true);
    } else {
      setIcon(null);
      setPressed(false);
    }
  }

  public boolean isChecked() {
    return isChecked;
  }

  @Override
  protected void renderIconOrText(@NotNull PoseStack poseStack) {
    super.renderIconOrText(poseStack);

    // TODO: maybe we don't always need to render this thing?
    Font font = Minecraft.getInstance().font;
    drawString(
        poseStack, Minecraft.getInstance().font, getMessage(),
        x + width + 3, y + (height / 2 - font.lineHeight / 2),
        getFGColor());
  }

  private void handlePress() {
    setChecked(!isChecked);
    // TODO: do we have to propagate this?
  }

  public static class Builder extends IconButton.Builder<CheckButton.Builder> {
    public Builder(int x, int y) {
      super(x, y);
    }

    public Builder(int x, int y, int width, int height) {
      super(x, y, width, height);
    }

    @Override
    protected void prepare() {
      super.prepare();
    }

    public CheckButton build() {
      prepare();
      return new CheckButton(x, y, width, height, message, onTooltip);
    }
  }
}
