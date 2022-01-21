package com.codeka.justconduits.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * This is a helper class for rendering lists. The actual rendering of the list is custom, but this class will handle
 * scrolling, drawing the scroll handle and so on.
 *
 * @param <T> The type of the items in each list.
 */
public class ListWidget<T> {
  /** Interface you need to implement to draw a single row of the list. */
  public interface ListItemRenderer<T> {
    void renderItem(
        @Nonnull GuiComponent gui, int index, @Nonnull T item, int x, int y, @Nonnull PoseStack poseStack, int mouseX,
        int mouseY, float partialTicks);
  }

  private final int left;
  private final int top;
  private final int width;
  private final int height;
  private final int itemHeight;
  private final ListItemRenderer<T> itemRenderer;

  private List<T> items;
  private int topIndex = 0;

  public ListWidget(int x, int y, int width, int height, int itemHeight, @Nonnull ListItemRenderer<T> itemRenderer) {
    this.left = x;
    this.top = y;
    this.width = width;
    this.height = height;
    this.itemHeight = itemHeight;
    this.itemRenderer = itemRenderer;
  }

  public void setItems(List<T> items) {
    this.items = items;
  }

  public void render(
      @Nonnull GuiComponent gui, @Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
    if (items == null || items.isEmpty()) {
      return;
    }

    int numVisibleItems = getNumVisibleItems();
    int maxTopIndex = getMaxTopIndex();
    if (topIndex > maxTopIndex) {
      topIndex = maxTopIndex;
    }

    int dy = top;
    for (int index = topIndex; index < items.size() && index <= (maxTopIndex + numVisibleItems); index++) {
      T item = items.get(index);
      if (item == null) {
        continue;
      }
      itemRenderer.renderItem(gui, index, item, left, dy, poseStack, mouseX, mouseY, partialTicks);
      dy += itemHeight;
    }
  }

  private int getNumVisibleItems() {
    return height / itemHeight;
  }

  /** Gets the maximum index we can scroll to so that all items are visible. */
  public int getMaxTopIndex() {
    int numVisibleItems = getNumVisibleItems();
    if (items.size() <= numVisibleItems) {
      return 0;
    }

    return items.size() - numVisibleItems;
  }
}
