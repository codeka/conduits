package com.codeka.justconduits.common;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ConduitCreativeModeTab extends CreativeModeTab {
  public static final CreativeModeTab TAB_CONDUIT = new ConduitCreativeModeTab();

  public ConduitCreativeModeTab() {
    super("Conduits");
  }

  @Override
  public ItemStack makeIcon() {
    return new ItemStack(ModItems.SIMPLE_ITEM_CONDUIT.get());
  }
}
