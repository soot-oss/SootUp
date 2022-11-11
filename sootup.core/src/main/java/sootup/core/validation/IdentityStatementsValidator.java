package sootup.core.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Christian Brüggemann, Markus Schmidt and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.List;
import sootup.core.model.Body;

public class IdentityStatementsValidator implements BodyValidator {

  /**
   * Checks the following invariants on this Jimple body:
   *
   * <ol>
   *   <li>this-references may only occur in instance methods
   *   <li>this-references may only occur as the first statement in a method, if they occur at all
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
