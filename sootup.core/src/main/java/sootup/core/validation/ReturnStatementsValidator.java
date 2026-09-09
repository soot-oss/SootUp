package sootup.core.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2025 Sahil Agichani
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
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.types.VoidType;
import sootup.core.views.View;

/**
 * Checks that this Body actually contains a throw or return statement, and that the return
 * statement is of the appropriate type (i.e. void/non-void).
 */
public class ReturnStatementsValidator implements BodyValidator {

  @Override
  public List<ValidationException> validate(Body body, View view) {
    List<ValidationException> validationException = new ArrayList<>();

    Optional<? extends SootMethod> sootMethodOpt = view.getMethod(body.getMethodSignature());
    if (sootMethodOpt.isPresent()) {
      final SootMethod method = sootMethodOpt.get();

      // Checks that this Body actually contains a throw or return statement, and
      // that the return statement is of the appropriate type (i.e. void/non-void)
      for (Stmt u : body.getStmts()) {
        if (u instanceof JThrowStmt) {
          return validationException;
        } else if (u instanceof JReturnStmt) {
          if (!(method.getReturnType() instanceof VoidType)) {
            return validationException;
          }
        } else if (u instanceof JReturnVoidStmt) {
          if (method.getReturnType() instanceof VoidType) {
            return validationException;
          }
        }
      }

      /*

      // A method can have an infinite loop and no return statement:
      // public static void main(String[] args) {
      // int i = 0; while (true) {i += 1;}
      // }
      //
      // Only check that the execution cannot fall off the code.
      Unit last = body.getUnits().getLast();
      while (last instanceof NopStmt) {
          // this can be fine since we can have this as a trap end statement
          last = body.getUnits().getPredOf(last);
      }
      if (last instanceof JGotoStmt || last instanceof GotoInst || last instanceof JThrowStmt || last instanceof ThrowInst) {
          return;
      }

       */

      validationException.add(
          new ValidationException(
              method,
              "The method does not contain a return statement, or the return statement is not of the appropriate type",
              "Body of method "
                  + method.getSignature()
                  + " does not contain a return statement, or the return statement is not of the appropriate type"));
    }

    return validationException;
  }
}
