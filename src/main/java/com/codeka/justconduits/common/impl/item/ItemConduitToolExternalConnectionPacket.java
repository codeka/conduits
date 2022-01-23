package com.codeka.justconduits.common.impl.item;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.impl.ConduitHolder;
import com.codeka.justconduits.common.impl.ConnectionMode;
import com.codeka.justconduits.packets.IConduitToolExternalPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemConduitToolExternalConnectionPacket implements IConduitToolExternalPacket {
  private final ArrayList<ExternalConnection> externalConnections = new ArrayList<>();

  public ItemConduitToolExternalConnectionPacket() {
  }

  public void init(ConduitBlockEntity conduitBlockEntity, ItemNetwork network, ConduitHolder conduitHolder) {
    Level level = conduitBlockEntity.getLevel();
    if (level == null) {
      return;
    }

    for (ConduitConnection conn : network.getExternalConnections()) {
      ItemNetwork.ItemStats stats = network.getStats(conn);
      externalConnections.add(
          new ExternalConnection(
              conn.getBlockPos(),
              conn.getConnectionName(conduitBlockEntity.getLevel()),
              level.getBlockState(conn.getConnectedBlockPos()).getBlock(),
              conn.getNetworkExternalConnection(conduitHolder.getConduitType()),
              stats.itemsExtracted,
              stats.itemsInserted));
    }
    network.resetStats();
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
    private final Block block;
    private final boolean isExtract;
    private final boolean isInsert;
    private final float numExtractedPerTick;
    private final float numInsertedPerTick;

    public ExternalConnection(
        BlockPos blockPos, Component blockName, Block block, ItemExternalConnection itemExternalConnection,
        float numExtractedPerTick, float numInsertedPerTick) {
      this.blockPos = blockPos;
      this.blockName = blockName;
      this.block = block;
      isExtract = itemExternalConnection.getExtractMode() != ConnectionMode.ALWAYS_OFF;
      isInsert = itemExternalConnection.getInsertMode() != ConnectionMode.ALWAYS_OFF;
      this.numExtractedPerTick = numExtractedPerTick;
      this.numInsertedPerTick = numInsertedPerTick;
    }

    public ExternalConnection(FriendlyByteBuf buffer) {
      blockPos = buffer.readBlockPos();
      blockName = buffer.readComponent();
      block = ForgeRegistries.BLOCKS.getValue(buffer.readResourceLocation());
      isExtract = buffer.readBoolean();
      isInsert = buffer.readBoolean();
      numExtractedPerTick = buffer.readFloat();
      numInsertedPerTick = buffer.readFloat();
    }

    public void encode(FriendlyByteBuf buffer) {
      buffer.writeBlockPos(blockPos);
      buffer.writeComponent(blockName);
      ResourceLocation blockResourceLocation = ForgeRegistries.BLOCKS.getKey(block);
      buffer.writeResourceLocation(
          Objects.requireNonNullElseGet(
              blockResourceLocation,
              () -> new ResourceLocation(ResourceLocation.DEFAULT_NAMESPACE, "error")));
      buffer.writeBoolean(isExtract);
      buffer.writeBoolean(isInsert);
      buffer.writeFloat(numExtractedPerTick);
      buffer.writeFloat(numInsertedPerTick);
    }

    public BlockPos getBlockPos() {
      return blockPos;
    }

    public Component getBlockName() {
      return blockName;
    }

    public Block getBlock() {
      return block;
    }

    /** Returns true if the extract setting is anything other than "always off". */
    public boolean isExtract() {
      return isExtract;
    }

    /** Returns true if the insert setting is anything other than "always off". */
    public boolean isInsert() {
      return isInsert;
    }

    public float getNumExtractedPerTick() {
      return numExtractedPerTick;
    }

    public float getNumInsertedPerTick() {
      return numInsertedPerTick;
    }
  }
}
