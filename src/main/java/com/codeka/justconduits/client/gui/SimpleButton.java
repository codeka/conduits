package com.codeka.justconduits.client.gui;

import com.codeka.justconduits.JustConduitsMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;

/** Base class for our buttons. */
public class SimpleButton extends Button {
  /** The default size (in pixels) of a button. */
  private static final int DEFAULT_BUTTON_SIZE = 20;

  private static final ResourceLocation WIDGETS =
      new ResourceLocation(JustConduitsMod.MODID, "textures/gui/widgets.png");

  private boolean isPressed;

  public SimpleButton(int x, int y, int width, int height, Component message, OnPress onPress, OnTooltip onTooltip) {
    super(x, y, width, height, message, onPress, onTooltip);
  }

  public void setPressed(boolean pressed) {
    isPressed = pressed;
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

    int yOffset = (isPressed ? 40 : 0) + (isHoveredOrFocused() ? 20 : 0);
    blit(poseStack, x, y, 0, yOffset, width / 2, height);
    blit(poseStack, x + width / 2, y, 200 - width / 2, yOffset, width / 2, height);

    renderBg(poseStack, Minecraft.getInstance(), mouseX, mouseY);
    renderIconOrText(poseStack);
  }

  protected void renderIconOrText(@Nonnull PoseStack poseStack) {
    drawCenteredString(
        poseStack, Minecraft.getInstance().font, getMessage(), x + width / 2, y + (height - 8) / 2,
        getFGColor() | Mth.ceil(alpha * 255.0F) << 24);
  }

  public static class Builder {
    protected final int x;
    protected final int y;
    protected final int width;
    protected final int height;
    protected Component message;
    protected OnPress onPress;
    protected OnTooltip onTooltip;

    private static final OnPress DEFAULT_ON_PRESS = button -> { };
    private static final OnTooltip DEFAULT_ON_TOOLTIP = (button, poseStack, mouseX, mouseY) -> { };

    /** Creates a builder for making a small square button. */
    public Builder(int x, int y) {
      this(x, y, DEFAULT_BUTTON_SIZE, DEFAULT_BUTTON_SIZE);
    }

    public Builder(int x, int y, int width, int height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
    }

    public Builder withMessage(Component message) {
      this.message = message;
      return this;
    }

    public Builder withOnPress(OnPress onPress) {
      this.onPress = onPress;
      return this;
    }

    public Builder withOnTooltip(OnTooltip onTooltip) {
      this.onTooltip = onTooltip;
      return this;
    }

    /** This should be called in the sub-class's {@link #build} method. */
    protected void prepare() {
      if (onPress == null) {
        onPress = DEFAULT_ON_PRESS;
      }
      if (onTooltip == null) {
        onTooltip = DEFAULT_ON_TOOLTIP;
      }
      if (message == null) {
        message = new TextComponent("");
      }
    }

    public SimpleButton build() {
      prepare();
      return new SimpleButton(x, y, width, height, message, onPress, onTooltip);
    }
  }
}
