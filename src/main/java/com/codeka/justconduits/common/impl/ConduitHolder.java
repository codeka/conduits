package com.codeka.justconduits.common.impl;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;

/**
 * Each block (represented by a {@link ConduitBlockEntity}) can hold multiple "kinds" of conduit (e.g. you can have
 * an item conduit, fluid conduit and energy conduit all in the same block). This class contains, for each block, all
 * the information we need to know for a single conduit type.
 */
public class ConduitHolder {
  private ConduitType conduitType;

  /**
   * The network that this conduit block entity belongs to. Will be -1 until we first populate it, so we'll need to
   * be careful.
   */
  protected long id;

  public ConduitHolder(ConduitType conduitType) {
    this.conduitType = conduitType;
  }

  public ConduitType getConduitType() {
    return conduitType;
  }

  public IConduit getConduitImpl() {
    return conduitType.getConduitImpl();
  }

  /** Gets the ID of the network we belong to. Could be -1 if we haven't populated it yet. */
  public long getNetworkId() {
    return id;
  }

  /**
   *  Called to join the given network.
   *
   * @return True if we actually changed network IDs.
   */
  public boolean updateNetworkId(long id) {
    if (this.id == id) {
      return false;
    }

    this.id = id;
    return true;
  }

}
