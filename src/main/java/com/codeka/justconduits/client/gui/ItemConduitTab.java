package com.codeka.justconduits.client.gui;

import com.codeka.justconduits.JustConduitsMod;
import com.codeka.justconduits.client.gui.widgets.ChannelColorButton;
import com.codeka.justconduits.client.gui.widgets.CheckButton;
import com.codeka.justconduits.client.gui.widgets.DataSource;
import com.codeka.justconduits.client.gui.widgets.SimpleButton;
import com.codeka.justconduits.common.ChannelColor;
import com.codeka.justconduits.common.blocks.ConduitBlock;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.capabilities.network.ConduitType;
import com.codeka.justconduits.common.capabilities.network.NetworkType;
import com.codeka.justconduits.common.capabilities.network.item.ItemExternalConnection;
import com.codeka.justconduits.packets.ConduitUpdatePacket;
import com.codeka.justconduits.packets.JustConduitsPacketHandler;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;

public class ItemConduitTab implements IConduitTab {
  private static final ResourceLocation ITEM_GUI =
      new ResourceLocation(JustConduitsMod.MODID, "textures/gui/conduit_item.png");

  private ConduitConnection connection;
  private ConduitBlockEntity conduitBlockEntity;
  private CheckButton insertCheckButton;
  private CheckButton extractCheckButton;
  private ChannelColorButton insertChannelColorButton;
  private ChannelColorButton extractChannelColorButton;

  private SimpleButton testButton1;
  private SimpleButton testButton2;

  @Override
  public void init(
      @Nonnull ConduitScreen screen, @Nonnull ConduitBlockEntity conduitBlockEntity,
      @Nonnull ConduitConnection connection) {
    this.conduitBlockEntity = conduitBlockEntity;
    this.connection = connection;

    insertCheckButton =
        new CheckButton.Builder(10, 20)
            .withMessage(new TextComponent("Insert"))
            .withCheckedDataSource(insertDataSource)
            .build();
    // TODO: make this generic?
    ItemExternalConnection externalConnection = connection.getNetworkExternalConnection(ConduitType.SIMPLE_ITEM);
    insertCheckButton.setChecked(externalConnection.isInsertEnabled());
    screen.add(insertCheckButton);

    insertChannelColorButton = new ChannelColorButton.Builder(10, 45).build();
    screen.add(insertChannelColorButton);

    testButton1 = new SimpleButton.Builder(35, 45).build();
    screen.add(testButton1);
    testButton2 = new SimpleButton.Builder(60, 45).build();
    screen.add(testButton2);

    extractCheckButton =
        new CheckButton.Builder(100, 20)
            .withMessage(new TextComponent("Extract"))
            .withCheckedDataSource(extractDataSource)
            .build();
    extractCheckButton.setChecked(externalConnection.isExtractEnabled());
    screen.add(extractCheckButton);

    extractChannelColorButton = new ChannelColorButton.Builder(100, 45).build();
    screen.add(extractChannelColorButton);

  }

  @Override
  public void render() {
    ItemExternalConnection externalConnection = connection.getNetworkExternalConnection(ConduitType.SIMPLE_ITEM);
    extractCheckButton.setChecked(externalConnection.isExtractEnabled());
    insertCheckButton.setChecked(externalConnection.isInsertEnabled());
  }

  @Nonnull
  @Override
  public ResourceLocation getBackground() {
    return ITEM_GUI;
  }


  private final DataSource<Boolean> extractDataSource = new DataSource<>() {
    @Override
    public Boolean getValue() {
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
