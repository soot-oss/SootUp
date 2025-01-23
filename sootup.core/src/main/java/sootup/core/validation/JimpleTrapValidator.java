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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import sootup.core.jimple.basic.Trap;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.util.printer.BriefStmtPrinter;
import sootup.core.views.View;

/**
 * This validator checks whether the jimple traps are correct. It does not perform the same checks
 * as {link sootup.validation.TrapsValidator}
 *
 * @see BodyValidator#validate(Body, View)
 * @author Marc Miltenberger
 */
public class JimpleTrapValidator implements BodyValidator {

  /**
   * Checks whether all Caught-Exception-References are associated to traps.
   *
   * @return
   */
  @Override
  public List<ValidationException> validate(Body body, View view) {

    List<ValidationException> exceptions = new ArrayList<>();

    Set<Stmt> caughtStmts = new HashSet<Stmt>();
    BriefStmtPrinter stmtPrinter = new BriefStmtPrinter();
    stmtPrinter.buildTraps(body.getStmtGraph());
    Iterable<Trap> traps = stmtPrinter.getTraps();
    for (Trap trap : traps) {
      caughtStmts.add(trap.getHandlerStmt());
      if (!(trap.getHandlerStmt() instanceof JIdentityStmt)) {
        exceptions.add(
            new ValidationException(
                trap.getHandlerStmt(),
                "Trap handler does not start with caught " + "exception reference"));
      } else {
        JIdentityStmt is = (JIdentityStmt) trap.getHandlerStmt();
        if (!(is.getRightOp() instanceof JCaughtExceptionRef)) {
          exceptions.add(
              new ValidationException(
                  trap.getHandlerStmt(),
                  "Trap handler does not start with caught " + "exception reference"));
        }
      }
    }
    for (Stmt s : body.getStmts()) {
      if (s instanceof JIdentityStmt) {
        JIdentityStmt id = (JIdentityStmt) s;
        if (id.getRightOp() instanceof JCaughtExceptionRef) {
          if (!caughtStmts.contains(id)) {
            exceptions.add(
                new ValidationException(
                    id,
                    "Could not find a corresponding trap using this statement as handler. Body of methodRef "
                        + body.getMethodSignature()
                        + " contains a caught exception reference,"
                        + "but not a corresponding trap using this statement as handler"));
          }
        }
      }
    }

    return exceptions;
  }
}
