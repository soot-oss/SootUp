package de.upb.swt.soot.core.validation;

import de.upb.swt.soot.core.model.Body;
import java.util.List;

/**
 * Checks whether the types used for locals, methodRef parameters, and methodRef return values are
 * allowed in final Jimple code. This reports an error if a methodRef uses e.g., null_type.
 */
public class TypesValidator implements BodyValidator {

  @Override
  public void validate(Body body, List<ValidationException> exceptions) {
    /*
     * SootMethod methodRef = body.getMethod();
     *
     * if (methodRef != null) { if (!methodRef.getReturnType().isAllowedInFinalCode()) { exceptions.add(new
     * ValidationException(methodRef, "Return type not allowed in final code: " + methodRef.getReturnType(),
     * "return type not allowed in final code:" + methodRef.getReturnType() + "\n methodRef: " + methodRef)); } for (Type t :
     * methodRef.getParameterTypes()) { if (!t.isAllowedInFinalCode()) { exceptions.add(new ValidationException(methodRef,
     * "Parameter type not allowed in final code: " + t, "parameter type not allowed in final code:" + t + "\n methodRef: " +
     * methodRef)); } } } for (Local l : body.getLocals()) { Type t = l.getType(); if (!t.isAllowedInFinalCode()) {
     * exceptions.add(new ValidationException(l, "Local type not allowed in final code: " + t, "(" + methodRef +
     * ") local type not allowed in final code: " + t + " local: " + l)); } }
     */
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
