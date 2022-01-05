package com.codeka.justconduits.common.capabilities.network;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;

import javax.annotation.Nullable;

/**
 * Each block (represented by a {@link ConduitBlockEntity}) can hold multiple "kinds" of conduit (e.g. you can have
 * an item conduit, fluid conduit and energy conduit all in the same block). This class contains, for each block, all
 * the information we need to know for a single conduit type.
 */
public class ConduitHolder {
  private ConduitType conduitType;

  /**
   * The network that this conduit block entity belongs to. Will be null until we first populate it, so we'll need to
   * be careful.
   */
  @Nullable
  protected NetworkRef networkRef;

  public ConduitHolder(ConduitType conduitType) {
    this.conduitType = conduitType;
  }

  public ConduitType getConduitType() {
    return conduitType;
  }

  public IConduit getConduitImpl() {
    return conduitType.getConduitImpl();
  }

  /** Gets the {@link NetworkRef} we belong to. Could be null if we haven't populated it yet. */
  // TODO: this should be per-conduit type.
  @Nullable
  public NetworkRef getNetworkRef() {
    return networkRef;
  }

  /** Called to join the given network. */
  // TODO: this should be per-conduit type.
  public void setNetworkRef(NetworkRef networkRef) {
    this.networkRef = networkRef;
  }

}
