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

import javax.annotation.Nonnull;

/** The button used to render tabs. */
public class TabButton extends Button {
  static final int TAB_BUTTON_SIZE = 20;

  private static final ResourceLocation WIDGETS =
      new ResourceLocation(JustConduitsMod.MODID, "textures/gui/widgets.png");

  public TabButton() {
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

    // TODO: draw icon
    drawCenteredString(
        poseStack, Minecraft.getInstance().font, new TextComponent("X"), x + width / 2, y + (height - 8) / 2,
        getFGColor() | Mth.ceil(alpha * 255.0F) << 24);
  }

  private void onPress(Button button) {
    // TODO: implement
  }

  private void onTooltip(Button button, PoseStack poseStack, int mouseX, int mouseY) {
    // TODO: implement;
  }
}
