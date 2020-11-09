package de.upb.swt.soot.core;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;

public class BaseViewChangeListener implements ViewChangeListener {
  @Override
  public void classAdded(SootClass sc) {
    System.out.println("class added: " + sc);
  }

  @Override
  public void classChanged(SootClass oldClass, SootClass newClass) {
    System.out.println("class changed: " + oldClass + " to " + newClass);
  }

  @Override
  public void classRemoved(SootClass sc) {
    System.out.println("class removed: " + sc);
  }

  @Override
  public void methodAdded(SootMethod m) {
    System.out.println("method added: " + m);
  }

  @Override
  public void methodChanged(SootMethod oldMethod, SootMethod newMethod) {
    System.out.println("method changed: " + oldMethod + " to " + newMethod);
  }

  @Override
  public void methodRemoved(SootMethod m) {
    System.out.println("method removed: " + m);
  }
}
