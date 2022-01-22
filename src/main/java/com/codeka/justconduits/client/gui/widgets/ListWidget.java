package com.codeka.justconduits.client.gui.widgets;

import com.codeka.justconduits.JustConduitsMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * This is a helper class for rendering lists. The actual rendering of the list is custom, but this class will handle
 * scrolling, drawing the scroll handle and so on.
 *
 * @param <T> The type of the items in each list.
 */
public class ListWidget<T> extends AbstractWidget {
  private static final Logger L = LogManager.getLogger();

  /** Interface you need to implement to draw a single row of the list. */
  public interface ListItemRenderer<T> {
    void renderItem(
        @Nonnull GuiComponent gui, int index, @Nonnull T item, int x, int y, @Nonnull PoseStack poseStack, int mouseX,
        int mouseY, float partialTicks);
  }

  private static final ResourceLocation WIDGETS =
      new ResourceLocation(JustConduitsMod.MODID, "textures/gui/widgets.png");

  private final int left;
  private final int top;
  private final int width;
  private final int height;
  private final int itemHeight;
  private final ListItemRenderer<T> itemRenderer;

  private List<T> items;
  private double thumbOffsetY = 0.0;
  private int topIndex = 0;

  public ListWidget(int x, int y, int width, int height, int itemHeight, @Nonnull ListItemRenderer<T> itemRenderer) {
    super(x, y, width, height, /* TODO: is this OK? */ new TextComponent("ListWidget"));
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
    for (int index = topIndex; index < items.size() && index < (topIndex + numVisibleItems); index++) {
      T item = items.get(index);
      if (item == null) {
        continue;
      }
      itemRenderer.renderItem(gui, index, item, left, dy, poseStack, mouseX, mouseY, partialTicks);
      dy += itemHeight;
    }

    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderTexture(0, WIDGETS);
    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
    RenderSystem.enableDepthTest();

    gui.blit(
        poseStack,
        left + width - 13, top + (int) thumbOffsetY,
        /* x offset in widgets.png */ 22, /* y offset in widgets.png */99,
        12, 15);
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

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    int newIndex = topIndex - (int) delta;
    if (newIndex < 0) {
      newIndex = 0;
    }
    if (newIndex > getMaxTopIndex()) {
      newIndex = getMaxTopIndex();
    }
    topIndex = newIndex;

    final double percent = topIndex / (double) getMaxTopIndex();
    thumbOffsetY = (height - 15.0) * percent;

    return super.mouseScrolled(mouseX, mouseY, delta);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (!isMouseOver(mouseX, mouseY)) {
      return false;
    }

    if (mouseX > left + width - 13) {
      // The click was within the column of the scrollbar. We'll want to get notified of drags here.
      updateThumbPosition(mouseY);
      return true;
    }

    return false;
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
    updateThumbPosition(mouseY);
    return true;
  }

  private void updateThumbPosition(double mouseY) {
    double y = mouseY - top - 7.0; // 6 is half the height of the thumb.
    if (y < 0 || y >= height - 15.0) {
      return;
    }
    thumbOffsetY = y;

    final double percent = y / (height - 15.0);
    topIndex = (int) Math.floor((getMaxTopIndex() + 1) * percent);
  }

  @Override
  public void updateNarration(@Nonnull NarrationElementOutput narrationElementOutput) {
    // TODO: narration.
  }
}
