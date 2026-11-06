package sootup.core.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Linghui Luo
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
import java.util.Optional;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.views.View;

/**
 * A basic validator that checks whether the length of the invoke statement's argument list matches
 * the length of the target methods's parameter type list.
 *
 * @author Steven Arzt
 */
public class InvokeArgumentValidator implements BodyValidator {

  @Override
  public List<ValidationException> validate(Body body, View view) {
    List<ValidationException> exception = new ArrayList<>();
    for (Stmt s : body.getStmts()) {
      if (s.isInvokableStmt()) {
        InvokableStmt invokableStmt = s.asInvokableStmt();
        Optional<AbstractInvokeExpr> invokeExpr = invokableStmt.getInvokeExpr();
        if (invokeExpr.isPresent()) {
          AbstractInvokeExpr abstractInvokeExpr = invokeExpr.get();
          Optional<? extends SootMethod> sootMethod =
              view.getMethod(abstractInvokeExpr.getMethodSignature());
          if (sootMethod.isPresent()) {
            if (abstractInvokeExpr.getArgCount() != sootMethod.get().getParameterCount()) {
              exception.add(
                  new ValidationException(
                      s,
                      "Invalid number of arguments passed from "
                          + abstractInvokeExpr
                          + "to method "
                          + sootMethod.get().getSignature()));
            }
          }
        }
      }
    }
    return exception;
  }
}
