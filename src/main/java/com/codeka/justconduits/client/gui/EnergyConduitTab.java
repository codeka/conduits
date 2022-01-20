package com.codeka.justconduits.client.gui;

import com.codeka.justconduits.JustConduitsMod;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class EnergyConduitTab implements IConduitTab {
  private static final ResourceLocation BG =
      new ResourceLocation(JustConduitsMod.MODID, "textures/gui/conduit_fluid.png");

  @Override
  public void init(
      @Nonnull ConduitScreen screen, @Nonnull ConduitBlockEntity conduitBlockEntity,
      @Nonnull ConduitConnection connection) {
  }

  @Override
  public void show() {
  }

  @Override
  public void hide() {
  }

  @Override
  public void beforeRender() {
  }

  @Nonnull
  @Override
  public ResourceLocation getBackground() {
    return BG;
  }
}
