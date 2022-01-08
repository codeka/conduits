package com.codeka.justconduits.client.gui;

import com.codeka.justconduits.common.capabilities.network.ConduitType;

import java.util.HashMap;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConduitTabMapping {
  private static HashMap<ConduitType, Supplier<IConduitTab>> tapSuppliers = new HashMap<>();

  public static void addMapping(ConduitType conduitType, Supplier<IConduitTab> supplier) {
    tapSuppliers.put(conduitType, supplier);
  }

  public static IConduitTab newTab(ConduitType conduitType) {
    var supplier = checkNotNull(tapSuppliers.get(conduitType));
    return supplier.get();
  }
}
