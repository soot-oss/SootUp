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
import java.util.Optional;
import sootup.core.jimple.basic.Local;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.types.*;
import sootup.core.views.View;

/**
 * Checks whether the types used for locals, method parameters, and method return values are allowed
 * in final Jimple code. This reports an error if a method uses e.g., null_type.
 */
public class TypesValidator implements BodyValidator {

  @Override
  public List<ValidationException> validate(Body body, View view) {
    List<ValidationException> validationExceptions = new ArrayList<>();

    Optional<? extends SootMethod> sootMethodOpt = view.getMethod(body.getMethodSignature());
    if (sootMethodOpt.isPresent()) {
      SootMethod method = sootMethodOpt.get();
      if (!(method.getReturnType() instanceof PrimitiveType
          || method.getReturnType() instanceof VoidType
          || method.getReturnType() instanceof ReferenceType)) {
        validationExceptions.add(
            new ValidationException(
                method,
                "Return type not allowed in final code: " + method.getReturnType(),
                "return type not allowed in final code:"
                    + method.getReturnType()
                    + "\n methodRef: "
                    + method));
      }
      for (Type t : method.getParameterTypes()) {
        if (!(t instanceof PrimitiveType || t instanceof ReferenceType)) {
          validationExceptions.add(
              new ValidationException(
                  method,
                  "Parameter type not allowed in final code: " + t,
                  "parameter type not allowed in final code:" + t + "\n methodRef: " + method));
        }
      }
      for (Local l : body.getLocals()) {
        Type t = l.getType();
        if (!(t instanceof PrimitiveType
            || t instanceof ReferenceType
            || t instanceof UnknownType)) {
          validationExceptions.add(
              new ValidationException(
                  l,
                  "Local type not allowed in final code: " + t,
                  "(" + method + ") local type not allowed in final code: " + t + " local: " + l));
        }
      }
    } else {
      System.out.println("Error: The method is not found in the view!!");
    }
    return validationExceptions;
  }
}
