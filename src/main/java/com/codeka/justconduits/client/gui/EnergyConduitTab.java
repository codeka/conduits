package com.codeka.justconduits.client.gui;

import com.codeka.justconduits.JustConduitsMod;
import com.codeka.justconduits.client.gui.widgets.ChannelColorButton;
import com.codeka.justconduits.client.gui.widgets.DataSource;
import com.codeka.justconduits.client.gui.widgets.Icon;
import com.codeka.justconduits.client.gui.widgets.IconListButton;
import com.codeka.justconduits.client.gui.widgets.SimpleButton;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.impl.ConduitType;
import com.codeka.justconduits.common.impl.NetworkType;
import com.codeka.justconduits.common.impl.energy.EnergyExternalConnection;
import com.codeka.justconduits.packets.ConduitUpdatePacket;
import com.codeka.justconduits.packets.JustConduitsPacketHandler;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;

public class EnergyConduitTab implements IConduitTab {
  private static final ResourceLocation BG =
      new ResourceLocation(JustConduitsMod.MODID, "textures/gui/conduit_basic.png");

  private ConduitScreen screen;
  private ConduitConnection connection;
  private ConduitBlockEntity conduitBlockEntity;
  private IconListButton insertModeButton;
  private IconListButton extractModeButton;

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
    EnergyExternalConnection externalConnection = connection.getNetworkExternalConnection(ConduitType.SIMPLE_ENERGY);
    insertModeButton.setIconIndex(externalConnection.getInsertMode().ordinal());

    extractModeButton =
        new IconListButton.Builder(100, 20)
            .withMessage(new TextComponent("Extract"))
            .addIcon(Icon.ALWAYS_OFF).addIcon(Icon.ALWAYS_ON).addIcon(Icon.REDSTONE_ON).addIcon(Icon.REDSTONE_OFF)
            .withIconIndexDataSource(extractDataSource)
            .build();
    extractModeButton.setIconIndex(externalConnection.getExtractMode().ordinal());
  }

  @Override
  public void show() {
    screen.add(insertModeButton);
    screen.add(extractModeButton);
  }

  @Override
  public void hide() {
    screen.remove(insertModeButton);
    screen.remove(extractModeButton);
  }

  @Override
  public void beforeRender() {
    EnergyExternalConnection externalConnection = connection.getNetworkExternalConnection(ConduitType.SIMPLE_ENERGY);
    extractModeButton.setIconIndex(externalConnection.getExtractMode().ordinal());
    insertModeButton.setIconIndex(externalConnection.getInsertMode().ordinal());
  }

  @Nonnull
  @Override
  public ResourceLocation getBackground() {
    return BG;
  }


  private final DataSource<Integer> extractDataSource = new DataSource<Integer>() {
    @Override
    public Integer getValue() {
      EnergyExternalConnection externalConnection = connection.getNetworkExternalConnection(ConduitType.SIMPLE_ENERGY);
      return externalConnection.getExtractMode().ordinal();
    }

    @Override
    public void setValue(Integer value) {
      if (conduitBlockEntity != null && connection != null) {
        sendPacketToServer(
            // TODO: make this generic.
            ConduitUpdatePacket.builder(conduitBlockEntity.getBlockPos(), NetworkType.ENERGY, connection.getDirection())
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

      EnergyExternalConnection externalConnection = connection.getNetworkExternalConnection(ConduitType.SIMPLE_ENERGY);
      return externalConnection.getInsertMode().ordinal();
    }

    @Override
    public void setValue(Integer value) {
      if (conduitBlockEntity != null && connection != null) {
        sendPacketToServer(
            ConduitUpdatePacket.builder(conduitBlockEntity.getBlockPos(), NetworkType.ENERGY, connection.getDirection())
                .withIntUpdate(ConduitUpdatePacket.UpdateType.INSERT_MODE, value)
                .build());
      }
    }
  };

  private void sendPacketToServer(ConduitUpdatePacket packet) {
    JustConduitsPacketHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(), packet);
  }
}
