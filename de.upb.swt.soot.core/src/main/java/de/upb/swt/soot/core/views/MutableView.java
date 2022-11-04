package de.upb.swt.soot.core.views;

import de.upb.swt.soot.core.ViewChangeListener;

public interface MutableView {

  void addChangeListener(ViewChangeListener listener);

  void removeChangeListener(ViewChangeListener listener);
}
