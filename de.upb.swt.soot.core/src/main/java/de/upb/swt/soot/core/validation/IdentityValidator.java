package de.upb.swt.soot.core.validation;

import de.upb.swt.soot.core.model.Body;
import java.util.List;

/**
 * This validator checks whether each ParameterRef and ThisRef is used exactly once.
 *
 * @author Marc Miltenberger
 */
public class IdentityValidator implements BodyValidator {

  /** Checks whether each ParameterRef and ThisRef is used exactly once. */
  @Override
  public void validate(Body body, List<ValidationException> exceptions) {
    // TODO: check copied code from old soot
    /*
     * boolean hasThisLocal = false; int paramCount = body.getMethod().getParameterCount(); boolean[] parameterRefs = new
     * boolean[paramCount];
     *
     * for (Unit u : body.getUnits()) { if (u instanceof JIdentityStmt) { JIdentityStmt id = (JIdentityStmt) u; if
     * (id.getRightOp() instanceof JThisRef) { hasThisLocal = true; } if (id.getRightOp() instanceof JParameterRef) {
     * JParameterRef ref = (JParameterRef) id.getRightOp(); if (ref.getIndex() < 0 || ref.getIndex() >= paramCount) { if
     * (paramCount == 0) { exceptions .add(new ValidationException(id,
     * "This methodRef has no parameters, so no parameter reference is allowed")); } else { exceptions.add(new
     * ValidationException(id, String.format("Parameter reference index must be between 0 and %d (inclusive)", paramCount -
     * 1))); } return; } if (parameterRefs[ref.getIndex()]) { exceptions.add( new ValidationException(id,
     * String.format("Only one local for parameter %d is allowed", ref.getIndex()))); } parameterRefs[ref.getIndex()] = true;
     * } } }
     *
     * if (!body.getMethod().isStatic() && !hasThisLocal) { exceptions.add(new ValidationException(body,
     * String.format("The methodRef %s is not static, but does not have a this local", body.getMethod().getSignature()))); }
     *
     * for (int i = 0; i < paramCount; i++) { if (!parameterRefs[i]) { exceptions .add(new ValidationException(body,
     * String.format("There is no parameter local for parameter number %d", i))); } }
     *
     */
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
