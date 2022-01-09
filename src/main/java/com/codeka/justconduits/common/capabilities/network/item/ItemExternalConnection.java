package com.codeka.justconduits.common.capabilities.network.item;

import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.capabilities.network.common.CommonExternalConnection;

/**
 * Represents a connection between an item conduit network and an external inventory.
 */
public class ItemExternalConnection extends CommonExternalConnection {
  private ConduitConnection connection;

  // The number of ticks left until we do another extract operation.
  int ticksUntilNextExtract;
}
