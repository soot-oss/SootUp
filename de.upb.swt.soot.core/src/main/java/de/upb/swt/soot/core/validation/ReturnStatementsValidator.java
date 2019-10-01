package de.upb.swt.soot.core.validation;

import de.upb.swt.soot.core.model.Body;
import java.util.List;

public class ReturnStatementsValidator implements BodyValidator {

  /**
   * Checks the following invariants on this Jimple body:
   *
   * <ol>
   *   <li>this-references may only occur in instance methods
   *   <li>this-references may only occur as the first statement in a methodRef, if they occur at
   *       all
   *   <li>param-references must precede all statements that are not themselves param-references or
   *       this-references, if they occur at all
   * </ol>
   */
  @Override
  public void validate(Body body, List<ValidationException> exceptions) {

    // TODO: check copied code from old soot
    /*
     * // Checks that this Jimple body actually contains a return statement for (Unit u : body.getUnits()) { if ((u
     * instanceof JReturnStmt) || (u instanceof JReturnVoidStmt) || (u instanceof JRetStmt) || (u instanceof JThrowStmt)) {
     * return; } }
     *
     * // A methodRef can have an infinite loop // and no return statement: // // public class Infinite { // public static
     * void main(String[] args) { // int i = 0; while (true) {i += 1;} } } // // Only check that the execution cannot fall
     * off the code. Unit last = body.getUnits().getLast(); if (last instanceof JGotoStmt|| last instanceof JThrowStmt) {
     * return; }
     *
     * exceptions.add(new ValidationException(body.getMethod(), "The methodRef does not contain a return statement",
     * "Body of methodRef " + body.getMethod().getSignature() + " does not contain a return statement"));
     */
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
