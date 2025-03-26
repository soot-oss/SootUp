package sootup.java.core.exceptions;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2025 Zun Wang
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

import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.Expr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.Ref;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.*;
import sootup.core.jimple.visitor.AbstractStmtVisitor;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.types.UnknownType;

public class ExceptionInferStmtVisitor extends AbstractStmtVisitor {

  private ExceptionInferResult result;
  private final TypeHierarchy hierarchy;
  private final ExceptionInferExprVisitor exprVisitor;
  private final ExceptionInferRefVisitor refVisitor;

  public ExceptionInferStmtVisitor(TypeHierarchy hierarchy) {
    this.hierarchy = hierarchy;
    this.refVisitor = new ExceptionInferRefVisitor();
    this.exprVisitor = new ExceptionInferExprVisitor(hierarchy);
    this.result = ExceptionInferResult.createDefaultResult();
  }

  @Override
  public void caseBreakpointStmt(@NonNull JBreakpointStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseInvokeStmt(@NonNull JInvokeStmt stmt) {
    if (stmt.getInvokeExpr().isPresent()) {
      Expr expr = stmt.getInvokeExpr().get();
      expr.accept(exprVisitor);
      result = result.addExceptions(exprVisitor.getResult(), hierarchy);
    }
  }

  @Override
  public void caseAssignStmt(@NonNull JAssignStmt stmt) {
    Value leftOp = stmt.getLeftOp();
    Value rightOp = stmt.getRightOp();
    // store in array
    if (leftOp instanceof Ref) {
      if (leftOp instanceof JArrayRef
          && (leftOp.getType() instanceof UnknownType || leftOp.getType() instanceof ClassType)) {
        result =
            result.addException(
                ExceptionInferResult.ExceptionType.ARRAY_STORE_EXCEPTION, hierarchy);
      }
      ((Ref) leftOp).accept(refVisitor);
      result = result.addExceptions(refVisitor.getResult(), hierarchy);
    }
    if (rightOp instanceof Ref) {
      ((Ref) rightOp).accept(refVisitor);
      result = result.addExceptions(refVisitor.getResult(), hierarchy);
    } else if (rightOp instanceof Expr) {
      ((Expr) rightOp).accept(exprVisitor);
      result = result.addExceptions(exprVisitor.getResult(), hierarchy);
    }
  }

  @Override
  public void caseIdentityStmt(@NonNull JIdentityStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseEnterMonitorStmt(@NonNull JEnterMonitorStmt stmt) {
    result =
        result.addException(ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION, hierarchy);
  }

  @Override
  public void caseExitMonitorStmt(@NonNull JExitMonitorStmt stmt) {
    result =
        result.addException(ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION, hierarchy);
    result =
        result.addException(
            ExceptionInferResult.ExceptionType.ILLEGAL_MONITOR_STATE_EXCEPTION, hierarchy);
  }

  @Override
  public void caseGotoStmt(@NonNull JGotoStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseIfStmt(@NonNull JIfStmt stmt) {
    defaultCaseStmt(stmt);
    // ConditionExpr has no implicit exceptions
    /*Expr conditionExpr = stmt.getCondition();
    conditionExpr.accept(exprVisitor);
    result = result.addExceptions(exprVisitor.getResult(), hierarchy);*/
  }

  @Override
  public void caseNopStmt(@NonNull JNopStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseRetStmt(@NonNull JRetStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseReturnStmt(@NonNull JReturnStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseReturnVoidStmt(@NonNull JReturnVoidStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseSwitchStmt(@NonNull JSwitchStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseThrowStmt(@NonNull JThrowStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void defaultCaseStmt(@NonNull Stmt stmt) {}

  public ExceptionInferResult getResult() {
    return this.result;
  }
}
