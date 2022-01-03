package com.codeka.justconduits.client.gui;

import net.minecraft.network.chat.Component;

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

  private void handlePress() {
    setChecked(!isChecked);
    // TODO: do we have to propagate this?
  }

  public static class Builder extends IconButton.Builder {
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
