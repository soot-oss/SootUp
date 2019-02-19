package de.upb.soot.jimple.symbolicreferences;

import de.upb.soot.signatures.MethodSignature;

public class PolymorphicMethodRef extends MethodRef {
  public PolymorphicMethodRef(MethodSignature methodSignature, boolean isStatic) {
    super(methodSignature, isStatic);
  }
}
