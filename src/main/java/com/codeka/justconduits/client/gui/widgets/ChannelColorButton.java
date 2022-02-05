package com.codeka.justconduits.client.gui.widgets;

import com.codeka.justconduits.common.ChannelColor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/** A button that lets you cycle between the {@link ChannelColor}s by clicking the button. */
public class ChannelColorButton extends SimpleButton {
  private static final Logger L = LogManager.getLogger();
  private DataSource<ChannelColor> color;

  public ChannelColorButton(int x, int y, int width, int height, Component message, OnTooltip onTooltip) {
    super(x, y, width, height, message, (btn) -> ((ChannelColorButton) btn).handlePress(), onTooltip);
  }

  private void handlePress() {
    color.setValue(color.getValue().nextColor());
    L.atInfo().log("color = {}", color.getValue().getDyeColor().getName());
  }

  @Override
  public boolean onRightPress() {
    color.setValue(color.getValue().prevColor());
    return true;
  }

  @Override
  protected void renderIconOrText(@NotNull PoseStack poseStack) {
    // TODO: we kind of just arbitrarily pick TextureDiffuseColors for this. Maybe we can hard-code our own that look
    //  better? Either way, this is OK for now.
    float[] textureColor = color.getValue().getDyeColor().getTextureDiffuseColors();
    int finalColor = 0xff000000 |
        (int) (textureColor[0] * 255.0f) << 16 |
        (int) (textureColor[1] * 255.0f) << 8 |
        (int) (textureColor[2] * 255.0f);
    fill(poseStack, x + 3, y + 3, x + width - 3, y + height - 3, finalColor);

    // TODO: maybe we don't always need to render this thing?
    Font font = Minecraft.getInstance().font;
    drawString(
        poseStack, Minecraft.getInstance().font, getMessage(),
        x + width + 3, y + (height / 2 - font.lineHeight / 2),
        getFGColor());
  }

  public static class Builder extends SimpleButton.Builder<ChannelColorButton.Builder> {
    private DataSource<ChannelColor> color;

    public Builder(int x, int y) {
      super(x, y);
    }

    public Builder withColor(DataSource<ChannelColor> color) {
      this.color = color;
      return this;
    }

    @Override
    protected void prepare() {
      super.prepare();
      if (color == null) {
        color = new DataSource<>() {
          private ChannelColor value = ChannelColor.BLUE;

          @Override
          public ChannelColor getValue() {
            return value;
          }

          @Override
          public void setValue(ChannelColor value) {
            this.value = value;
          }
        };
      }
    }

    public ChannelColorButton build() {
      prepare();
      ChannelColorButton btn = new ChannelColorButton(x, y, width, height, message, onTooltip);
      btn.color = color;
      return btn;
    }
  }
}
