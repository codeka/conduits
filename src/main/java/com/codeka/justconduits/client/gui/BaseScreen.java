package com.codeka.justconduits.client.gui;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base class for screens.
 *
 * <p>For some reason, AbstractContainerMenu does not call it's super class implementations of {@link #mouseDragged}
 * and {@link #mouseReleased}, so when we try to override them in widgets, the widget implementations never get called.
 * So we have to re-implement it.
 */
public abstract class BaseScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
  private static final Logger L = LogManager.getLogger();

  public BaseScreen(T menu, Inventory playerInventory, Component title) {
    super(menu, playerInventory, title);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (getChildAt(mouseX, mouseY).filter((child) -> {
      if (child.mouseClicked(mouseX, mouseY, button)) {
        setFocused(child);
        if (button == 0) {
          setDragging(true);
        }
        return true;
      }
      return false;
    }).isPresent()) {
      return true;
    }

    return super.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
    GuiEventListener focused = getFocused();
    if (focused != null && isDragging() && button == 0 && focused.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
      return true;
    }

    return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    setDragging(false);
    if (getChildAt(mouseX, mouseY).filter((child) -> child.mouseReleased(mouseX, mouseY, button)).isPresent()) {
      return true;
    }

    return super.mouseReleased(mouseX, mouseY, button);
  }
}
