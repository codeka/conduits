package com.codeka.justconduits.common.impl.common;

import com.codeka.justconduits.common.impl.ConnectionMode;
import com.codeka.justconduits.common.impl.NetworkExternalConnection;

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
