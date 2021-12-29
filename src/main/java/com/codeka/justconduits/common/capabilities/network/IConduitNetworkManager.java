package com.codeka.justconduits.common.capabilities.network;

/**
 * The conduit network manager manages all the *networks* of conduits that run through a particular block entity. A
 * fully-connected graph of a single type of conduit (e.g. item or fluid or redstone) represents a single network.
 *
 * Each {@link com.codeka.justconduits.common.blocks.ConduitBlockEntity} can have multiple types of conduits, and
 * therefore multiple conduit networks running through it. This class manages access to the networks of conduits
 * running through a particular block.
 *
 * Conduit networks are not saved to disk, but are re-calculated from scratch on chunk load. TODO: is that a good idea?
 *
 * From the conduit network, you can get access to the endpoints (when things go in and out of the network), as well
 * as upgrades and things like that.
 */
public interface IConduitNetworkManager {

}
