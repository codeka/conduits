package com.codeka.justconduits.common.impl.item;

import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.impl.AbstractNetwork;
import com.codeka.justconduits.common.impl.NetworkType;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class ItemNetwork extends AbstractNetwork {
  private long lastConduitToolPacketTick;
  private final HashMap<ConduitConnection, ItemStats> conduitToolStats = new HashMap<>();

  public ItemNetwork() {
    super(NetworkType.ITEM);
  }

  @Nonnull
  public ItemStats getStats(ConduitConnection connection) {
    ItemStats stats = conduitToolStats.get(connection);
    if (stats == null) {
      stats = new ItemStats();
      conduitToolStats.put(connection, stats);
    }
    return stats;
  }

  public void resetStats() {
    conduitToolStats.clear();
  }

  public static final class ItemStats {
    public int itemsExtracted;
    public int itemsInserted;
  }
}
