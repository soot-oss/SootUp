package de.upb.soot.jimple.common.ref;

import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.views.IView;

public class PolymorphicMethodRef extends MethodRef {
  public PolymorphicMethodRef(IView view, MethodSignature methodSignature, boolean isStatic) {
    super(view, methodSignature, isStatic);
  }
}
