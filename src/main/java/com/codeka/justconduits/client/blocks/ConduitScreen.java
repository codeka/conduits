package com.codeka.justconduits.client.blocks;

import com.codeka.justconduits.JustConduitsMod;
import com.codeka.justconduits.client.gui.CheckButton;
import com.codeka.justconduits.client.gui.SimpleButton;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.blocks.ConduitContainerMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConduitScreen extends AbstractContainerScreen<ConduitContainerMenu> {
  private static final Logger L = LogManager.getLogger();

  private final ResourceLocation ITEM_GUI =
      new ResourceLocation(JustConduitsMod.MODID, "textures/gui/conduit_item.png");

  private final Component connectionName;
  @Nullable
  private final ConduitConnection connection;

  public ConduitScreen(ConduitContainerMenu menu, Inventory playerInventory, Component title) {
    super(menu, playerInventory, title);
    imageWidth = 180;
    imageHeight = 190;

    connection = menu.getConnection();
    ConduitBlockEntity conduitBlockEntity = menu.getConduitBlockEntity();
    if (connection != null && conduitBlockEntity != null) {
      connectionName = conduitBlockEntity.getConnectionName(connection);
    } else {
      connectionName = new TextComponent("??");
    }
  }

  @Override
  protected void init() {
    super.init();
    CheckButton btn = new CheckButton.Builder(leftPos + 10, topPos + 20).withMessage(new TextComponent("Insert")).build();
    addRenderableWidget(btn);

    btn = new CheckButton.Builder(leftPos + 100, topPos + 20).withMessage(new TextComponent("Extract")).build();
    addRenderableWidget(btn);
  }

  @Override
  protected void renderBg(@Nonnull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
    RenderSystem.setShaderTexture(0, ITEM_GUI);
    int relX = (this.width - this.imageWidth) / 2;
    int relY = (this.height - this.imageHeight) / 2;

    blit(poseStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
  }

  @Override
  protected void renderLabels(@Nonnull PoseStack matrixStack, int mouseX, int mouseY) {
    drawString(matrixStack, Minecraft.getInstance().font, connectionName, 10, 10, 0xffffffff);
  }

  @Override
  public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
    renderBackground(poseStack);
    super.render(poseStack, mouseX, mouseY, partialTick);
    renderTooltip(poseStack, mouseX, mouseY);
  }
}