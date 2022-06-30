package de.upb.swt.soot.core;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;

public class BaseViewChangeListener implements ViewChangeListener {
  @Override
  public void classAdded(SootClass sc) {
    System.out.println("class added: " + sc);
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
  public void methodRemoved(SootMethod m) {
    System.out.println("method removed: " + m);
  }
}
