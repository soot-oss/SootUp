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

/**
 * This validator checks whether the jimple traps are correct. It does not perform the same checks
 * as {link sootup.validation.TrapsValidator}
 *
 * @see JimpleTrapValidator#validate(Body, List)
 * @author Marc Miltenberger
 */
public class JimpleTrapValidator implements BodyValidator {

  /** Checks whether all Caught-Exception-References are associated to traps. */
  @Override
  public void validate(Body body, List<ValidationException> exceptions) {
    // TODO: check copied code from old soot
    /*
     * Set<Unit> caughtUnits = new HashSet<Unit>(); for (Trap trap : body.getTraps()) {
     * caughtUnits.add(trap.getHandlerUnit());
     *
     * if (!(trap.getHandlerUnit() instanceof IdentityStmt)) { exceptions.add(new ValidationException(trap,
     * "Trap handler does not start with caught " + "exception reference")); } else { JIdentityStmt is = (JIdentityStmt)
     * trap.getHandlerUnit(); if (!(is.getRightOp() instanceof CaughtExceptionRef)) { exceptions.add(new
     * ValidationException(trap, "Trap handler does not start with caught " + "exception reference")); } } } for (Unit u :
     * body.getUnits()) { if (u instanceof JIdentityStmt) { JIdentityStmt id = (JIdentityStmt) u; if (id.getRightOp()
     * instanceof CaughtExceptionRef) { if (!caughtUnits.contains(id)) { exceptions.add(new ValidationException(id,
     * "Could not find a corresponding trap using this statement as handler", "Body of methodRef " +
     * body.getMethod().getSignature() + " contains a caught exception reference," +
     * "but not a corresponding trap using this statement as handler")); } } } }
     */
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
