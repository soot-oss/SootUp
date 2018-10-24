package de.upb.soot.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2018 Raja Vallée-Rai and others
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

import de.upb.soot.core.Body;

import java.util.List;

public enum ReturnStatementsValidator implements BodyValidator {
  INSTANCE;

  public static ReturnStatementsValidator getInstance() {
    return INSTANCE;
  }

  /**
   * Checks the following invariants on this Jimple body:
   * <ol>
   * <li>this-references may only occur in instance methods
   * <li>this-references may only occur as the first statement in a method, if they occur at all
   * <li>param-references must precede all statements that are not themselves param-references or this-references, if they
   * occur at all
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
     * // A method can have an infinite loop // and no return statement: // // public class Infinite { // public static void
     * main(String[] args) { // int i = 0; while (true) {i += 1;} } } // // Only check that the execution cannot fall off the
     * code. Unit last = body.getUnits().getLast(); if (last instanceof JGotoStmt|| last instanceof JThrowStmt) { return; }
     * 
     * exceptions.add(new ValidationException(body.getMethod(), "The method does not contain a return statement",
     * "Body of method " + body.getMethod().getSignature() + " does not contain a return statement"));
     */
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
