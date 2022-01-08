package com.codeka.justconduits.client.gui.widgets;

/**
 * Represents a source of data for a piece of the GUI. This allows us to easily track the value in real-time and also
 * handle callbacks.
 */
public interface DataSource<T> {
  T getValue();
  void setValue(T value);
}
