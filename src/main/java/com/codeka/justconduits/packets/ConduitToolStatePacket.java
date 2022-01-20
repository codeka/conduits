package com.codeka.justconduits.packets;

import com.codeka.justconduits.client.gui.conduittool.ConduitToolScreenPacketHandler;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.capabilities.network.ConduitHolder;
import com.codeka.justconduits.common.capabilities.network.ConduitType;
import com.codeka.justconduits.common.capabilities.network.NetworkType;
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
          new ConduitNetworkStatePacket(conduitType.getNetworkType(), conduitHolder.getNetworkRef().getId()));
    }
  }

  public ConduitToolStatePacket(FriendlyByteBuf buffer) {
    blockPos = buffer.readBlockPos();
    List<ConduitNetworkStatePacket> packets = buffer.readList((buf) -> {
      NetworkType networkType = NetworkType.fromName(buf.readUtf());
      long networkId = buf.readVarLong();
      return new ConduitNetworkStatePacket(networkType, networkId);
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

    public ConduitNetworkStatePacket(NetworkType networkType, long networkId) {
      this.networkType = networkType;
      this.networkId = networkId;
    }

    public NetworkType getNetworkType() {
      return networkType;
    }

    public long getNetworkId() {
      return networkId;
    }
  }
}
