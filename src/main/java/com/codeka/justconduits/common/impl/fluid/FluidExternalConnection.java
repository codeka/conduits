package com.codeka.justconduits.common.impl.fluid;

import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.impl.common.CommonExternalConnection;

public class FluidExternalConnection extends CommonExternalConnection {
  private ConduitConnection connection;

  // The number of ticks left until we do another extract operation.
  int ticksUntilNextExtract;
}
