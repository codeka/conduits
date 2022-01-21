package com.codeka.justconduits.common.impl;

import com.codeka.justconduits.common.blocks.ConduitConnection;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface IConduitNetwork {

  @Nonnull
  NetworkType getNetworkType();

  long getId();
  void updateId(long id);

  void addExternalConnection(@Nonnull ConduitConnection conn);

  @Nonnull
  Collection<ConduitConnection> getExternalConnections();

  void combine(@Nonnull IConduitNetwork network);
}
