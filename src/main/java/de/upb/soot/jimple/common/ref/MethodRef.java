package de.upb.soot.jimple.common.ref;

import de.upb.soot.core.SootMethod;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.views.IView;

import java.util.Optional;

/** @author Andreas Dann created on 02.02.19 */
public class MethodRef {

  //FIXME: for all Ref, IMHO we should pass the view to it, when we call the "resolve" method
  // e.g., "getField" "getSootMethod"

  private final IView view;
  private final MethodSignature methodSignature;
  private final boolean isStatic;

  public MethodRef(IView view, MethodSignature methodSignature, boolean isStatic) {
    this.view = view;
    this.methodSignature = methodSignature;
    this.isStatic = isStatic;
  }

  public boolean isStatic() {
    return isStatic;
  }

  /** @return The Soot signature of this method. Used to refer to methods unambiguously. */
  public MethodSignature getSignature() {
    return methodSignature;
  }

  public Optional<SootMethod> getSootMethod() {
    // put here the logic to get the class
    return null;
  }
}
