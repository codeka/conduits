package com.codeka.justconduits.common.impl;

import com.codeka.justconduits.JustConduitsMod;
import com.codeka.justconduits.client.gui.widgets.Icon;
import com.codeka.justconduits.common.impl.energy.EnergyConduit;
import com.codeka.justconduits.common.impl.energy.EnergyConduitClientStatePacket;
import com.codeka.justconduits.common.impl.energy.EnergyExternalConnection;
import com.codeka.justconduits.common.impl.fluid.FluidConduit;
import com.codeka.justconduits.common.impl.fluid.FluidConduitClientStatePacket;
import com.codeka.justconduits.common.impl.fluid.FluidExternalConnection;
import com.codeka.justconduits.common.impl.item.ItemConduit;
import com.codeka.justconduits.common.impl.item.ItemConduitClientStatePacket;
import com.codeka.justconduits.common.impl.item.ItemConduitConfig;
import com.codeka.justconduits.common.impl.item.ItemExternalConnection;
import com.codeka.justconduits.packets.IConduitTypeClientStatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

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
          "simple_item", NetworkType.ITEM, Icon.ITEMS, ItemConduit::new, ItemConduitConfig.SIMPLE,
          ItemExternalConnection::new, ItemConduitClientStatePacket::new);

  public static ConduitType REGULAR_ITEM =
      new ConduitType(
          "regular_item", NetworkType.ITEM, Icon.ITEMS, ItemConduit::new, ItemConduitConfig.REGULAR,
          ItemExternalConnection::new, ItemConduitClientStatePacket::new);

  public static ConduitType SIMPLE_FLUID =
      new ConduitType(
          "simple_fluid", NetworkType.FLUID, Icon.FLUID, FluidConduit::new, null, FluidExternalConnection::new,
          FluidConduitClientStatePacket::new);

  public static ConduitType SIMPLE_ENERGY =
      new ConduitType(
          "simple_energy", NetworkType.ENERGY, Icon.ENERGY, EnergyConduit::new, null, EnergyExternalConnection::new,
          EnergyConduitClientStatePacket::new);

  private final String name;
  private final NetworkType networkType;
  private final IConduit conduitImpl;
  private final Icon guiIcon;
  private final Supplier<NetworkExternalConnection> externalConnectionSupplier;
  private final Supplier<IConduitTypeClientStatePacket> clientStatePacketSupplier;
  private final Object config;

  public static ConduitType fromName(String name) {
    // TODO: actually register these. Or read from config?
    return switch (name) {
      case "simple_item" -> SIMPLE_ITEM;
      case "regular_item" -> REGULAR_ITEM;
      case "simple_fluid" -> SIMPLE_FLUID;
      case "simple_energy" -> SIMPLE_ENERGY;
      default -> throw new RuntimeException("Unknown ConduitType: " + name);
    };
  }

  public ConduitType(
      String name, NetworkType networkType, Icon guiIcon, Supplier<IConduit> supplier, Object config,
      Supplier<NetworkExternalConnection> externalConnectionSupplier,
      Supplier<IConduitTypeClientStatePacket> clientStatePacketSupplier) {
    this.name = name;
    this.networkType = networkType;
    this.guiIcon = guiIcon;
    this.conduitImpl = supplier.get();
    this.externalConnectionSupplier = externalConnectionSupplier;
    this.clientStatePacketSupplier = clientStatePacketSupplier;
    this.config = config;
  }

  public String getName() {
    return name;
  }

  public ItemStack getItemStack(int count) {
    Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(JustConduitsMod.MODID, name + "_conduit"));
    if (item == null) {
      return ItemStack.EMPTY;
    }

    return new ItemStack(item, count);
  }

  public NetworkType getNetworkType() {
    return networkType;
  }

  public Icon getGuiIcon() {
    return guiIcon;
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

  @SuppressWarnings("unchecked")
  public <T> T getConfig() {
    return (T) config;
  }

  @Override
  public String toString() {
    return getName();
  }
}
