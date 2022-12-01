package sootup.core.views;

import sootup.core.ViewChangeListener;

/**
 * This interface has to be implemented to create a mutable view. It defines methods to register and
 * unregister listeners.
 */
public interface MutableView {

  void addChangeListener(ViewChangeListener listener);

  void removeChangeListener(ViewChangeListener listener);
}
