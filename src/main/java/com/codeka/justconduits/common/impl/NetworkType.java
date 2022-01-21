package com.codeka.justconduits.common.impl;

import com.codeka.justconduits.common.impl.energy.EnergyNetwork;
import com.codeka.justconduits.common.impl.fluid.FluidNetwork;
import com.codeka.justconduits.common.impl.item.ItemNetwork;

import java.util.function.Supplier;

/**
 * The type of a network.
 *
 * Each ConduitBlockEntity can have at most one network of each type running through it. Additionally, a network must be
 * made up of all the same conduits. For example, a "simple item conduit" and an "advanced item conduit" cannot exist
 * together in the same network, because they are both NetworkType#ITEM conduits of different classes. Similarly, if
 * one conduit block has a "simple item conduit" and it's neighbor has an "advanced item conduit", they cannot join
 * because those are different classes.
 */
// TODO: can we use resource locations as the name? so other mods can add their own network types?
public class NetworkType {
  public static final NetworkType ITEM = new NetworkType("item", ItemNetwork::new);

  public static final NetworkType FLUID = new NetworkType("fluid", FluidNetwork::new);

  public static final NetworkType ENERGY = new NetworkType("energy", EnergyNetwork::new);

  public static final NetworkType REDSTONE = new NetworkType("redstone", () -> null);

  public static NetworkType fromName(String name) {
    // TODO: actually register these. Or read from config?
    return switch (name) {
      case "item" -> ITEM;
      case "fluid" -> FLUID;
      case "energy" -> ENERGY;
      case "redstone" -> REDSTONE;
      default -> throw new RuntimeException("Unknown network type name");
    };
  }

  private final String name;
  private final Supplier<IConduitNetwork> networkSupplier;

  public NetworkType(String name, Supplier<IConduitNetwork> networkSupplier) {
    this.name = name;
    this.networkSupplier = networkSupplier;
  }

  public String getName() {
    return name;
  }

  public IConduitNetwork newNetwork() {
    return networkSupplier.get();
  }
}
