package com.codeka.justconduits.client.blocks;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.capabilities.network.ConduitType;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.ArrayList;
import java.util.List;

public class ConduitModelProps {
  public static final ModelProperty<List<ConduitConnection>> CONNECTIONS = new ModelProperty<>();
  public static final ModelProperty<String> CONDUIT_TYPE = new ModelProperty<>();

  public static IModelData getModelData(ConduitBlockEntity conduitBlockEntity) {
    // TODO: support more than one
    ArrayList<ConduitType> conduitTypes = new ArrayList<>(conduitBlockEntity.getConduitTypes());
    String conduitTypeName =
        conduitTypes.size() == 0 ? ConduitType.SIMPLE_ITEM.getName() : conduitTypes.get(0).getName();

    return new ModelDataMap.Builder()
        .withInitial(CONNECTIONS, new ArrayList<>(conduitBlockEntity.getConnections()))
        .withInitial(CONDUIT_TYPE, conduitTypeName)
        .build();
  }
}
