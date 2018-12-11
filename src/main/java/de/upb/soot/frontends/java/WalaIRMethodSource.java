package de.upb.soot.frontends.java;

import de.upb.soot.core.Body;
import de.upb.soot.core.SootMethod;
import de.upb.soot.namespaces.classprovider.IMethodSource;
import de.upb.soot.signatures.MethodSignature;

public class WalaIRMethodSource implements IMethodSource {

  private MethodSignature methodSignature;

  public WalaIRMethodSource(MethodSignature methodSignature) {
    this.methodSignature = methodSignature;
  }

  @Override
  public Body getBody(SootMethod m) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public MethodSignature getSignature() {
    return methodSignature;
  }

}
