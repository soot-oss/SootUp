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
import java.util.Iterator;
import java.util.List;
import sootup.core.IdentifierFactory;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
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
    List<ValidationException> validationException = new ArrayList<>();

    TypeHierarchy typeHierarchy = view.getTypeHierarchy();
    IdentifierFactory identifierFactory = view.getIdentifierFactory();

    for (Stmt stmt : body.getStmts()) {
      if (!stmt.containsInvokeExpr()) {
        continue;
      }

      AbstractInvokeExpr invExpr = stmt.getInvokeExpr();
      MethodSignature callee = invExpr.getMethodSignature();
      List<Immediate> args = invExpr.getArgs();
      List<Type> parameterTypes = callee.getParameterTypes();

      if (invExpr.getArgCount() != parameterTypes.size()) {
        validationException.add(
            new ValidationException(
                stmt,
                "Argument count '"
                    + invExpr.getArgCount()
                    + "' does not match the number of expected parameters '"
                    + parameterTypes.size()
                    + "'."));
        continue;
      }

      // check argument type
      ClassType argClassType;
      Iterator<Type> iterParameters = parameterTypes.iterator();
      for (Immediate arg : args) {
        Type argType = arg.getType();
        Type parameterType = iterParameters.next();

        // handle implicit conversion cases. e.g., `int` is used as an argument of a `double`
        // parameter
        if (argType instanceof PrimitiveType) {
          if (parameterType instanceof PrimitiveType) {

            if (argType == parameterType) {
              continue;
            }

            if (!PrimitiveType.isImplicitlyConvertibleTo(
                (PrimitiveType) argType, (PrimitiveType) parameterType)) {
              validationException.add(
                  new ValidationException(
                      stmt,
                      String.format(
                          "Invalid argument type - type conversion is not applicable to '%s' and the provided '%s'.",
                          parameterType, argType)));
            }
            continue;
          }

          // prepare autoboxing test
          argClassType = identifierFactory.getBoxedType(((PrimitiveType) argType));

        } else {
          argClassType = (ClassType) argType;
        }

        // non-primitive type cases, primitive+autoboxing
        ClassType parameterClassType = (ClassType) parameterType;
        if (argClassType == parameterClassType) {
          continue;
        }
        if (typeHierarchy.contains(parameterClassType)
            && !typeHierarchy.isSubtype(parameterClassType, argClassType)) {
          validationException.add(
              new ValidationException(
                  stmt,
                  String.format(
                      "Invalid argument type. Required '%s' but provided was '%s'.",
                      parameterType, argType)));
        }
      }
    }
    return validationException;
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
