package com.codeka.justconduits.common.impl.item;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.impl.ConduitHolder;
import com.codeka.justconduits.common.impl.ConnectionMode;
import com.codeka.justconduits.packets.IConduitToolExternalPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ItemConduitToolExternalConnectionPacket implements IConduitToolExternalPacket {
  private final ArrayList<ExternalConnection> externalConnections = new ArrayList<>();

  public ItemConduitToolExternalConnectionPacket() {
  }

  public void init(ConduitBlockEntity conduitBlockEntity, ItemNetwork network, ConduitHolder conduitHolder) {
    for (ConduitConnection conn : network.getExternalConnections()) {
      externalConnections.add(
          new ExternalConnection(
              conn.getBlockPos(),
              conn.getConnectionName(conduitBlockEntity.getLevel()),
              conn.getNetworkExternalConnection(conduitHolder.getConduitType())));
    }
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeCollection(externalConnections, (buf, conn) -> conn.encode(buf));
  }

  @Override
  public void decode(FriendlyByteBuf buffer) {
    externalConnections.addAll(buffer.readList(ExternalConnection::new));
  }

  public List<ExternalConnection> getExternalConnections() {
    return externalConnections;
  }

  public static final class ExternalConnection {
    private final BlockPos blockPos;
    private final Component blockName;
    private final boolean isExtract;
    private final boolean isInsert;

    public ExternalConnection(BlockPos blockPos, Component blockName, ItemExternalConnection itemExternalConnection) {
      this.blockPos = blockPos;
      this.blockName = blockName;
      isExtract = itemExternalConnection.getExtractMode() != ConnectionMode.ALWAYS_OFF;
      isInsert = itemExternalConnection.getInsertMode() != ConnectionMode.ALWAYS_OFF;
    }

    public ExternalConnection(FriendlyByteBuf buffer) {
      blockPos = buffer.readBlockPos();
      blockName = buffer.readComponent();
      isExtract = buffer.readBoolean();
      isInsert = buffer.readBoolean();
    }

    public void encode(FriendlyByteBuf buffer) {
      buffer.writeBlockPos(blockPos);
      buffer.writeComponent(blockName);
      buffer.writeBoolean(isExtract);
      buffer.writeBoolean(isInsert);
    }

    public BlockPos getBlockPos() {
      return blockPos;
    }

    public Component getBlockName() {
      return blockName;
    }

    /** Returns true if the extract setting is anything other than "always off". */
    public boolean isExtract() {
      return isExtract;
    }

    /** Returns true if the insert setting is anything other than "always off". */
    public boolean isInsert() {
      return isInsert;
    }
  }
}
