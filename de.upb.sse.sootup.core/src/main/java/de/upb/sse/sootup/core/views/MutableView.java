package de.upb.sse.sootup.core.views;

import de.upb.sse.sootup.core.ViewChangeListener;

public interface MutableView {

  void addChangeListener(ViewChangeListener listener);

  void removeChangeListener(ViewChangeListener listener);
}
