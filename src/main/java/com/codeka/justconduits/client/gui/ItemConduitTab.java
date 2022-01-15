package com.codeka.justconduits.client.gui;

import com.codeka.justconduits.JustConduitsMod;
import com.codeka.justconduits.client.gui.widgets.ChannelColorButton;
import com.codeka.justconduits.client.gui.widgets.DataSource;
import com.codeka.justconduits.client.gui.widgets.Icon;
import com.codeka.justconduits.client.gui.widgets.IconListButton;
import com.codeka.justconduits.client.gui.widgets.SimpleButton;
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

  private ConduitScreen screen;
  private ConduitConnection connection;
  private ConduitBlockEntity conduitBlockEntity;
  private IconListButton insertModeButton;
  private IconListButton extractModeButton;
  private ChannelColorButton insertChannelColorButton;
  private ChannelColorButton extractChannelColorButton;

  private SimpleButton testButton1;
  private SimpleButton testButton2;

  @Override
  public void init(
      @Nonnull ConduitScreen screen, @Nonnull ConduitBlockEntity conduitBlockEntity,
      @Nonnull ConduitConnection connection) {
    this.screen = screen;
    this.conduitBlockEntity = conduitBlockEntity;
    this.connection = connection;

    insertModeButton =
        new IconListButton.Builder(10, 20)
            .withMessage(new TextComponent("Insert"))
            .addIcon(Icon.ALWAYS_OFF).addIcon(Icon.ALWAYS_ON).addIcon(Icon.REDSTONE_ON).addIcon(Icon.REDSTONE_OFF)
            .withIconIndexDataSource(insertDataSource)
            .build();
    // TODO: make this generic?
    ItemExternalConnection externalConnection = connection.getNetworkExternalConnection(ConduitType.SIMPLE_ITEM);
    insertModeButton.setIconIndex(externalConnection.getInsertMode().ordinal());

    insertChannelColorButton = new ChannelColorButton.Builder(10, 45).build();

    testButton1 = new SimpleButton.Builder(35, 45).build();
    testButton2 = new SimpleButton.Builder(60, 45).build();

    extractModeButton =
        new IconListButton.Builder(100, 20)
            .withMessage(new TextComponent("Extract"))
            .addIcon(Icon.ALWAYS_OFF).addIcon(Icon.ALWAYS_ON).addIcon(Icon.REDSTONE_ON).addIcon(Icon.REDSTONE_OFF)
            .withIconIndexDataSource(extractDataSource)
            .build();
    extractModeButton.setIconIndex(externalConnection.getExtractMode().ordinal());

    extractChannelColorButton = new ChannelColorButton.Builder(100, 45).build();
  }

  @Override
  public void show() {
    screen.add(insertModeButton);
    screen.add(insertChannelColorButton);
    screen.add(testButton1);
    screen.add(testButton2);
    screen.add(extractModeButton);
    screen.add(extractChannelColorButton);
  }

  @Override
  public void hide() {
    screen.remove(insertModeButton);
    screen.remove(insertChannelColorButton);
    screen.remove(testButton1);
    screen.remove(testButton2);
    screen.remove(extractModeButton);
    screen.remove(extractChannelColorButton);
  }

  @Override
  public void beforeRender() {
    ItemExternalConnection externalConnection = connection.getNetworkExternalConnection(ConduitType.SIMPLE_ITEM);
    extractModeButton.setIconIndex(externalConnection.getExtractMode().ordinal());
    insertModeButton.setIconIndex(externalConnection.getInsertMode().ordinal());
  }

  @Nonnull
  @Override
  public ResourceLocation getBackground() {
    return ITEM_GUI;
  }


  private final DataSource<Integer> extractDataSource = new DataSource<Integer>() {
    @Override
    public Integer getValue() {
      ItemExternalConnection externalConnection = connection.getNetworkExternalConnection(ConduitType.SIMPLE_ITEM);
      return externalConnection.getExtractMode().ordinal();
    }

    @Override
    public void setValue(Integer value) {
      if (conduitBlockEntity != null && connection != null) {
        sendPacketToServer(
            // TODO: make this generic.
            ConduitUpdatePacket.builder(conduitBlockEntity.getBlockPos(), NetworkType.ITEM, connection.getDirection())
                .withIntUpdate(ConduitUpdatePacket.UpdateType.EXTRACT_MODE, value)
                .build());
      }
    }
  };

  private final DataSource<Integer> insertDataSource = new DataSource<>() {
    @Override
    public Integer getValue() {
      if (connection == null) {
        return 0;
      }

      ItemExternalConnection externalConnection = connection.getNetworkExternalConnection(ConduitType.SIMPLE_ITEM);
      return externalConnection.getInsertMode().ordinal();
    }

    @Override
    public void setValue(Integer value) {
      if (conduitBlockEntity != null && connection != null) {
        sendPacketToServer(
            ConduitUpdatePacket.builder(conduitBlockEntity.getBlockPos(), NetworkType.ITEM, connection.getDirection())
                .withIntUpdate(ConduitUpdatePacket.UpdateType.INSERT_MODE, value)
                .build());
      }
    }
  };

  private void sendPacketToServer(ConduitUpdatePacket packet) {
    JustConduitsPacketHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(), packet);
  }
}
