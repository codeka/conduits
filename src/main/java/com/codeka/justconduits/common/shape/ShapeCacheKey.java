package com.codeka.justconduits.common.shape;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.capabilities.network.NetworkType;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Key used for caching the shape of conduits. We record everything that makes the *shape* (not counting textures) of
 * a conduit unique, so that different blocks can use the same shape objects, assuming they look the same.
 */
public class ShapeCacheKey {
  private final Map<Direction, ConnectionKey> connections = new HashMap<>();

  public ShapeCacheKey(ConduitBlockEntity conduitBlockEntity) {
    for (ConduitConnection connection : conduitBlockEntity.getConnections()) {
      connections.put(connection.getDirection(), new ConnectionKey(connection));
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(connections.keySet(), connections.values());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ShapeCacheKey other) {
      return Objects.equals(connections.keySet(), other.connections.keySet()) &&
          Objects.equals(connections.values(), other.connections.values());
    }

    return false;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    for (Map.Entry<Direction, ConnectionKey> entry : connections.entrySet()) {
      sb.append(String.format("{%s : %s}", entry.getKey(), entry.getValue()));
    }
    sb.append("}");
    return sb.toString();
  }

  /** The stuff we need to know about a connection, to generate a unique key for it. */
  private static final class ConnectionKey {
    private final ConduitConnection.ConnectionType connectionType;
    private final ArrayList<NetworkType> connectedNetworks;

    public ConnectionKey(ConduitConnection connection) {
      connectionType = connection.getConnectionType();
      connectedNetworks = new ArrayList<>(connection.getConnectedNetworks());
    }

    @Override
    public int hashCode() {
      return Objects.hash(connectionType, connectedNetworks);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ConnectionKey other) {
        return Objects.equals(other.connectionType, connectionType) &&
            Objects.equals(other.connectedNetworks, connectedNetworks);
      }

      return false;
    }

    @Override
    public String toString() {
      return String.format(
          "{%s, [%s]}",
          connectionType,
          String.join(", ",  Arrays.toString(connectedNetworks.toArray())));
    }
  }
}
