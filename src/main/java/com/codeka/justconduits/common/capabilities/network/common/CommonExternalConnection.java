package com.codeka.justconduits.common.capabilities.network.common;

import com.codeka.justconduits.common.capabilities.network.ConnectionMode;
import com.codeka.justconduits.common.capabilities.network.NetworkExternalConnection;

/**
 * Base class for "regular" external connections, that support insert and extract modes.
 */
public class CommonExternalConnection extends NetworkExternalConnection {
  private ConnectionMode extractMode = ConnectionMode.ALWAYS_OFF;
  private ConnectionMode insertMode = ConnectionMode.ALWAYS_OFF;

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
