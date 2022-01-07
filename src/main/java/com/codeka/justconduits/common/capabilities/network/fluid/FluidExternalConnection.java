package com.codeka.justconduits.common.capabilities.network.fluid;

import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.capabilities.network.NetworkExternalConnection;

public class FluidExternalConnection extends NetworkExternalConnection {
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
