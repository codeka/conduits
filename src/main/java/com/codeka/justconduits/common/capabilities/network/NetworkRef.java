package com.codeka.justconduits.common.capabilities.network;

/**
 * A simple class that holds a reference to a network ID.
 *
 * As we populate a network, we might meet a chunk border that is already loaded and populated. Rather than having to
 * go back through all the existing nodes we've visited and update the network ID to the existing one again, we can
 * just update a single NetworkRef that all the nodes we've populated so far share.
 */
public class NetworkRef {
  private long id;

  public NetworkRef(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof NetworkRef other) {
      return other.id == id;
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format("->%d", id);
  }
}
