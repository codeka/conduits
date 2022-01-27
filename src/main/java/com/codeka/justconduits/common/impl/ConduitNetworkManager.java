package com.codeka.justconduits.common.impl;

import com.codeka.justconduits.common.blocks.ConduitBlock;
import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.capabilities.network.IConduitNetworkManager;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Stack;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The default implementation of {@link IConduitNetworkManager}.
 */
public class ConduitNetworkManager implements IConduitNetworkManager {
  private static final Logger L = LogManager.getLogger();

  /**
   * This is called when a new {@link ConduitBlockEntity} is added to the world. Either at load time, or you've just
   * placed one down. We need to either add it to an existing network, or create a new one.
   *
   * <p>This will be called after all the connections have been made, so we can use them to query for conduits that
   * are around us.
   *
   * @param conduitBlockEntity The {@link ConduitBlockEntity} that was just created.
   */
  public void init(ConduitBlockEntity conduitBlockEntity) {
    final Level level = conduitBlockEntity.getLevel();
    if (level == null) {
      L.atError().log("ConduitBlockEntity doesn't have a level.");
      return;
    }

    for (ConduitHolder conduitHolder : conduitBlockEntity.getConduitHolders()) {
      addConduit(level, conduitBlockEntity, conduitHolder);
    }
  }

  /**
   * This is called when you add a new conduit to a {@link ConduitBlockEntity}. We need to setup the network.
   *
   * @param conduitBlockEntity The {@link ConduitBlockEntity}  you're adding a conduit to.
   * @param conduitType The {@link ConduitType} you've just added.
   */
  public void addConduit(ConduitBlockEntity conduitBlockEntity, ConduitType conduitType) {
    Level level = conduitBlockEntity.getLevel();
    if (level == null) {
      L.atError().log("ConduitBlockEntity doesn't have a level.");
      return;
    }

    ConduitHolder conduitHolder = conduitBlockEntity.getConduitHolder(conduitType);
    if (conduitHolder != null) {
      addConduit(level, conduitBlockEntity, conduitHolder);
    }
  }

  /**
   * This is called when you remove a conduit from a {@link ConduitBlockEntity}. We need to update the networks that
   * are neighboring us. They may no longer be connected.
   *
   * @param conduitBlockEntity The {@link ConduitBlockEntity} you're removing a conduit from.
   * @param conduitType The {@link ConduitType} you're removing.
   */
  public void removeConduit(ConduitBlockEntity conduitBlockEntity, ConduitType conduitType) {
    // TODO: implement me.
  }

  private void addConduit(Level level, ConduitBlockEntity conduitBlockEntity, ConduitHolder conduitHolder) {
    if (conduitHolder.getNetworkId() > 0) {
      L.atWarn().log("addConduit called on ConduitBlockEntity that already belongs to a network.");
      return;
    }

    // First, create a new network that consists of only this conduit.
    IConduitNetwork network = conduitHolder.getConduitType().getNetworkType().newNetwork();
    NetworkRegistry.register(network);

    // Keep a stack of the ConduitBlockEntities that we haven't visited yet. Start with the one we're at now.
    Stack<ConduitBlockEntity> open = new Stack<>();
    open.add(conduitBlockEntity);

    ConduitBlockEntity previousConduitBlockEntity = null;
    while (!open.isEmpty()) {
      ConduitBlockEntity cbe = open.pop();
      ConduitHolder ch = cbe.getConduitHolder(conduitHolder.getConduitType());
      if (ch == null) {
        continue;
      }

      if (ch.getNetworkId() > 0 && ch.getNetworkId() != network.getId()) {
        // This ConduitBlockEntity already belongs to a network. We should join this network, since it's already
        // populated.

        IConduitNetwork existingNetwork = NetworkRegistry.getNetwork(ch.getNetworkId());
        if (existingNetwork == null) {
          L.atError().log("ConduitBlockEntity has a network reference that isn't registered.");
          // TODO: should we crash here? something's corrupted.
          return;
        }
        existingNetwork.combine(network);

        // And now we are populating the existing network, so unregister our network, and start using the new one.
        NetworkRegistry.unregister(network);
        network.updateId(existingNetwork.getId());
        network = existingNetwork;

        // Don't forget to go back through all the existing blocks that had the old ID and update them to the new one!
        if (previousConduitBlockEntity != null) {
          replaceNetworkId(previousConduitBlockEntity, ch.getConduitType(), network.getId());
        }

        continue;
      }

      ch.updateNetworkId(network.getId());

      for (ConduitConnection conn : cbe.getConnections()) {
        switch (conn.getConnectionType()) {
          case CONDUIT -> {
            if (conn.getConnectedBlockEntity(level) instanceof ConduitBlockEntity neighbor) {
              ConduitHolder neighborConduitHolder = neighbor.getConduitHolder(conduitHolder.getConduitType());
              if (neighborConduitHolder == null) {
                continue;
              }
              if (neighborConduitHolder.getNetworkId() == network.getId()) {
                // We've already added this one, skip it.
                continue;
              }

              // We'll want to visit this conduit and add it to our network.
              open.add(neighbor);
            } else {
              L.atError().log("Got a conduit connection that doesn't join with a ConduitBlockEntity");
            }
          }
          case EXTERNAL -> {
            // TODO: is there more to do?
            network.addExternalConnection(conn);
          }
        }
      }

      previousConduitBlockEntity = cbe;
    }
  }

  /**
   * When we're populating the network and we encounter an existing one, we need to make sure the network ID is all
   * the same. So we go back and update all the blocks with the old ID to the new ID.
   *
   * @param conduitBlockEntity The {@link ConduitBlockEntity} we're starting on.
   * @param conduitType The {@link ConduitType} of the network we're updating.
   * @param newNetworkId The new network ID.
   */
  private void replaceNetworkId(
      ConduitBlockEntity conduitBlockEntity, ConduitType conduitType, long newNetworkId) {
    final Level level = checkNotNull(conduitBlockEntity.getLevel());
    Stack<ConduitBlockEntity> open = new Stack<>();
    open.add(conduitBlockEntity);

    while (!open.isEmpty()) {
      ConduitBlockEntity cbe = open.pop();

      ConduitHolder holder = cbe.getConduitHolder(conduitType);
      if (holder != null && holder.updateNetworkId(newNetworkId)) {
        for (ConduitConnection conn : cbe.getConnections()) {
          if (conn.getConnectionType() == ConduitConnection.ConnectionType.CONDUIT
              && conn.getConduitTypes().contains(conduitType)) {
            if (conn.getConnectedBlockEntity(level) instanceof ConduitBlockEntity connectedConduitBlockEntity) {
              open.add(connectedConduitBlockEntity);
            }
          }
        }
      }
    }
  }
}
