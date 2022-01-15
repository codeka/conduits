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

  /** Called when the tab is shown. We'll want to add our buttons and things to the {@link ConduitScreen}. */
  void show();

  /** Called when the tab is hidden. We'll want to remove our buttons and things from the {@link ConduitScreen}. */
  void hide();

  void beforeRender();

  @Nonnull
  ResourceLocation getBackground();
}
