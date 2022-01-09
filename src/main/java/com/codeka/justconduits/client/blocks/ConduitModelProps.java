package com.codeka.justconduits.client.blocks;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.capabilities.network.ConduitType;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ConduitModelProps {
  public static final ModelProperty<List<ConduitConnection>> CONNECTIONS = new ModelProperty<>();
  public static final ModelProperty<List<String>> CONDUIT_TYPES = new ModelProperty<>();

  public static IModelData getModelData(ConduitBlockEntity conduitBlockEntity) {
    Collection<ConduitType> conduitTypes = conduitBlockEntity.getConduitTypes();
    ArrayList<String> conduitTypeNames = new ArrayList<>(conduitTypes.size());
    for (ConduitType conduitType : conduitTypes) {
      conduitTypeNames.add(conduitType.getName());
    }

    return new ModelDataMap.Builder()
        .withInitial(CONNECTIONS, new ArrayList<>(conduitBlockEntity.getConnections()))
        .withInitial(CONDUIT_TYPES, conduitTypeNames)
        .build();
  }
}
