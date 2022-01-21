package com.codeka.justconduits.packets;

import com.codeka.justconduits.client.gui.conduittool.ConduitToolScreenPacketHandler;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.impl.ConduitHolder;
import com.codeka.justconduits.common.impl.ConduitType;
import com.codeka.justconduits.common.impl.NetworkType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * This packet is sent continuously from the server to the client as long as you have the conduit tool menu open. We
 * use this to communicate the current state of the conduit networks for the client to display to the player.
 */
public class ConduitToolStatePacket {
  private final BlockPos blockPos;
  private final HashMap<NetworkType, ConduitNetworkStatePacket> networks;

  public ConduitToolStatePacket(ConduitBlockEntity conduitBlockEntity) {
    blockPos = conduitBlockEntity.getBlockPos();

    networks = new HashMap<>();
    for (ConduitType conduitType : conduitBlockEntity.getConduitTypes()) {
      ConduitHolder conduitHolder = conduitBlockEntity.getConduitHolder(conduitType);
      if (conduitHolder == null) {
        continue;
      }

      networks.put(
          conduitType.getNetworkType(),
          new ConduitNetworkStatePacket(
              conduitType.getNetworkType(),
              conduitHolder.getNetworkId(),
              conduitHolder.getConduitImpl().createConduitToolPacket(conduitBlockEntity, conduitHolder)));
    }
  }

  public ConduitToolStatePacket(FriendlyByteBuf buffer) {
    blockPos = buffer.readBlockPos();
    List<ConduitNetworkStatePacket> packets = buffer.readList((buf) -> {
      NetworkType networkType = NetworkType.fromName(buf.readUtf());
      long networkId = buf.readVarLong();
      IConduitToolExternalPacket externalPacket = networkType.newConduitToolExternalPacket();
      if (externalPacket != null) {
        externalPacket.decode(buf);
      }
      return new ConduitNetworkStatePacket(networkType, networkId, externalPacket);
    });
    networks = new HashMap<>();
    for (ConduitNetworkStatePacket packet : packets) {
      networks.put(packet.networkType, packet);
    }
  }

  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBlockPos(blockPos);
    buffer.writeCollection(networks.entrySet(), (buf, entry) -> {
      NetworkType networkType = entry.getKey();
      ConduitNetworkStatePacket packet = entry.getValue();

      buf.writeUtf(networkType.getName());
      buf.writeVarLong(packet.getNetworkId());
      if (packet.externalPacket != null) {
        packet.externalPacket.encode(buf);
      }
    });
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() ->
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ConduitToolScreenPacketHandler.handle(this, ctx.get())));
    ctx.get().setPacketHandled(true);
  }

  public Map<NetworkType, ConduitNetworkStatePacket> getNetworks() {
    return networks;
  }

  /** The state of a single network. */
  public static class ConduitNetworkStatePacket {
    private final NetworkType networkType;
    private final long networkId;
    private final IConduitToolExternalPacket externalPacket;

    public ConduitNetworkStatePacket(
        NetworkType networkType, long networkId, IConduitToolExternalPacket externalPacket) {
      this.networkType = networkType;
      this.networkId = networkId;
      this.externalPacket = externalPacket;
    }

    public NetworkType getNetworkType() {
      return networkType;
    }

    public long getNetworkId() {
      return networkId;
    }

    @SuppressWarnings("unchecked")
    public <T extends IConduitToolExternalPacket> T getExternalPacket() {
      return (T) externalPacket;
    }
  }
}
