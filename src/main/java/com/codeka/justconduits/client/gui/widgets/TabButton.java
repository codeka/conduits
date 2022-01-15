package com.codeka.justconduits.client.gui.widgets;

import com.codeka.justconduits.JustConduitsMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

/** The button used to render tabs. */
public class TabButton extends Button {
  private static final Logger L = LogManager.getLogger();

  private final Icon icon;
  private final TabButtonRow tabButtonRow;
  static final int TAB_BUTTON_SIZE = 20;

  private static final ResourceLocation WIDGETS =
      new ResourceLocation(JustConduitsMod.MODID, "textures/gui/widgets.png");

  public TabButton(@Nonnull TabButtonRow tabButtonRow, @Nonnull Icon icon) {
    super(0, 0, TAB_BUTTON_SIZE, TAB_BUTTON_SIZE, new TextComponent(""), new OnPress() {
      @Override
      public void onPress(Button button) {
        ((TabButton) button).onPress(button);
      }
    }, new OnTooltip() {
      @Override
      public void onTooltip(Button button, PoseStack poseStack, int mouseX, int mouseY) {
        ((TabButton) button).onTooltip(button, poseStack, mouseX, mouseY);
      }
    });
    this.tabButtonRow = tabButtonRow;
    this.icon = icon;
  }

  /**
   * We take over rendering the button completely, so that we can customize some more of the features (for example, we
   * want to be able to show a 'pressed' state, render an icon or color instead of text, etc).
   */
  @Override
  public void renderButton(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderTexture(0, WIDGETS);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
    RenderSystem.enableDepthTest();

    int yOffset = (isHoveredOrFocused() ? 20 : 0);
    blit(poseStack, x, y, 0, 96, width, height);

    renderBg(poseStack, Minecraft.getInstance(), mouseX, mouseY);

    // Draw the icon.
    blit(
        poseStack,
        x + (width / 2 - Icon.WIDTH / 2), y + (height / 2 - Icon.HEIGHT / 2),
        icon.getX(), icon.getY(), Icon.WIDTH, Icon.HEIGHT);
  }

  private void onPress(Button button) {
    tabButtonRow.onTabButtonPressed(this);
  }

  private void onTooltip(Button button, PoseStack poseStack, int mouseX, int mouseY) {
    L.atInfo().log("onTooltip");
    // TODO: implement;
  }
}
