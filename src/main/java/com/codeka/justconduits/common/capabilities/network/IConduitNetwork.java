package com.codeka.justconduits.common.capabilities.network;

import com.codeka.justconduits.common.blocks.ConduitConnection;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface IConduitNetwork {

  @Nonnull
  NetworkType getNetworkType();

  @Nonnull
  NetworkRef getNetworkRef();

  void addExternalConnection(@Nonnull ConduitConnection conn);

  @Nonnull
  Collection<ConduitConnection> getExternalConnections();

  void combine(@Nonnull IConduitNetwork network);
}