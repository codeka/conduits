package com.codeka.justconduits.client.gui;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Implemented by each conduit's settings tab page.
 */
public interface IConduitTab {
  void init(
      @Nonnull ConduitScreen screen, @Nonnull ConduitBlockEntity blockEntity, @Nonnull ConduitConnection connection);

  void render();

  @Nonnull
  ResourceLocation getBackground();
}
