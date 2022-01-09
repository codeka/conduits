package com.codeka.justconduits.common.capabilities.network.item;

import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.capabilities.network.ConnectionMode;
import com.codeka.justconduits.common.capabilities.network.NetworkExternalConnection;

/**
 * Represents a connection between an item conduit network and an external inventory.
 */
public class ItemExternalConnection extends NetworkExternalConnection {
  private ConnectionMode extractMode = ConnectionMode.ALWAYS_OFF;
  private ConnectionMode insertMode = ConnectionMode.ALWAYS_OFF;

  private ConduitConnection connection;

  // The number of ticks left until we do another extract operation.
  int ticksUntilNextExtract;

  public ConnectionMode getExtractMode() {
    return extractMode;
  }

  public void setExtractMode(ConnectionMode value) {
    extractMode = value;
  }

  public ConnectionMode getInsertMode() {
    return insertMode;
  }

  public void setInsertMode(ConnectionMode value) {
    insertMode = value;
  }
}
