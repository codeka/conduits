package com.codeka.justconduits.client.gui.widgets;

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
  private DataSource<Boolean> isChecked;

  public CheckButton(int x, int y, int width, int height, Component message, OnTooltip onTooltip) {
    super(x, y, width, height, message, (btn) -> ((CheckButton) btn).handlePress() ,onTooltip);
  }

  public void setChecked(boolean checked) {
    if (isChecked.getValue() == checked) {
      return;
    }

    isChecked.setValue(checked);
  }

  @Override
  protected void renderIconOrText(@NotNull PoseStack poseStack) {
    boolean showCheck = isChecked.getValue();
    setIcon(showCheck ? Icon.CHECKMARK : null);
    setPressed(showCheck);

    super.renderIconOrText(poseStack);

    // TODO: maybe we don't always need to render this thing?
    Font font = Minecraft.getInstance().font;
    drawString(
        poseStack, Minecraft.getInstance().font, getMessage(),
        x + width + 3, y + (height / 2 - font.lineHeight / 2),
        getFGColor());
  }

  private void handlePress() {
    setChecked(!isChecked.getValue());
    // TODO: do we have to propagate this?
  }

  public static class Builder extends IconButton.Builder<CheckButton.Builder> {
    private DataSource<Boolean> checkedDataSource;

    public Builder(int x, int y) {
      super(x, y);
    }

    public Builder(int x, int y, int width, int height) {
      super(x, y, width, height);
    }

    public Builder withCheckedDataSource(DataSource<Boolean> checkedDataSource) {
      this.checkedDataSource = checkedDataSource;
      return this;
    }

    @Override
    protected void prepare() {
      super.prepare();

      if (checkedDataSource == null) {
        checkedDataSource = new DataSource<>() {
          private boolean value;

          @Override
          public Boolean getValue() {
            return value;
          }

          @Override
          public void setValue(Boolean value) {
            this.value = value;
          }
        };
      }
    }

    public CheckButton build() {
      prepare();
      CheckButton btn = new CheckButton(x, y, width, height, message, onTooltip);
      btn.isChecked = checkedDataSource;
      return btn;
    }
  }
}
