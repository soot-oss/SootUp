package de.upb.swt.soot.core;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;

public interface ViewChangeListener {
  void classAdded(SootClass sc);

  void classRemoved(SootClass sc);

  void methodAdded(SootMethod m);

  void methodRemoved(SootMethod m);
}
