package de.upb.sse.sootup.core;

import de.upb.sse.sootup.core.model.SootClass;
import de.upb.sse.sootup.core.model.SootMethod;

public interface ViewChangeListener {
  void classAdded(SootClass sc);

  void classRemoved(SootClass sc);

  void methodAdded(SootMethod m);

  void methodRemoved(SootMethod m);
}
