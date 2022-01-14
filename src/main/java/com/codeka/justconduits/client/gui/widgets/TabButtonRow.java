package com.codeka.justconduits.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a row of {@link TabButton}s.
 *
 * The tab buttons are attached to the side of the window (which side is represented by {@link TabPosition}), and only
 * one button is "selected" or "active" at once.
 */
public class TabButtonRow {
  /** The position of the tab button on the window. */
  public enum TabPosition {
    /** The buttons are attached to the top of the window. */
    TOP,
    /** The buttons are attached to the left side of the window. */
    LEFT,
  }

  private final TabPosition tabPosition;
  private final ArrayList<TabButton> buttons;
  private int currIndex;

  public TabButtonRow(TabPosition tabPosition) {
    this.tabPosition = tabPosition;
    this.buttons = new ArrayList<>();
    currIndex = -1;
  }

  /** Replaces the buttons we render with the given collection. */
  public void updateButtons(Collection<TabButton> buttons) {
    this.buttons.clear();;
    this.buttons.addAll(buttons);
    this.currIndex = buttons.size() > 0 ? 0 : -1;
  }

  public int getCurrentIndex() {
    return currIndex;
  }

  public void setCurrentIndex(int index) {
    if (index >= 0 && buttons.size() < index) {
      currIndex = index;
    }
  }

  public void beforeRender(int windowX, int windowY, int windowWidth, int windowHeight) {
    // We need to update the buttons so they are in the right position on screen.
    int x = windowX;
    int y = windowY - TabButton.TAB_BUTTON_SIZE + 2;
    for (TabButton btn : buttons) {
      btn.x = x;
      btn.y = y;
      x += TabButton.TAB_BUTTON_SIZE;
    }
  }

  /**
   * Called before the window's background is rendered. We render the non-active buttons so they appear "behind"
   * the window.
   */
  public void beforeWindowRender(@Nonnull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
    for (int i = 0; i < buttons.size(); i++) {
      if (i == currIndex) {
        continue;
      }

      buttons.get(i).renderButton(poseStack, mouseX, mouseY, partialTick);
    }

  }

  /**
   * Called after the window's background is rendered. We render the active button so that it appears "above" the
   * window.
   */
  public void afterWindowRender(@Nonnull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
    if (currIndex >= 0 && currIndex < buttons.size()) {
      buttons.get(currIndex).renderButton(poseStack, mouseX, mouseY, partialTick);
    }
  }
}
