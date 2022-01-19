package com.codeka.justconduits.client.gui.conduittool;

import com.codeka.justconduits.JustConduitsMod;
import com.codeka.justconduits.client.gui.widgets.TabButton;
import com.codeka.justconduits.client.gui.widgets.TabButtonRow;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.capabilities.network.ConduitType;
import com.codeka.justconduits.common.items.ConduitToolContainerMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

public class ConduitToolScreen extends AbstractContainerScreen<ConduitToolContainerMenu> {
  private static final Logger L = LogManager.getLogger();

  private final ResourceLocation BG =
      new ResourceLocation(JustConduitsMod.MODID, "textures/gui/conduit_tool.png");

  private final ConduitBlockEntity conduitBlockEntity;
  private final TabButtonRow conduitTabButtons = new TabButtonRow(TabButtonRow.TabPosition.TOP);

  public ConduitToolScreen(ConduitToolContainerMenu menu, Inventory playerInventory, Component title) {
    super(menu, playerInventory, title);

    imageWidth = 220;
    imageHeight = 190;
    conduitBlockEntity = menu.getConduitBlockEntity();
  }

  @Override
  protected void init() {
    super.init();

    ArrayList<TabButton> tabButtons = new ArrayList<>();
    for (ConduitType conduitType : conduitBlockEntity.getConduitTypes()) {
      TabButton tabButton = new TabButton(conduitTabButtons, conduitType.getGuiIcon());
      // Note: we handle our own rendering, so just add the widget.
      addWidget(tabButton);
      tabButtons.add(tabButton);
    }
    conduitTabButtons.updateButtons(tabButtons);

    ConduitToolScreenPacketHandler.register(onPacketHandler);
  }

  @Override
  public void removed() {
    super.removed();

    ConduitToolScreenPacketHandler.unregister(onPacketHandler);
  }

  @Override
  protected void renderBg(@Nullable PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
    conduitTabButtons.beforeWindowRender(poseStack, partialTick, mouseX, mouseY);

    RenderSystem.setShaderTexture(0, BG);
    int relX = (this.width - this.imageWidth) / 2;
    int relY = (this.height - this.imageHeight) / 2;

    blit(poseStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);

    conduitTabButtons.afterWindowRender(poseStack, partialTick, mouseX, mouseY);
  }

  @Override
  public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
    conduitTabButtons.beforeRender(leftPos, topPos, width, height);

    renderBackground(poseStack);
    super.render(poseStack, mouseX, mouseY, partialTick);
    renderTooltip(poseStack, mouseX, mouseY);
  }

  private final ConduitToolScreenPacketHandler.Handler onPacketHandler = (packet) -> {
    L.atInfo().log("got packet {}", packet);
  };
}
