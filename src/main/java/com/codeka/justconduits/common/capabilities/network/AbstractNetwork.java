package com.codeka.justconduits.common.capabilities.network;

import com.codeka.justconduits.common.blocks.ConduitConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Base class for a network of conduits. Each conduit will have its own network.
 */
public abstract class AbstractNetwork implements IConduitNetwork {
  private static final Logger L = LogManager.getLogger();

  private long id;
  private final NetworkType networkType;

  // A list of all the external connections we have.
  private final ArrayList<ConduitConnection> externalConnections = new ArrayList<>();

  public AbstractNetwork(NetworkType networkType) {
    id = NetworkRegistry.newId();
    this.networkType = networkType;
  }

  @Nonnull
  @Override
  public NetworkType getNetworkType() {
    return networkType;
  }

  @Nonnull
  @Override
  public long getId() {
    return id;
  }

  @Override
  public void updateId(long id) {
    this.id = id;
  }

  @Override
  public void addExternalConnection(@Nonnull ConduitConnection conn) {
    externalConnections.add(conn);
  }

  @Nonnull
  @Override
  public Collection<ConduitConnection> getExternalConnections() {
    return externalConnections;
  }

  /**
   * Combine the given {@link AbstractNetwork} with us.
   *
   * We will take all the given network's connections and add it to our collection.
   */
  @Override
  public void combine(@Nonnull IConduitNetwork network) {
    if (networkType != network.getNetworkType()) {
      L.atError().log(
          "Trying to combine two networks of a different type ({} and {})!", networkType, network.getNetworkType());
      return;
    }

    for (ConduitConnection conn : network.getExternalConnections()) {
      addExternalConnection(conn);
    }
  }
}
