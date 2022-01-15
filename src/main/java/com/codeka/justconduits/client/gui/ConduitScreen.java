package com.codeka.justconduits.client.gui;

import com.codeka.justconduits.JustConduitsMod;
import com.codeka.justconduits.client.gui.widgets.CheckButton;
import com.codeka.justconduits.client.gui.widgets.DataSource;
import com.codeka.justconduits.client.gui.widgets.TabButton;
import com.codeka.justconduits.client.gui.widgets.TabButtonRow;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.blocks.ConduitContainerMenu;
import com.codeka.justconduits.common.capabilities.network.ConduitType;
import com.codeka.justconduits.common.capabilities.network.NetworkType;
import com.codeka.justconduits.common.capabilities.network.item.ItemExternalConnection;
import com.codeka.justconduits.packets.ConduitUpdatePacket;
import com.codeka.justconduits.packets.JustConduitsPacketHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class ConduitScreen extends AbstractContainerScreen<ConduitContainerMenu> {
  private static final Logger L = LogManager.getLogger();

  private final ResourceLocation ITEM_GUI =
      new ResourceLocation(JustConduitsMod.MODID, "textures/gui/conduit_item.png");

  private final Component connectionName;
  private ConduitBlockEntity conduitBlockEntity;
  private final ConduitConnection connection;

  // The list of tabs we'll be displaying for this ConduitBlockEntity.
  private final ArrayList<IConduitTab> tabs = new ArrayList<>();

  // The current index of the tab we're rendering.
  private int tabIndex = -1;

  // The buttons used to switch tabs for the conduits.
  private final TabButtonRow conduitTabButtons = new TabButtonRow(TabButtonRow.TabPosition.TOP);

  public ConduitScreen(ConduitContainerMenu menu, Inventory playerInventory, Component title) {
    super(menu, playerInventory, title);
    imageWidth = 180;
    imageHeight = 190;

    connection = menu.getConnection();
    conduitBlockEntity = menu.getConduitBlockEntity();
    if (connection == null) {
      // Something happened...
      onClose();
      connectionName = new TextComponent("??");
    } else {
      connectionName = conduitBlockEntity.getConnectionName(connection);
    }
  }

  @Override
  protected void init() {
    super.init();

    ArrayList<TabButton> tabButtons = new ArrayList<>();
    for (ConduitType conduitType : conduitBlockEntity.getConduitTypes()) {
      IConduitTab tab = ConduitTabMapping.newTab(conduitType);
      tab.init(this, conduitBlockEntity, connection);
      tabs.add(tab);

      TabButton tabButton = new TabButton(conduitTabButtons, conduitType.getGuiIcon());
      // Note: we handle our own rendering, so just add the widget.
      addWidget(tabButton);
      tabButtons.add(tabButton);
    }
    conduitTabButtons.updateButtons(tabButtons);
  }

  public void add(AbstractWidget widget) {
    widget.x += leftPos;
    widget.y += topPos;
    super.addRenderableWidget(widget);
  }

  public void remove(AbstractWidget widget) {
    widget.x -= leftPos;
    widget.y -= topPos;
    super.removeWidget(widget);
  }

  @Override
  protected void renderBg(@Nonnull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
    conduitTabButtons.beforeWindowRender(poseStack, partialTick, mouseX, mouseY);

    RenderSystem.setShaderTexture(0, ITEM_GUI);
    int relX = (this.width - this.imageWidth) / 2;
    int relY = (this.height - this.imageHeight) / 2;

    blit(poseStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);

    conduitTabButtons.afterWindowRender(poseStack, partialTick, mouseX, mouseY);
  }

  @Override
  protected void renderLabels(@Nonnull PoseStack matrixStack, int mouseX, int mouseY) {
    drawString(matrixStack, Minecraft.getInstance().font, connectionName, 10, 10, 0xffffffff);
  }

  @Override
  public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
    int index = conduitTabButtons.getCurrentIndex();
    if (index >= 0 && index < tabs.size()) {
      if (index != tabIndex) {
        if (tabIndex >= 0) {
          tabs.get(tabIndex).hide();
        }
        tabIndex = index;
        tabs.get(tabIndex).show();
      }
      tabs.get(index).beforeRender();
    }
    conduitTabButtons.beforeRender(leftPos, topPos, width, height);

    renderBackground(poseStack);
    super.render(poseStack, mouseX, mouseY, partialTick);
    renderTooltip(poseStack, mouseX, mouseY);
  }
}
