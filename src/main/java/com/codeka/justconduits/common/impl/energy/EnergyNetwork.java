package com.codeka.justconduits.common.impl.energy;

import com.codeka.justconduits.common.impl.AbstractNetwork;
import com.codeka.justconduits.common.impl.NetworkType;
import net.minecraftforge.energy.IEnergyStorage;

public class EnergyNetwork extends AbstractNetwork implements IEnergyStorage {
  // The maximum amount of energy we can keep in the buffer.
  private int capacity = 256;

  // The current energy we have in the buffer.
  private int currEnergy = 0;

  public EnergyNetwork() {
    super(NetworkType.ENERGY);
  }

  @Override
  public int receiveEnergy(int maxReceive, boolean simulate) {
    int amount = Math.min(maxReceive, capacity - currEnergy);
    if (simulate) {
      return amount;
    }

    currEnergy += amount;
    return amount;
  }

  @Override
  public int extractEnergy(int maxExtract, boolean simulate) {
    int amount = Math.min(maxExtract, currEnergy);
    if (simulate) {
      return amount;
    }

    currEnergy -= amount;
    return amount;
  }

  @Override
  public int getEnergyStored() {
    return currEnergy;
  }

  @Override
  public int getMaxEnergyStored() {
    return capacity;
  }

  @Override
  public boolean canExtract() {
    return currEnergy > 0;
  }

  @Override
  public boolean canReceive() {
    return currEnergy < capacity;
  }
}
