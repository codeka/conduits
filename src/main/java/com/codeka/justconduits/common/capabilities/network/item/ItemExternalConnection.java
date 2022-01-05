package com.codeka.justconduits.common.capabilities.network.item;

import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.capabilities.network.NetworkExternalConnection;

/**
 * Represents a connection between an item conduit network and an external inventory.
 */
public class ItemExternalConnection extends NetworkExternalConnection {
  private boolean extractEnabled;
  private boolean insertEnabled;

  private ConduitConnection connection;

  // The number of ticks left until we do another extract operation.
  int ticksUntilNextExtract;

  public boolean isExtractEnabled() {
    return extractEnabled;
  }

  public void setExtractEnabled(boolean value) {
    extractEnabled = value;
  }

  public boolean isInsertEnabled() {
    return insertEnabled;
  }

  public void setInsertEnabled(boolean value) {
    insertEnabled = value;
  }
}
