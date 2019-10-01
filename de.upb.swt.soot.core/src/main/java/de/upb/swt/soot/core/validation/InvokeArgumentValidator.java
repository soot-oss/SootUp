package de.upb.swt.soot.core.validation;

import de.upb.swt.soot.core.model.Body;
import java.util.List;

/**
 * A basic validator that checks whether the length of the invoke statement's argument list matches
 * the length of the target methods's parameter type list.
 *
 * @author Steven Arzt
 */
public class InvokeArgumentValidator implements BodyValidator {

  @Override
  public void validate(Body body, List<ValidationException> exceptions) {
    // TODO: check copied code from old soot
    /*
     * for (Unit u : body.getUnits()) { Stmt s = (Stmt) u; if (s.containsInvokeExpr()) { InvokeExpr iinvExpr =
     * s.getInvokeExpr(); SootMethod callee = iinvExpr.getMethod(); if (callee != null && iinvExpr.getArgCount() !=
     * callee.getParameterCount()) { exceptions.add(new ValidationException(s, "Invalid number of arguments")); } } }
     */
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
