package com.codeka.justconduits.client.gui.conduittool;

import com.codeka.justconduits.JustConduitsMod;
import com.codeka.justconduits.client.gui.BaseScreen;
import com.codeka.justconduits.client.gui.widgets.TabButton;
import com.codeka.justconduits.client.gui.widgets.TabButtonRow;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.impl.ConduitType;
import com.codeka.justconduits.common.impl.NetworkType;
import com.codeka.justconduits.common.items.ConduitToolContainerMenu;
import com.codeka.justconduits.packets.ConduitToolStatePacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

public class ConduitToolScreen extends BaseScreen<ConduitToolContainerMenu> {
  private static final Logger L = LogManager.getLogger();

  public static final String NETWORK_INFO_STRING = "conduit_tool_screen.network_info";

  private final ResourceLocation BG =
      new ResourceLocation(JustConduitsMod.MODID, "textures/gui/conduit_tool.png");

  private final ConduitBlockEntity conduitBlockEntity;
  private final TabButtonRow conduitTabButtons = new TabButtonRow(TabButtonRow.TabPosition.TOP);
  public final ArrayList<NetworkType> networkTypes = new ArrayList<>();

  @Nullable
  private ConduitToolStatePacket lastPacket = null;

  private Object currState;
  private int lastIndex = -1;

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
      networkTypes.add(conduitType.getNetworkType());
    }
    conduitTabButtons.updateButtons(tabButtons);

    ConduitToolScreenPacketHandler.register(onPacketHandler);
  }

  @Override
  public void removed() {
    super.removed();

    ConduitToolScreenPacketHandler.unregister(onPacketHandler);
  }

  public <T extends GuiEventListener & NarratableEntry> void add(T eventListener) {
    super.addWidget(eventListener);
  }

  public void remove(GuiEventListener eventListener) {
    super.removeWidget(eventListener);
  }

  @Override
  protected void renderLabels(@Nonnull PoseStack matrixStack, int mouseX, int mouseY) {
    ConduitToolStatePacket.ConduitNetworkStatePacket networkStatePacket = getNetworkPacket();
    if (networkStatePacket == null) {
      return;
    }

    Component infoString =
        new TextComponent(I18n.get(NETWORK_INFO_STRING, Long.toString(networkStatePacket.getNetworkId())));
    drawString(matrixStack, Minecraft.getInstance().font, infoString, 10, 10, 0xffffffff);
  }

  @Override
  protected void renderBg(@Nonnull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
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
    IConduitToolScreenRenderer renderer = getRenderer();
    if (lastPacket != null && renderer != null) {
      renderer.render(lastPacket, this, poseStack, mouseX, mouseY, partialTick, currState);
    }
    renderTooltip(poseStack, mouseX, mouseY);
  }

  @Nullable
  private IConduitToolScreenRenderer getRenderer() {
    if (networkTypes == null) {
      return null;
    }

    int index = conduitTabButtons.getCurrentIndex();
    if (index < 0 || index >= networkTypes.size()) {
      return null;
    }

    IConduitToolScreenRenderer renderer = networkTypes.get(index).getConduitToolScreenRenderer();
    if (index != lastIndex) {
      if (lastIndex >= 0 && lastIndex < networkTypes.size()) {
        IConduitToolScreenRenderer oldRenderer = networkTypes.get(lastIndex).getConduitToolScreenRenderer();
        oldRenderer.close(currState);
      }

      if (renderer != null) {
        currState = renderer.init(this);
        lastIndex = index;
      }
    }

    return renderer;
  }


  @Nullable
  private ConduitToolStatePacket.ConduitNetworkStatePacket getNetworkPacket() {
    return lastPacket == null
        ? null
        : lastPacket.getNetworks().get(networkTypes.get(conduitTabButtons.getCurrentIndex()));
  }

  private final ConduitToolScreenPacketHandler.Handler onPacketHandler = (packet) -> {
    lastPacket = packet;
  };
}
