package com.codeka.justconduits.common.items;

import com.codeka.justconduits.common.ConduitCreativeModeTab;
import com.codeka.justconduits.common.capabilities.network.ConduitType;

/** The item for a Simple Fluid Conduit. */
public class SimpleFluidConduitItem extends BaseConduitItem {
  private static final Properties PROPERTIES = new Properties();

  static {
    PROPERTIES.tab(ConduitCreativeModeTab.TAB_CONDUIT);
  }

  public SimpleFluidConduitItem() {
    super(PROPERTIES);
  }

  @Override
  protected ConduitType getConduitType() {
    return ConduitType.SIMPLE_FLUID;
  }
}
