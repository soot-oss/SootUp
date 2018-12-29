package de.upb.soot.frontends.java;

import de.upb.soot.core.Body;
import de.upb.soot.core.SootMethod;
import de.upb.soot.namespaces.classprovider.IMethodSourceContent;
import de.upb.soot.signatures.MethodSignature;

public class WalaIRMethodSourceContent implements IMethodSourceContent {

  private MethodSignature methodSignature;

  public WalaIRMethodSourceContent(MethodSignature methodSignature) {
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
