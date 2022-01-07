package com.codeka.justconduits.common.capabilities.network;

import com.codeka.justconduits.common.capabilities.network.fluid.FluidConduit;
import com.codeka.justconduits.common.capabilities.network.fluid.FluidConduitClientStatePacket;
import com.codeka.justconduits.common.capabilities.network.fluid.FluidExternalConnection;
import com.codeka.justconduits.common.capabilities.network.item.ItemConduit;
import com.codeka.justconduits.common.capabilities.network.item.ItemConduitClientStatePacket;
import com.codeka.justconduits.common.capabilities.network.item.ItemExternalConnection;
import com.codeka.justconduits.packets.IConduitTypeClientStatePacket;

import java.util.function.Supplier;

/**
 * Similar to {@link NetworkType}, but conduit type is a registry of each "concrete" conduit type. For example, while
 * {@link NetworkType} has only a single "ITEM" type, {@link ConduitType} has "SIMPLE_ITEN", "REGULAR_ITEM" and
 * "ADVANCED_ITEM" types.
 */
// TODO: this is where we register the textures and so on as well.
public class ConduitType {
  public static ConduitType SIMPLE_ITEM =
      new ConduitType(
          "simple_item", NetworkType.ITEM, ItemConduit::new, ItemExternalConnection::new,
          ItemConduitClientStatePacket::new);

  public static ConduitType SIMPLE_FLUID =
      new ConduitType(
          "simple_fluid", NetworkType.FLUID, FluidConduit::new, FluidExternalConnection::new,
          FluidConduitClientStatePacket::new);

  private final String name;
  private final NetworkType networkType;
  private final IConduit conduitImpl;
  private final Supplier<NetworkExternalConnection> externalConnectionSupplier;
  private final Supplier<IConduitTypeClientStatePacket> clientStatePacketSupplier;

  public static ConduitType fromName(String name) {
    // TODO: actually register these. Or read from config?
    return switch (name) {
      case "simple_item" -> SIMPLE_ITEM;
      case "simple_fluid" -> SIMPLE_FLUID;
      default -> throw new RuntimeException("Unknown ConduitType: " + name);
    };
  }

  public ConduitType(
      String name, NetworkType networkType, Supplier<IConduit> supplier,
      Supplier<NetworkExternalConnection> externalConnectionSupplier,
      Supplier<IConduitTypeClientStatePacket> clientStatePacketSupplier) {
    this.name = name;
    this.networkType = networkType;
    this.conduitImpl = supplier.get();
    this.externalConnectionSupplier = externalConnectionSupplier;
    this.clientStatePacketSupplier = clientStatePacketSupplier;
  }

  public String getName() {
    return name;
  }

  public NetworkType getNetworkType() {
    return networkType;
  }

  public IConduit getConduitImpl() {
    return conduitImpl;
  }

  public NetworkExternalConnection newNetworkExternalConnection() {
    return externalConnectionSupplier.get();
  }

  public IConduitTypeClientStatePacket newConduitTypeClientStatePacket() {
    return clientStatePacketSupplier.get();
  }
}
