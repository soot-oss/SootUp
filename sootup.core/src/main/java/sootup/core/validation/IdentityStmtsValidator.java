package sootup.core.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Markus Schmidt and others
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
import java.util.Optional;
import java.util.Set;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.ref.JThisRef;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.views.View;

/**
 * This validator checks whether each ParameterRef and ThisRef is used exactly once.
 *
 * @author Marc Miltenberger
 */
public class IdentityStmtsValidator implements BodyValidator {

  /**
   * Checks whether each ParameterRef and ThisRef is used exactly once.
   *
   * @return a list of found validation exceptions
   */
  @Override
  public List<ValidationException> validate(Body body, View view) {
    List<ValidationException> exceptions = new ArrayList<>();

    boolean hasThisLocal = false;
    Optional<? extends SootMethod> sootMethodOpt = view.getMethod(body.getMethodSignature());
    if (!sootMethodOpt.isPresent()) {
      throw new IllegalStateException(
          "We should find the given method to the given Body in the View. wrong View or Method?");
    }

    SootMethod method = sootMethodOpt.get();
    int paramCount = method.getParameterCount();
    boolean[] parameterRefs = new boolean[paramCount];

    for (Stmt stmt : body.getControlFlowGraph().getNodes()) {
      if (stmt instanceof JIdentityStmt) {
        JIdentityStmt identityStmt = (JIdentityStmt) stmt;
        if (identityStmt.getRightOp() instanceof JThisRef) {
          if (hasThisLocal) {
            exceptions.add(new ValidationException(identityStmt, "@this occures more than once."));
          }
          hasThisLocal = true;
        } else if (identityStmt.getRightOp() instanceof JParameterRef) {
          JParameterRef ref = (JParameterRef) identityStmt.getRightOp();
          if (ref.getIndex() < 0 || ref.getIndex() >= paramCount) {
            if (paramCount == 0) {
              exceptions.add(
                  new ValidationException(
                      identityStmt,
                      "This methodRef has no parameters, so no parameter reference is allowed"));
            } else {
              exceptions.add(
                  new ValidationException(
                      identityStmt,
                      String.format(
                          "Parameter reference index must be between 0 and %d (inclusive)",
                          paramCount - 1)));
            }
          } else {
            if (parameterRefs[ref.getIndex()]) {
              exceptions.add(
                  new ValidationException(
                      identityStmt,
                      String.format("Only one local for parameter %d is allowed", ref.getIndex())));
            }
            parameterRefs[ref.getIndex()] = true;
          }
        }
      }
    }

    // the @this and @parameter identity stmts form the preamble of the body: they come first, @this
    // before the parameters, and nowhere else
    ControlFlowGraph<?> graph = body.getControlFlowGraph();
    Set<Stmt> preamble = new HashSet<>();
    Stmt stmt = graph.getStartingStmt();
    while (isThisOrParameterAssignment(stmt) && preamble.add(stmt)) {
      if (((JIdentityStmt) stmt).getRightOp() instanceof JThisRef && preamble.size() > 1) {
        exceptions.add(
            new ValidationException(stmt, "@this-assignment must precede all other statements"));
      }
      List<Stmt> successors = graph.successors(stmt);
      if (successors.size() != 1) {
        break;
      }
      stmt = successors.get(0);
    }
    for (Stmt node : graph.getNodes()) {
      if (isThisOrParameterAssignment(node) && !preamble.contains(node)) {
        exceptions.add(
            new ValidationException(
                node, "@this- and @parameter-assignments must precede all other statements"));
      }
    }

    if (method.isStatic() == hasThisLocal) {
      exceptions.add(
          new ValidationException(
              body,
              String.format(
                  "The method %s is %s static, but does %s have a this local",
                  body.getMethodSignature(),
                  (method.isStatic() ? "" : "not"),
                  (hasThisLocal ? "" : "not"))));
    }

    for (int i = 0; i < paramCount; i++) {
      if (!parameterRefs[i]) {
        exceptions.add(
            new ValidationException(
                body, String.format("There is no Local assigned for parameter number %d", i)));
      }
    }
    return exceptions;
  }

  private static boolean isThisOrParameterAssignment(Stmt stmt) {
    return stmt instanceof JIdentityStmt
        && (((JIdentityStmt) stmt).getRightOp() instanceof JThisRef
            || ((JIdentityStmt) stmt).getRightOp() instanceof JParameterRef);
  }
}
