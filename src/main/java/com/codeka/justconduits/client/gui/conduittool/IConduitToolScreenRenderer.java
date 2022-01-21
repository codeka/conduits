package com.codeka.justconduits.client.gui.conduittool;

import com.codeka.justconduits.packets.ConduitToolStatePacket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;

import javax.annotation.Nonnull;

/** Interface implemented by individual conduit networks that want to render stuff on the conduit tool screen. */
public interface IConduitToolScreenRenderer {
  /**
   * Initializes the renderer.
   *
   * @param leftPos The leftPos of the screen we're rendering inside.
   * @param topPos  The topPos of the screen we're rendering inside.
   * @return A context object that we'll pass back to your {@link #render} method where you can keep various bits of
   *   state.
   */
  Object init(int leftPos, int topPos);

  /**
   * Renders your screen.
   *
   * @param packet The last {@link ConduitToolStatePacket} that was received.
   * @param gui The {@link GuiComponent} that will do the actual rendering.
   * @param poseStack The {@link PoseStack} for rendering.
   * @param mouseX Current mouse X position.
   * @param mouseY Current mouse y position.
   * @param partialTick Partial ticks.
   * @param state The state object that you returned from {@link #init}.
   */
  void render(
      @Nonnull ConduitToolStatePacket packet, GuiComponent gui, @Nonnull PoseStack poseStack, int mouseX, int mouseY,
      float partialTick, Object state);
}
