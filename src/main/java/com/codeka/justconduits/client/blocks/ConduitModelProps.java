package com.codeka.justconduits.client.blocks;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

public class ConduitModelProps {
  public static final ModelProperty<ConduitBlockEntity> CONDUIT_BLOCK_ENTITY = new ModelProperty<>();

  public static IModelData getModelData(ConduitBlockEntity conduitBlockEntity) {
    return new ModelDataMap.Builder()
        .withInitial(CONDUIT_BLOCK_ENTITY, conduitBlockEntity)
        .build();
  }
}
