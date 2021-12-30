package com.codeka.justconduits.common.blocks;

import net.minecraft.core.Direction;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Keeps track of the information that connects one {@link ConduitBlockEntity} to another.
 */
public class ConduitConnection {
  public enum ConnectionType {
    /** An unknown type of connection. Shouldn't be used. */
    UNKNOWN,

    /** The connection is to another conduit. */
    CONDUIT,

    /** The connection is to an external block. */
    EXTERNAL
  }

  private final Direction dir;
  private final ConnectionType connectionType;

  public ConduitConnection(@Nonnull Direction dir, @Nonnull ConnectionType connectionType) {
    this.dir = checkNotNull(dir);
    this.connectionType = checkNotNull(connectionType);
  }

  public Direction getDirection() {
    return dir;
  }

  public ConnectionType getConnectionType() {
    return connectionType;
  }
}
