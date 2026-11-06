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
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.Type;
import sootup.core.views.View;

public class CheckEscapingValidator implements BodyValidator {

  @Override
  public List<ValidationException> validate(Body body, View view) {
    List<ValidationException> validationException = new ArrayList<>();

    for (Stmt stmt : body.getStmts()) {
      if (stmt.isInvokableStmt()) {
        Optional<AbstractInvokeExpr> invokeExprOpt = stmt.asInvokableStmt().getInvokeExpr();
        if (invokeExprOpt.isPresent()) {
          MethodSignature ref = invokeExprOpt.get().getMethodSignature();
          if (ref.getName().contains("'")
              || ref.getDeclClassType().getFullyQualifiedName().contains("'")) {
            validationException.add(
                new ValidationException(stmt, "Escaped name found in signature"));
          }
          for (Type paramType : ref.getParameterTypes()) {
            if (paramType.toString().contains("'")) {
              validationException.add(
                  new ValidationException(stmt, "Escaped name found in signature"));
            }
          }
        }
      }
    }
    return validationException;
  }
}
