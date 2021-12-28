package com.codeka.justconduits.common.items;

import com.codeka.justconduits.common.ConduitCreativeModeTab;

/** The item for a Simple Item Conduit. */
public class SimpleItemConduitItem extends BaseConduitItem {
  private static final Properties PROPERTIES = new Properties();

  static {
    PROPERTIES.tab(ConduitCreativeModeTab.TAB_CONDUIT);
  }

  public SimpleItemConduitItem() {
    super(PROPERTIES);
  }
}
