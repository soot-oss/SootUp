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
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;

public class InvokeValidator implements BodyValidator {

  @Override
  public List<ValidationException> validate(Body body, View view) {
    List<ValidationException> validationException = new ArrayList<>();

    ClassType objectClassType = view.getIdentifierFactory().getClassType("java.lang.Object");
    final Optional<? extends SootClass> objClass = view.getClass(objectClassType);
    if (objClass.isPresent()) {
      for (Stmt statement : body.getStmts()) {
        if (statement.isInvokableStmt()) {
          Optional<AbstractInvokeExpr> ieOpt = statement.asInvokableStmt().getInvokeExpr();
          if (ieOpt.isPresent()) {
            final AbstractInvokeExpr ie = ieOpt.get();
            final MethodSignature methodRef = ie.getMethodSignature();
            try {
              Optional<? extends SootMethod> sootMethodOpt = view.getMethod(methodRef);
              if (sootMethodOpt.isPresent()) {
                final SootMethod method = sootMethodOpt.get();
                boolean staticInitializerSubSignature =
                    view.getIdentifierFactory()
                        .isStaticInitializerSubSignature(method.getSubSignature());

                if (staticInitializerSubSignature) {
                  validationException.add(
                      new ValidationException(
                          statement, "Calling <clinit> methods is not allowed."));
                } else if (method.isStatic()) {
                  if (!(ie instanceof JStaticInvokeExpr)) {
                    validationException.add(
                        new ValidationException(
                            statement, "Should use staticinvoke for static methods."));
                  }
                } else {
                  Optional<? extends SootClass> clazzDeclaringOpt =
                      view.getClass(method.getDeclClassType());
                  if (clazzDeclaringOpt.isPresent()) {
                    final SootClass clazzDeclaring = clazzDeclaringOpt.get();
                    if (clazzDeclaring.isInterface()) {
                      if (!(ie instanceof JInterfaceInvokeExpr)) {
                        // There are cases where the Java bytecode verifier allows an
                        // invokevirtual or invokespecial to target an interface method.
                        if (!(ie instanceof JVirtualInvokeExpr
                            || ie instanceof JSpecialInvokeExpr)) {
                          validationException.add(
                              new ValidationException(
                                  statement,
                                  "Should use interface/virtual/specialinvoke for interface methods."));
                        }
                      }
                    } else if (method.isPrivate()
                        || method.isConstructor(view.getIdentifierFactory())) {
                      if (!(ie instanceof JSpecialInvokeExpr)) {
                        String type = method.isPrivate() ? "private methods" : "constructors";
                        validationException.add(
                            new ValidationException(
                                statement, "Should use specialinvoke for " + type + "."));
                      }
                    } else {
                      Optional<? extends SootClass> sootClassOpt =
                          view.getClass(methodRef.getDeclClassType());
                      if (sootClassOpt.isPresent()) {
                        if (sootClassOpt.get().isInterface() && objClass.equals(clazzDeclaring)) {
                          // invokeinterface can be used to invoke the base Object methods
                          if (!(ie instanceof JInterfaceInvokeExpr
                              || ie instanceof JVirtualInvokeExpr
                              || ie instanceof JSpecialInvokeExpr)) {
                            validationException.add(
                                new ValidationException(
                                    statement,
                                    "Should use interface/virtual/specialinvoke for java.lang.Object methods."));
                          }
                        } else {
                          // NOTE: beyond constructors, there's not a rule to separate
                          // super.X from this.X because there exist scenarios where it
                          // is valid to use the exact same references with either a
                          // specialinvoke or a virtualinvoke. Consider classes A and B
                          // where B extends A. Both classes define a method "void m()".
                          // It is legal for a method in B to have either of the these:
                          // - virtualinvoke this.<A: void m()>() //i.e. ((A)this).m()
                          // - specialinvoke this.<A: void m()>() //i.e. super.m()
                          // Both are valid bytecode (although their behavior differs).
                          if (!(ie instanceof JVirtualInvokeExpr
                              || ie instanceof JSpecialInvokeExpr)) {
                            validationException.add(
                                new ValidationException(
                                    statement, "Should use virtualinvoke or specialinvoke."));
                          }
                        }
                      }
                    }
                  }
                }
              }
            } catch (Exception e) {
              // Error on resolving
            }
          }
        }
      }
    }

    return validationException;
  }
}
