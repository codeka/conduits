package com.codeka.justconduits.client.blocks;

import com.codeka.justconduits.common.blocks.ConduitConnection;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.List;

public class ConduitModelProps {
  public static final ModelProperty<List<ConduitConnection>> CONNECTIONS = new ModelProperty<>();
}
