package com.codeka.justconduits.common;

import com.codeka.justconduits.common.capabilities.network.IConduitNetworkManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class ModCapabilities {
  public static Capability<IConduitNetworkManager> CONDUIT_NETWORK_MANAGER =
      CapabilityManager.get(new CapabilityToken<>(){});
}
