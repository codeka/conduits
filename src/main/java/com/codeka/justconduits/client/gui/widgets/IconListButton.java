package com.codeka.justconduits.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/** A button that lets you choose between a list of item by clicking. */
public class IconListButton extends IconButton {
  private DataSource<Integer> iconIndex;
  private final ArrayList<Icon> icons = new ArrayList<>();

  public IconListButton(int x, int y, int width, int height, Component message, OnTooltip onTooltip) {
    super(x, y, width, height, message, (btn) -> ((IconListButton) btn).handlePress() ,onTooltip);
  }

  public void setIconIndex(int index) {
    if (iconIndex.getValue() == index) {
      return;
    }

    if (index < 0 || index >= icons.size()) {
      return;
    }

    iconIndex.setValue(index);
  }

  public int getIconIndex() {
    return iconIndex.getValue();
  }

  @Override
  protected void renderIconOrText(@NotNull PoseStack poseStack) {
    Icon icon = icons.get(iconIndex.getValue());
    setIcon(icon);
//    setPressed(showCheck);

    super.renderIconOrText(poseStack);

    // TODO: maybe we don't always need to render this thing?
    Font font = Minecraft.getInstance().font;
    drawString(
        poseStack, Minecraft.getInstance().font, getMessage(),
        x + width + 3, y + (height / 2 - font.lineHeight / 2),
        getFGColor());
  }

  private void handlePress() {
    // TODO: handle right-click to go backwards.
    int newIndex = iconIndex.getValue() + 1;
    if (newIndex >= icons.size()) {
      newIndex = 0;
    }

    setIconIndex(newIndex);
    // TODO: do we have to propagate this?
  }

  public static class Builder extends IconButton.Builder<IconListButton.Builder> {
    private DataSource<Integer> iconIndexDataSource;
    private final ArrayList<Icon> icons = new ArrayList<>();

    public Builder(int x, int y) {
      super(x, y);
    }

    public Builder(int x, int y, int width, int height) {
      super(x, y, width, height);
    }

    public IconListButton.Builder withIconIndexDataSource(DataSource<Integer> iconIndexDataSource) {
      this.iconIndexDataSource = iconIndexDataSource;
      return this;
    }

    public IconListButton.Builder addIcon(Icon icon) {
      icons.add(icon);
      return this;
    }

    @Override
    protected void prepare() {
      super.prepare();

      if (iconIndexDataSource == null) {
        iconIndexDataSource = new DataSource<>() {
          private int value;

          @Override
          public Integer getValue() {
            return value;
          }

          @Override
          public void setValue(Integer value) {
            this.value = value;
          }
        };
      }
    }

    public IconListButton build() {
      prepare();
      IconListButton btn = new IconListButton(x, y, width, height, message, onTooltip);
      btn.iconIndex = iconIndexDataSource;
      btn.icons.addAll(icons);
      return btn;
    }
  }
}
