package de.upb.soot.jimple.symbolicreferences;

import de.upb.soot.core.SootMethod;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.views.IView;

/** @author Andreas Dann created on 02.02.19 */
public class MethodRef implements SymbolicRef<SootMethod> {

  // FIXME: for all Ref, IMHO we should only pass the view to it, when we call the "resolve" methodRef
  // e.g., "getField" "getSootMethod" BUT NOT in the constructor...

  private final MethodSignature methodSignature;
  private final boolean isStatic;

  public MethodRef(MethodSignature methodSignature, boolean isStatic) {
    this.methodSignature = methodSignature;
    this.isStatic = isStatic;
  }

  public boolean isStatic() {
    return isStatic;
  }

  /** @return The Soot signature of this methodRef. Used to refer to methods unambiguously. */
  public MethodSignature getSignature() {
    return methodSignature;
  }

  @Override
  public SootMethod resolve() {
    return null;
  }
}
