package com.codeka.justconduits.packets;

import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitBlockPacketHandler;
import com.codeka.justconduits.common.impl.ConduitType;
import com.google.common.base.MoreObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This packet is sent to the client when the state of a conduit changes on the server. We only send the state that is
 * necessary for actually rendering the conduit.
 */
// TODO: can we make this more generic or something?
public class ConduitClientStatePacket {
  private static final Logger L = LogManager.getLogger();
  private final BlockPos blockPos;
  private final ArrayList<ConnectionPacket> connectionPackets;
  private final HashMap<ConduitType, IConduitTypeClientStatePacket> conduitTypes;

  public ConduitClientStatePacket(ConduitBlockEntity conduitBlockEntity) {
    blockPos = conduitBlockEntity.getBlockPos();
    connectionPackets = new ArrayList<>();
    for (ConduitConnection conn : conduitBlockEntity.getConnections()) {
      connectionPackets.add(new ConnectionPacket(conn));
    }
    conduitTypes = new HashMap<>();
    for (ConduitType conduitType : conduitBlockEntity.getConduitTypes()) {
      conduitTypes.put(conduitType, conduitType.getConduitImpl().createClientState(conduitBlockEntity));
    }
  }

  public ConduitClientStatePacket(FriendlyByteBuf buffer) {
    blockPos = buffer.readBlockPos();
    ArrayList<HashMap.Entry<ConduitType, IConduitTypeClientStatePacket>> entries =
        buffer.readCollection(ArrayList::new, (buf) -> {
          int len = buf.readVarInt();
          String name = buf.readCharSequence(len, StandardCharsets.UTF_8).toString();

          ConduitType conduitType = checkNotNull(ConduitType.fromName(name));
          IConduitTypeClientStatePacket packet = conduitType.newConduitTypeClientStatePacket();
          packet.decode(buf);

          return new AbstractMap.SimpleEntry<>(conduitType, packet);
        });
    conduitTypes = new HashMap<>();
    for (var entry : entries) {
      conduitTypes.put(entry.getKey(), entry.getValue());
    }
    connectionPackets = buffer.readCollection(ArrayList::new, ConnectionPacket::new);
  }

  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBlockPos(blockPos);
    buffer.writeCollection(conduitTypes.entrySet(), (buf, entry) -> {
      ConduitType conduitType = entry.getKey();
      IConduitTypeClientStatePacket packet = entry.getValue();

      buf.writeVarInt(conduitType.getName().length());
      buf.writeCharSequence(conduitType.getName(), StandardCharsets.UTF_8);
      packet.encode(buffer);
    });
    buffer.writeCollection(connectionPackets, (buf, conn) -> {
      conn.encode(buf);
    });
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ConduitBlockPacketHandler.handle(this, ctx));
    });
    ctx.get().setPacketHandled(true);
  }

  public BlockPos getBlockPos() {
    return blockPos;
  }

  public Map<ConduitType, IConduitTypeClientStatePacket> getConduits() {
    return conduitTypes;
  }

  public HashMap<Direction, ConnectionPacket> getConnectionPackets() {
    HashMap<Direction, ConnectionPacket> packets = new HashMap<>();
    for (ConnectionPacket conn : connectionPackets) {
      packets.put(conn.getDirection(), conn);
    }
    return packets;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("blockPos", blockPos)
        .add("connectionPackets", connectionPackets)
        .add("conduitTypes", conduitTypes)
        .toString();
  }

  /** All the info we need about a connection. */
  public static final class ConnectionPacket {
    private final Direction direction;
    private final ConduitConnection.ConnectionType connectionType;
    private final Set<ConduitType> conduitTypes;

    public ConnectionPacket(ConduitConnection connection) {
      direction = connection.getDirection();
      connectionType = connection.getConnectionType();
      conduitTypes = new HashSet<>(connection.getConduitTypes());
    }

    public ConnectionPacket(FriendlyByteBuf buffer) {
      direction = buffer.readEnum(Direction.class);
      connectionType = buffer.readEnum(ConduitConnection.ConnectionType.class);
      conduitTypes = buffer.readCollection(HashSet::new, (buf) -> ConduitType.fromName(buf.readUtf()));
    }

    public void encode(FriendlyByteBuf buffer) {
      buffer.writeEnum(direction);
      buffer.writeEnum(connectionType);
      buffer.writeCollection(conduitTypes, (buf, conduitTypes) -> buf.writeUtf(conduitTypes.getName()));
    }

    public Direction getDirection() {
      return direction;
    }

    public ConduitConnection.ConnectionType getConnectionType() {
      return connectionType;
    }

    public Set<ConduitType> getConduitTypes() {
      return conduitTypes;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("direction", direction)
          .add("connectionType", connectionType)
          .add("conduitTypes", conduitTypes)
          .toString();
    }
  }
}
