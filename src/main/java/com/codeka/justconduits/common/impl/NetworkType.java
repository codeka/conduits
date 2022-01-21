package com.codeka.justconduits.common.impl;

import com.codeka.justconduits.client.gui.conduittool.IConduitToolScreenRenderer;
import com.codeka.justconduits.client.gui.conduittool.ItemConduitToolScreenRenderer;
import com.codeka.justconduits.common.impl.energy.EnergyNetwork;
import com.codeka.justconduits.common.impl.fluid.FluidNetwork;
import com.codeka.justconduits.common.impl.item.ItemConduitToolExternalConnectionPacket;
import com.codeka.justconduits.common.impl.item.ItemNetwork;
import com.codeka.justconduits.packets.IConduitToolExternalPacket;

import java.util.function.Supplier;

/**
 * The type of network.
 *
 * Each ConduitBlockEntity can have at most one network of each type running through it. Additionally, a network must be
 * made up of all the same conduits. For example, a "simple item conduit" and an "advanced item conduit" cannot exist
 * together in the same network, because they are both NetworkType#ITEM conduits of different classes. Similarly, if
 * one conduit block has a "simple item conduit" and it's neighbor has an "advanced item conduit", they cannot join
 * because those are different classes.
 */
// TODO: can we use resource locations as the name? so other mods can add their own network types?
public class NetworkType {
  public static final NetworkType ITEM =
      new NetworkType(
          "item", ItemNetwork::new, ItemConduitToolExternalConnectionPacket::new,
          new ItemConduitToolScreenRenderer());

  public static final NetworkType FLUID = new NetworkType("fluid", FluidNetwork::new, () -> null, null);

  public static final NetworkType ENERGY = new NetworkType("energy", EnergyNetwork::new, () -> null, null);

  public static final NetworkType REDSTONE = new NetworkType("redstone", () -> null, () -> null, null);

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
  private final Supplier<IConduitToolExternalPacket> externalPacketSupplier;
  private final IConduitToolScreenRenderer conduitToolScreenRenderer;

  public NetworkType(
      String name,
      Supplier<IConduitNetwork> networkSupplier,
      Supplier<IConduitToolExternalPacket> externalPacketSupplier,
      IConduitToolScreenRenderer conduitToolScreenRenderer) {
    this.name = name;
    this.networkSupplier = networkSupplier;
    this.externalPacketSupplier = externalPacketSupplier;
    this.conduitToolScreenRenderer = conduitToolScreenRenderer;
  }

  public String getName() {
    return name;
  }

  public IConduitNetwork newNetwork() {
    return networkSupplier.get();
  }

  public IConduitToolExternalPacket newConduitToolExternalPacket() {
    return externalPacketSupplier.get();
  }

  public IConduitToolScreenRenderer getConduitToolScreenRenderer() {
    return conduitToolScreenRenderer;
  }
}
