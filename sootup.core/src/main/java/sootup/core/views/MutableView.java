package sootup.core.views;

import sootup.core.ViewChangeListener;

public interface MutableView {

  void addChangeListener(ViewChangeListener listener);

  void removeChangeListener(ViewChangeListener listener);
}
