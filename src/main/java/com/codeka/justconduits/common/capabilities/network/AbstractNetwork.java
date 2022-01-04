package com.codeka.justconduits.common.capabilities.network;

import com.codeka.justconduits.common.blocks.ConduitConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Base class for a network of conduits. Each conduit will have its own network.
 */
public abstract class AbstractNetwork {
  private static final Logger L = LogManager.getLogger();

  private NetworkRef id;
  private NetworkType networkType;

  // A list of all the external connections we have.
  private final ArrayList<ConduitConnection> externalConnections = new ArrayList<>();

  public AbstractNetwork(NetworkType networkType) {
    id = new NetworkRef(NetworkRegistry.newId());
    this.networkType = networkType;
  }

  public NetworkType getNetworkType() {
    return networkType;
  }

  public NetworkRef getNetworkRef() {
    return id;
  }

  public void addExternalConnection(ConduitConnection conn) {
    externalConnections.add(conn);
  }

  public Collection<ConduitConnection> getExternalConnections() {
    return externalConnections;
  }

  /**
   * Combine the given {@link AbstractNetwork} with us.
   *
   * We will take all the given network's connections and add it to our collection.
   */
  public void combine(AbstractNetwork network) {
    if (networkType != network.getNetworkType()) {
      L.atError().log(
          "Trying to combine two networks of a different type ({} and {})!", networkType, network.getNetworkType());
      return;
    }

    for (ConduitConnection conn : network.externalConnections) {
      addExternalConnection(conn);
    }
  }
}
