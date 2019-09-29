package de.upb.soot.core.frontend;

import de.upb.soot.core.model.Body;
import de.upb.soot.core.signatures.MethodSignature;
import javax.annotation.Nonnull;

/** @author Linghui Luo */
public class EagerMethodSource implements MethodSource {

  private final MethodSignature methodSignature;
  private final Body body;

  public EagerMethodSource(MethodSignature methodSignature, Body body) {
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
