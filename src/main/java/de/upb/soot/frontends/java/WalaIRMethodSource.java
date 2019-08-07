package de.upb.soot.frontends.java;

import de.upb.soot.core.Body;
import de.upb.soot.frontends.MethodSource;
import de.upb.soot.signatures.MethodSignature;
import javax.annotation.Nonnull;

/** @author Linghui Luo */
public class WalaIRMethodSource implements MethodSource {

  private final MethodSignature methodSignature;
  private final Body body;

  public WalaIRMethodSource(MethodSignature methodSignature, Body body) {
    this.methodSignature = methodSignature;
    this.body = body;
  }

  @Override
  public Body resolveBody() {
    return body;
  }

  @Nonnull
  @Override
  public MethodSignature getSignature() {
    return methodSignature;
  }
}
