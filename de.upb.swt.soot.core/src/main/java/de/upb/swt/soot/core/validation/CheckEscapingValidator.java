package de.upb.swt.soot.core.validation;

import de.upb.swt.soot.core.model.Body;
import java.util.List;

public class CheckEscapingValidator implements BodyValidator {

  @Override
  public void validate(Body body, List<ValidationException> exception) {

    // TODO: check code from old soot below

    /*
     * for (Unit u : body.getUnits()) { if (u instanceof Stmt) { Stmt stmt = (Stmt) u; if (stmt.containsInvokeExpr()) {
     * InvokeExpr iexpr = stmt.getInvokeExpr(); SootMethodRef ref = iexpr.getMethodRef(); if (ref.name().contains("'") ||
     * ref.declaringClass().getName().contains("'")) { throw new ValidationException(stmt,
     * "Escaped name in signature found"); } for (Type paramType : ref.parameterTypes()) { if
     * (paramType.toString().contains("'")) { throw new ValidationException(stmt, "Escaped name in signature found"); } } } }
     * }
     */
  }

  @Override
  public boolean isBasicValidator() {
    return false;
  }
}
