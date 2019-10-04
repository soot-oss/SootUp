package de.upb.swt.soot.core.validation;

import de.upb.swt.soot.core.model.Body;
import java.util.List;

public class MethodValidator implements BodyValidator {

  /**
   * Checks the following invariants on this Jimple body:
   *
   * <ol>
   *   <li>static initializer should have 'static' modifier
   * </ol>
   */
  @Override
  public void validate(Body body, List<ValidationException> exceptions) {
    // TODO: check copied code from old soot
    /*
     * SootMethod methodRef = body.getMethod(); if (methodRef.isAbstract()) { return; } if (methodRef.isStaticInitializer()
     * && !methodRef.isStatic()) { exceptions.add(new ValidationException(methodRef, SootMethod.staticInitializerName +
     * " should be static! Static initializer without 'static'('0x8') modifier" +
     * " will cause problem when running on android platform: " + "\"<clinit> is not flagged correctly wrt/ static\"!")); }
     */
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
