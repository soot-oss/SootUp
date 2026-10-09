package sootup.core.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Markus Schmidt, linghui Luo
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
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.*;
import sootup.core.views.View;

public class CheckTypesValidator implements BodyValidator {

  @Override
  public List<ValidationException> validate(Body body, View view) {

    List<ValidationException> validationException = new ArrayList<>();
    final String methodSuffix = " in " + body.getMethodSignature();
    for (Stmt stmt : body.getStmts()) {
      String errorSuffix = " at " + stmt + methodSuffix;

      if (stmt instanceof AbstractDefinitionStmt) {
        AbstractDefinitionStmt astmt = (AbstractDefinitionStmt) stmt;
        if (!(astmt.getRightOp() instanceof JCaughtExceptionRef)) {
          Type leftType = Type.toMachineType(astmt.getLeftOp().getType());
          Type rightType = Type.toMachineType(astmt.getRightOp().getType());

          checkCopy(astmt, validationException, leftType, rightType, errorSuffix, view);
        }
      }
      if (stmt.isInvokableStmt()) {
        Optional<AbstractInvokeExpr> invokeExpr = stmt.asInvokableStmt().getInvokeExpr();
        if (invokeExpr.isPresent()) {
          AbstractInvokeExpr iexpr = invokeExpr.get();
          MethodSignature called = iexpr.getMethodSignature();

          if (iexpr instanceof AbstractInstanceInvokeExpr) {
            checkCopy(
                stmt,
                validationException,
                called.getDeclClassType(),
                iexpr.getType(),
                " in receiver of call" + errorSuffix,
                view);
          }

          final int argCount = iexpr.getArgCount();
          if (called.getParameterTypes().size() != argCount) {
            validationException.add(
                new ValidationException(
                    stmt,
                    "Argument count does not match the signature of the called function"
                        + "Warning: Argument count doesn't match up with signature in call"
                        + errorSuffix));
          } else {
            for (int i = 0; i < argCount; i++) {
              checkCopy(
                  stmt,
                  validationException,
                  Type.toMachineType(called.getParameterType(i)),
                  Type.toMachineType(iexpr.getArg(i).getType()),
                  " in argument "
                      + i
                      + " of call"
                      + errorSuffix
                      + " (Note: Parameters are zero-indexed)",
                  view);
            }
          }
        }
      }
    }
    return validationException;
  }

  private void checkCopy(
      Stmt stmt,
      List<ValidationException> exception,
      Type leftType,
      Type rightType,
      String errorSuffix,
      View view) {
    final ClassType objectClassType = view.getIdentifierFactory().getClassType("java.lang.Object");
    final ClassType serializableClassType =
        view.getIdentifierFactory().getClassType("java.io.Serializable");
    final ClassType cloneableClassType =
        view.getIdentifierFactory().getClassType("java.lang.Cloneable");

    if (leftType instanceof PrimitiveType || rightType instanceof PrimitiveType) {
      if ((leftType instanceof PrimitiveType.IntType && rightType instanceof PrimitiveType.IntType)
          || (leftType instanceof PrimitiveType.LongType
              && rightType instanceof PrimitiveType.LongType)) {
        return;
      }
      if (leftType instanceof PrimitiveType.FloatType
          && rightType instanceof PrimitiveType.FloatType) {
        return;
      }
      if (leftType instanceof PrimitiveType.DoubleType
          && rightType instanceof PrimitiveType.DoubleType) {
        return;
      }

      return;
    }

    if ((rightType instanceof NullType)
        || (leftType instanceof ReferenceType
            && objectClassType.getFullyQualifiedName().equals(leftType.toString()))) {
      return;
    }

    if (leftType instanceof ArrayType || rightType instanceof ArrayType) {
      if (leftType instanceof ArrayType && rightType instanceof ArrayType) {
        return;
      }
      // it is legal to assign arrays to variables of type Serializable, Cloneable or Object
      if (rightType instanceof ArrayType) {
        if (leftType.equals(serializableClassType)
            || leftType.equals(cloneableClassType)
            || leftType.equals(objectClassType)) {
          return;
        }
      }

      exception.add(new ValidationException(stmt, "Warning: Bad use of array type" + errorSuffix));
      return;
    }

    if (leftType instanceof ReferenceType && rightType instanceof ReferenceType) {
      ViewTypeHierarchy viewTypeHierarchy = new ViewTypeHierarchy(view);
      SootClass leftClass = view.getClass((ClassType) leftType).orElse(null);
      SootClass rightClass = view.getClass((ClassType) rightType).orElse(null);
      if (leftClass != null && rightClass != null) {
        if (leftClass.isInterface()) {
          if (rightClass.isInterface()) {
            boolean interfaceSubinterfaceOf =
                viewTypeHierarchy
                    .subinterfacesOf((ClassType) leftType)
                    .anyMatch(x -> x.equals(rightType));
            if (!(leftClass.getName().equals(rightClass.getName()) || interfaceSubinterfaceOf)) {
              exception.add(
                  new ValidationException(
                      stmt, "Warning: Bad use of interface type" + errorSuffix));
            }
          } else {
            // No quick way to check this for now.
          }
        } else if (rightClass.isInterface()) {
          exception.add(
              new ValidationException(
                  stmt,
                  "Warning: trying to use interface type where non-Object class expected"
                      + errorSuffix));
        } else if (viewTypeHierarchy
            .subclassesOf((ClassType) leftType)
            .noneMatch(x -> x.equals(rightType))) {
          exception.add(
              new ValidationException(stmt, "Warning: Bad use of class type" + errorSuffix));
        }
        return;
      }
    }
    exception.add(new ValidationException(stmt, "Warning: Bad types" + errorSuffix));
  }
}
