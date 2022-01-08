package com.codeka.justconduits.client.blocks;

import com.codeka.justconduits.JustConduitsMod;
import com.codeka.justconduits.client.gui.CheckButton;
import com.codeka.justconduits.client.gui.DataSource;
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
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.network.PacketDistributor;
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
  private ConduitBlockEntity conduitBlockEntity;
  @Nullable
  private final ConduitConnection connection;

  private CheckButton insertCheckButton;
  private CheckButton extractCheckButton;

  public ConduitScreen(ConduitContainerMenu menu, Inventory playerInventory, Component title) {
    super(menu, playerInventory, title);
    imageWidth = 180;
    imageHeight = 190;

    connection = menu.getConnection();
    conduitBlockEntity = menu.getConduitBlockEntity();
    if (connection != null && conduitBlockEntity != null) {
      connectionName = conduitBlockEntity.getConnectionName(connection);
    } else {
      connectionName = new TextComponent("??");
    }
  }

  @Override
  protected void init() {
    super.init();
    insertCheckButton =
        new CheckButton.Builder(leftPos + 10, topPos + 20)
            .withMessage(new TextComponent("Insert"))
            .withCheckedDataSource(insertDataSource)
            .build();
    if (connection != null) {
      // TODO: make this generic
      ItemExternalConnection externalConnection = connection.getNetworkExternalConnection(ConduitType.SIMPLE_ITEM);
      insertCheckButton.setChecked(externalConnection.isInsertEnabled());
    }
    addRenderableWidget(insertCheckButton);

    extractCheckButton =
        new CheckButton.Builder(leftPos + 100, topPos + 20)
            .withMessage(new TextComponent("Extract"))
            .withCheckedDataSource(extractDataSource)
            .build();
    if (connection != null) {
      // TODO: make this generic
      ItemExternalConnection externalConnection = connection.getNetworkExternalConnection(ConduitType.SIMPLE_ITEM);
      extractCheckButton.setChecked(externalConnection.isExtractEnabled());
    }
    addRenderableWidget(extractCheckButton);
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
    // TODO: this is weird, do we really need to?
    if (connection != null) {
      // TODO: make this generic
      ItemExternalConnection externalConnection = connection.getNetworkExternalConnection(ConduitType.SIMPLE_ITEM);
      extractCheckButton.setChecked(externalConnection.isExtractEnabled());
      insertCheckButton.setChecked(externalConnection.isInsertEnabled());
    }

    renderBackground(poseStack);
    super.render(poseStack, mouseX, mouseY, partialTick);
    renderTooltip(poseStack, mouseX, mouseY);
  }

  private final DataSource<Boolean> extractDataSource = new DataSource<>() {
    @Override
    public Boolean getValue() {
      if (connection == null) {
        return false;
      }
      ItemExternalConnection externalConnection = connection.getNetworkExternalConnection(ConduitType.SIMPLE_ITEM);
      return externalConnection.isExtractEnabled();
    }

    @Override
    public void setValue(Boolean value) {
      if (conduitBlockEntity != null && connection != null) {
        sendPacketToServer(
            // TODO: make this generic.
            ConduitUpdatePacket.builder(conduitBlockEntity.getBlockPos(), NetworkType.ITEM, connection.getDirection())
                .withBooleanUpdate(ConduitUpdatePacket.UpdateType.EXTRACT_ENABLED, value)
                .build());
      }
    }
  };

  private final DataSource<Boolean> insertDataSource = new DataSource<>() {
    @Override
    public Boolean getValue() {
      if (connection == null) {
        return false;
      }

      ItemExternalConnection externalConnection = connection.getNetworkExternalConnection(ConduitType.SIMPLE_ITEM);
      return externalConnection.isInsertEnabled();
    }

    @Override
    public void setValue(Boolean value) {
      if (conduitBlockEntity != null && connection != null) {
        sendPacketToServer(
            ConduitUpdatePacket.builder(conduitBlockEntity.getBlockPos(), NetworkType.ITEM, connection.getDirection())
                .withBooleanUpdate(ConduitUpdatePacket.UpdateType.INSERT_ENABLED, value)
                .build());
      }
    }
  };

  private void sendPacketToServer(ConduitUpdatePacket packet) {
    JustConduitsPacketHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(), packet);
  }
}
