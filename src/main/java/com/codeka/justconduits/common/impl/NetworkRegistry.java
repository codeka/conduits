package com.codeka.justconduits.common.impl;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * We keep track of all the registries in memory, they're not saved to disk instead they are recreated each time a
 * chunk is loaded.
 *
 * <p>Networks are identified by a long, we just increment it for each new network. We'll create a lot of "temporary"
 * networks as they are built, which means we'll go through identifiers quickly. But that's OK.
 */
public class NetworkRegistry {
  private static AtomicLong nextId = new AtomicLong(1);

  private static final HashMap<Long, IConduitNetwork> networks = new HashMap<>();

  public static long newId() {
    return nextId.incrementAndGet();
  }

  /** Returns the network with the given ID, or null if the network is not registered. */
  @SuppressWarnings("unchecked")
  @Nullable
  public static <T extends IConduitNetwork> T  getNetwork(long id) {
    IConduitNetwork network = networks.get(id);
    if (network == null) {
      return null;
    }

    try {
      return (T) network;
    } catch (ClassCastException e) {
      // TODO: log error
      return null;
    }
  }

  public static void register(IConduitNetwork network) {
    if (networks.containsKey(network.getId())) {
      // TODO: log error
      return;
    }

    networks.put(network.getId(), network);
  }

  public static void unregister(IConduitNetwork network) {
    networks.remove(network.getId());
  }
}
