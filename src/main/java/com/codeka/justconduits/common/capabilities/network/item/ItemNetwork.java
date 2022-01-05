package com.codeka.justconduits.common.capabilities.network.item;

import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.capabilities.network.AbstractNetwork;
import com.codeka.justconduits.common.capabilities.network.NetworkType;

import java.util.ArrayList;

public class ItemNetwork extends AbstractNetwork {
 // private final ArrayList<ItemExternalConnection> itemExternalConnections = new ArrayList<>();

  public ItemNetwork() {
    super(NetworkType.ITEM);
  }
/*
  @Override
  public void addExternalConnection(ConduitConnection conn) {
    super.addExternalConnection(conn);


  }*/
}
