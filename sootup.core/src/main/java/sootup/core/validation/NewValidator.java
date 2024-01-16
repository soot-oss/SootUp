package sootup.core.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Christian Brüggemann, Markus Schmidt, Akshita Dubey and others
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
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.ReferenceType;
import sootup.core.types.UnknownType;
import sootup.core.views.View;

public class NewValidator implements BodyValidator {

  private static final String ERROR_MSG =
      "There is a path from '%s' to the usage '%s' where <init> does not get called in between.";

  public static boolean MUST_CALL_CONSTRUCTOR_BEFORE_RETURN = false;

  //  Checks whether after each new-instruction a constructor call follows.
  @Override
  public List<ValidationException> validate(Body body, View view) {

    List<ValidationException> exceptions = new ArrayList<>();

    StmtGraph g = body.getStmtGraph();
    for (Stmt u : body.getStmts()) {
      if (u instanceof JAssignStmt) {
        JAssignStmt assign = (JAssignStmt) u;

        // First seek for a JNewExpr.
        if (assign.getRightOp() instanceof JNewExpr) {
          if (!((assign.getLeftOp().getType() instanceof ReferenceType)
              || assign.getLeftOp().getType() instanceof UnknownType)) {
            exceptions.add(
                new ValidationException(
                    assign.getLeftOp(),
                    String.format(
                        "Body of methodRef %s contains a new-expression, which is assigned to a non-reference local",
                        body.getMethodSignature())));
            return exceptions;
          }

          checkForInitializerOnPath(g, assign, exceptions);
        }
      }
    }
    return exceptions;
  }

  private boolean checkForInitializerOnPath(
      StmtGraph g, JAssignStmt newStmt, List<ValidationException> exception) {
    List<Stmt> workList = new ArrayList<>();
    Set<Stmt> doneSet = new HashSet<>();
    workList.add(newStmt);

    Set<Local> aliasingLocals = new HashSet<>();
    aliasingLocals.add((Local) newStmt.getLeftOp());

    while (!workList.isEmpty()) {
      Stmt curStmt = workList.remove(0);
      if (!doneSet.add(curStmt)) {
        continue;
      }
      if (!newStmt.equals(curStmt)) {
        if (curStmt.containsInvokeExpr()) {
          AbstractInvokeExpr expr = curStmt.getInvokeExpr();
          if (!(expr instanceof JSpecialInvokeExpr)) {
            exception.add(
                new ValidationException(
                    curStmt.getInvokeExpr(),
                    "<init> methodRef calls may only be used with specialinvoke.")); // At least we
            // found an initializer, so we return true...
            return true;
          }
          if (!(curStmt instanceof JInvokeStmt)) {
            exception.add(
                new ValidationException(
                    curStmt.getInvokeExpr(),
                    "<init> methods may only be called with invoke statements.")); // At least we
            // found an initializer, so we return true...
            return true;
          }

          JSpecialInvokeExpr invoke = (JSpecialInvokeExpr) expr;
          if (aliasingLocals.contains(invoke.getBase())) {
            // We are happy now,continue the loop and check other paths
            continue;
          }
        }

        // We are still in the loop, so this was not the constructor call we were looking for
        boolean creatingAlias = false;
        if (curStmt instanceof JAssignStmt) {
          JAssignStmt assignCheck = (JAssignStmt) curStmt;
          if (aliasingLocals.contains(assignCheck.getRightOp())) {
            if (assignCheck.getLeftOp() instanceof Local) {
              // A new alias is created.
              aliasingLocals.add((Local) assignCheck.getLeftOp());
              creatingAlias = true;
            }
          }
          Local originalLocal = aliasingLocals.iterator().next();
          if (originalLocal.equals(assignCheck.getLeftOp())) { // In case of dead assignments:

            // Handles cases like // r0 = new x; // r0 = null;

            // But not cases like // r0 = new x; // r1 = r0; // r1 = null; // Because we check
            // for the original local
            continue;
          } else {
            // Since the local on the left hand side gets overwritten
            // even if it was aliasing with our original local,
            // now it does not any more...
            aliasingLocals.remove(assignCheck.getLeftOp());
          }
        }

        if (!creatingAlias) {
          for (Value box : curStmt.getUses()) {
            if (aliasingLocals.contains(box)) {
              // The current unit uses one of the aliasing locals, but
              // there was no initializer in between.
              // However, when creating such an alias, the use is okay.
              exception.add(
                  new ValidationException(
                      newStmt.getLeftOp(), String.format(ERROR_MSG, newStmt, curStmt)));
              return false;
            }
          }
        }
      }
      // Enqueue the successors
      List successors = g.successors(curStmt);
      if (successors.isEmpty() && MUST_CALL_CONSTRUCTOR_BEFORE_RETURN) {
        // This means that we are e.g.at the end of
        // the methodRef // There was no <init> call
        // on our way...
        exception.add(
            new ValidationException(
                newStmt.getLeftOp(), String.format(ERROR_MSG, newStmt, curStmt)));
        return false;
      }
      workList.addAll(successors);
    }
    return true;
  }

  @Override
  public boolean isBasicValidator() {
    return false;
  }
}
