package com.codeka.justconduits.common.impl.item;

import com.codeka.justconduits.common.ChannelColor;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.impl.common.CommonExternalConnection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a connection between an item conduit network and an external inventory.
 */
public class ItemExternalConnection extends CommonExternalConnection {
  private ConduitConnection connection;

  /**
   * The {@link ChannelColor} of this connection for extracting.
   */
  private ChannelColor extractChannelColor = ChannelColor.BLUE;

  /**
   * The {@link ChannelColor} of this connection for inserting.
   */
  private ChannelColor insertChannelColor = ChannelColor.BLUE;

  // The number of ticks left until we do another extract operation.
  int ticksUntilNextExtract;

  public ChannelColor getExtractChannelColor() {
    return extractChannelColor;
  }

  public void setExtractChannelColor(@Nonnull ChannelColor channelColor) {
    extractChannelColor = channelColor;
  }

  public ChannelColor getInsertChannelColor() {
    return insertChannelColor;
  }

  public void setInsertChannelColor(@Nonnull ChannelColor channelColor) {
    insertChannelColor = channelColor;
  }
}
