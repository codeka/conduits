package com.codeka.justconduits.common.capabilities.network;

import com.codeka.justconduits.common.blocks.ConduitBlockEntity;
import com.codeka.justconduits.common.blocks.ConduitConnection;
import com.codeka.justconduits.common.capabilities.network.item.ItemNetwork;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Stack;

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
    L.atInfo().log("Initializing CBE @ {}", conduitBlockEntity.getBlockPos());

    final Level level = conduitBlockEntity.getLevel();
    if (level == null) {
      L.atError().log("ConduitBlockEntity doesn't have a level.");
      return;
    }

    for (ConduitHolder conduitHolder : conduitBlockEntity.getConduitHolders()) {
      init(level, conduitBlockEntity, conduitHolder);
    }

    L.atInfo().log(" - complete.");
  }

  private void init(Level level, ConduitBlockEntity conduitBlockEntity, ConduitHolder conduitHolder) {
    if (conduitHolder.getNetworkRef() != null) {
      L.atWarn().log("init called on ConduitBlockEntity that already belongs to a network.");
      return;
    }

    // First, create a new network that consists of only this conduit.
    ItemNetwork itemNetwork = new ItemNetwork();
    NetworkRegistry.register(itemNetwork);

    // Keep a stack of the ConduitBlockEntities that we haven't visited yet. Start with the one we're at now.
    Stack<ConduitBlockEntity> open = new Stack<>();
    open.add(conduitBlockEntity);

    while (!open.isEmpty()) {
      ConduitBlockEntity cbe = open.pop();
      ConduitHolder ch = cbe.getConduitHolder(conduitHolder.getConduitType());
      if (ch == null) {
        continue;
      }

      if (ch.getNetworkRef() != null && ch.getNetworkRef().getId() != itemNetwork.getNetworkRef().getId()) {
        // This ConduitBlockEntity already belongs to a network. We should join this network, since it's already
        // populated.

        ItemNetwork existingNetwork = NetworkRegistry.getNetwork(ch.getNetworkRef().getId());
        if (existingNetwork == null) {
          L.atError().log("ConduitBlockEntity has a network reference that isn't registered.");
          // TODO: should we crash here? something's corrupted.
          return;
        }
        existingNetwork.combine(itemNetwork);

        // And now we are populating the existing network, so unregister our network, update the network ref and
        // continue with the existing one.
        NetworkRegistry.unregister(itemNetwork);
        itemNetwork.getNetworkRef().setId(existingNetwork.getNetworkRef().getId());
        itemNetwork = existingNetwork;

        continue;
      }

      ch.setNetworkRef(itemNetwork.getNetworkRef());

      for (ConduitConnection conn : cbe.getConnections()) {
        switch (conn.getConnectionType()) {
          case CONDUIT -> {
            BlockEntity be = level.getBlockEntity(cbe.getBlockPos().relative(conn.getDirection()));
            if (be instanceof ConduitBlockEntity neighbor) {
              ConduitHolder neighborConduitHolder = neighbor.getConduitHolder(conduitHolder.getConduitType());
              if (neighborConduitHolder == null) {
                continue;
              }
              if (neighborConduitHolder.getNetworkRef() != null &&
                  neighborConduitHolder.getNetworkRef().getId() == itemNetwork.getNetworkRef().getId()) {
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
            itemNetwork.addExternalConnection(conn);
          }
        }
      }
    }
  }
}
