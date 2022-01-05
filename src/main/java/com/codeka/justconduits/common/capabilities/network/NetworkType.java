package com.codeka.justconduits.common.capabilities.network;

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
  public static final NetworkType ITEM = new NetworkType("item");

  public static final NetworkType FLUID = new NetworkType("fluid");

  public static final NetworkType ENERGY = new NetworkType("energy");

  public static final NetworkType REDSTONE = new NetworkType("redstone");

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

  private String name;

  public NetworkType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
