package sootup.core;

import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;

public interface ViewChangeListener {
  void classAdded(SootClass sc);

  void classRemoved(SootClass sc);

  void methodAdded(SootMethod m);

  void methodRemoved(SootMethod m);
}
