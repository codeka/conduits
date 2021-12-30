package com.codeka.justconduits.client.blocks;

import net.minecraft.core.Direction;

/**
 * Keeps track of the information that connects one {@link ConduitBlockEntity} to another.
 */
public class Connection {
  private Direction dir;

  public Connection() {
  }

  public Connection(Direction dir) {
    this.dir = dir;
  }

  public Direction getDirection() {
    return dir;
  }
}
