package sootup.core;

import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;

public class BaseViewChangeListener implements ViewChangeListener {
  @Override
  public void classAdded(SootClass sc) {}

  @Override
  public void classRemoved(SootClass sc) {}

  @Override
  public void methodAdded(SootMethod m) {}

  @Override
  public void methodRemoved(SootMethod m) {}
}
