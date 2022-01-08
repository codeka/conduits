package com.codeka.justconduits.client.gui;

import com.codeka.justconduits.JustConduitsMod;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class FluidConduitTab implements IConduitTab {
  private static final ResourceLocation FLUID_GUI =
      new ResourceLocation(JustConduitsMod.MODID, "textures/gui/conduit_fluid.png");

  @Override
  public void init(
      @Nonnull ConduitScreen screen, @Nonnull ConduitBlockEntity blockEntity, @Nonnull ConduitConnection connection) {
  }

  @Override
  public void render() {

  }

  @Nonnull
  @Override
  public ResourceLocation getBackground() {
    return FLUID_GUI;
  }
}
