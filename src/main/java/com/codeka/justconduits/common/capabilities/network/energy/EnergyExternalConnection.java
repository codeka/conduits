package com.codeka.justconduits.common.capabilities.network.energy;

import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.capabilities.network.common.CommonExternalConnection;

public class EnergyExternalConnection extends CommonExternalConnection {
  private ConduitConnection connection;

  // The number of ticks left until we do another extract operation.
  int ticksUntilNextExtract;
}
