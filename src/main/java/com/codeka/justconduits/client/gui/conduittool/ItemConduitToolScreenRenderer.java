package com.codeka.justconduits.client.gui.conduittool;

import com.codeka.justconduits.client.gui.widgets.ListWidget;
import com.codeka.justconduits.common.impl.NetworkType;
import com.codeka.justconduits.common.impl.item.ItemConduitToolExternalConnectionPacket;
import com.codeka.justconduits.packets.ConduitToolStatePacket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;

import javax.annotation.Nonnull;
import java.util.Locale;

public class ItemConduitToolScreenRenderer implements IConduitToolScreenRenderer {
  @Override
  public Object init(ConduitToolScreen screen) {
    State state = new State();
    state.screen = screen;
    state.itemRenderer = Minecraft.getInstance().getItemRenderer();
    state.itemsList = new ListWidget<>(
        /* x = */ 7 + screen.getGuiLeft(), /* y = */ 37 + screen.getGuiTop(), /* width = */ 207, /* height = */ 115,
        /* itemHeight = */ 23, this::renderItem);
    screen.add(state.itemsList);
    return state;
  }

  @Override
  public void close(Object stateObject) {
    if (stateObject instanceof State state) {
      state.screen.remove(state.itemsList);
    }
  }

  @Override
  public void render(
      @Nonnull ConduitToolStatePacket packet, @Nonnull GuiComponent gui, @Nonnull PoseStack poseStack, int mouseX,
      int mouseY, float partialTick, Object stateObject) {
    ConduitToolStatePacket.ConduitNetworkStatePacket itemPacket = packet.getNetworks().get(NetworkType.ITEM);
    if (itemPacket == null) {
      return;
    }

    if (stateObject instanceof State state) {
      ItemConduitToolExternalConnectionPacket itemConduitToolExternalConnectionPacket = itemPacket.getExternalPacket();
      render(gui, itemConduitToolExternalConnectionPacket, poseStack, mouseX, mouseY, partialTick, state);
    }
  }

  private void render(
      @Nonnull GuiComponent gui, @Nonnull ItemConduitToolExternalConnectionPacket packet, @Nonnull PoseStack poseStack,
      int mouseX, int mouseY, float partialTick, State state) {
    state.itemsList.setItems(packet.getExternalConnections());
    state.itemsList.render(gui, poseStack, mouseX, mouseY, partialTick);
  }

  private void renderItem(
      @Nonnull GuiComponent gui, int index, @Nonnull ItemConduitToolExternalConnectionPacket.ExternalConnection item,
      int x, int y, @Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {

    Minecraft.getInstance().getItemRenderer().renderGuiItem(item.getBlock().asItem().getDefaultInstance(), x, y);

    GuiComponent.drawString(poseStack, Minecraft.getInstance().font, item.getBlockName(), x + 20, y, 0xffffffff);
    String rate =
        String.format(
            Locale.getDefault(), "E: %.1f I: %.1f items/s", item.getNumExtractedPerTick(),
            item.getNumInsertedPerTick());
    GuiComponent.drawString(poseStack, Minecraft.getInstance().font, rate, x + 20, y + 12, 0xffffffff);
  }

  private static final class State {
    public ConduitToolScreen screen;
    public ItemRenderer itemRenderer;
    public ListWidget<ItemConduitToolExternalConnectionPacket.ExternalConnection> itemsList;
  }
}
