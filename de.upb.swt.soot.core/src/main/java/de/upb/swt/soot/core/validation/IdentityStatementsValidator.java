package de.upb.swt.soot.core.validation;

import de.upb.swt.soot.core.model.Body;
import java.util.List;

public class IdentityStatementsValidator implements BodyValidator {

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
     * SootMethod methodRef = body.getMethod(); if (methodRef.isAbstract()) { return; }
     *
     * Chain<Unit> units = body.getUnits().getNonPatchingChain();
     *
     * boolean foundNonThisOrParamIdentityStatement = false; boolean firstStatement = true;
     *
     * for (Unit unit : units) { if (unit instanceof IdentityStmt) { IdentityStmt identityStmt = (IdentityStmt) unit; if
     * (identityStmt.getRightOp() instanceof ThisRef) { if (methodRef.isStatic()) { exceptions.add(new
     * ValidationException(identityStmt, "@this-assignment in a static methodRef!")); } if (!firstStatement) {
     * exceptions.add(new ValidationException(identityStmt, "@this-assignment statement should precede all other statements"
     * + "\n methodRef: " + methodRef)); } } else if (identityStmt.getRightOp() instanceof ParameterRef) { if
     * (foundNonThisOrParamIdentityStatement) { exceptions.add(new ValidationException(identityStmt,
     * "@param-assignment statements should precede all non-identity statements" + "\n methodRef: " + methodRef)); } } else {
     * // @caughtexception statement foundNonThisOrParamIdentityStatement = true; } } else { // non-identity statement
     * foundNonThisOrParamIdentityStatement = true; } firstStatement = false; }
     */
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
