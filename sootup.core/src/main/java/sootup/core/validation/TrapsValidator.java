package sootup.core.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Markus Schmidt, Linghui Luo and others
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
import java.util.List;
import sootup.core.jimple.basic.Trap;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.util.printer.BriefStmtPrinter;
import sootup.core.views.View;

public class TrapsValidator implements BodyValidator {

  /**
   * Verifies that the begin, end and handler units of each trap are in this body.
   *
   * @return
   */
  @Override
  public List<ValidationException> validate(Body body, View view) {
    List<ValidationException> exceptions = new ArrayList<>();
    BriefStmtPrinter stmtPrinter = new BriefStmtPrinter();
    stmtPrinter.buildTraps(body.getStmtGraph());
    Iterable<Trap> traps = stmtPrinter.getTraps();

    List<Stmt> stmts = body.getStmts();
    for (Trap t : traps) {
      if (!stmts.contains(t.getBeginStmt()))
        exceptions.add(
            new ValidationException(
                t.getBeginStmt(), "begin not in chain" + " in " + body.getMethodSignature()));

      if (!stmts.contains(t.getEndStmt()))
        exceptions.add(
            new ValidationException(
                t.getEndStmt(), "end not in chain" + " in " + body.getMethodSignature()));

      if (!stmts.contains(t.getHandlerStmt()))
        exceptions.add(
            new ValidationException(
                t.getHandlerStmt(), "handler not in chain" + " in " + body.getMethodSignature()));
    }

    return exceptions;
  }
}
