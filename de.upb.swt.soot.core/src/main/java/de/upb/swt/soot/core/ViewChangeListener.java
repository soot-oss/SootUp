package de.upb.swt.soot.core;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;

public interface ViewChangeListener {
  void classAdded(SootClass sc);

  void classChanged(SootClass oldClass, SootClass newClass);

  void classRemoved(SootClass sc);

  void methodAdded(SootMethod m);

  void methodChanged(SootMethod oldMethod, SootMethod newMethod);

  void methodRemoved(SootMethod m);
}
