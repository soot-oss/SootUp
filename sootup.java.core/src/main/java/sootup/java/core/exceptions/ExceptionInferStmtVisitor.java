package sootup.java.core.exceptions;

import javax.annotation.Nonnull;
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
  public void caseBreakpointStmt(@Nonnull JBreakpointStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseInvokeStmt(@Nonnull JInvokeStmt stmt) {
    if (stmt.getInvokeExpr().isPresent()) {
      Expr expr = stmt.getInvokeExpr().get();
      expr.accept(exprVisitor);
      result = result.addExceptions(exprVisitor.getResult(), hierarchy);
    }
  }

  @Override
  public void caseAssignStmt(@Nonnull JAssignStmt stmt) {
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
  public void caseIdentityStmt(@Nonnull JIdentityStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseEnterMonitorStmt(@Nonnull JEnterMonitorStmt stmt) {
    result =
        result.addException(ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION, hierarchy);
  }

  @Override
  public void caseExitMonitorStmt(@Nonnull JExitMonitorStmt stmt) {
    result =
        result.addException(ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION, hierarchy);
    result =
        result.addException(
            ExceptionInferResult.ExceptionType.ILLEGAL_MONITOR_STATE_EXCEPTION, hierarchy);
  }

  @Override
  public void caseGotoStmt(@Nonnull JGotoStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseIfStmt(@Nonnull JIfStmt stmt) {
    defaultCaseStmt(stmt);
    // ConditionExpr has no implicit exceptions
    /*Expr conditionExpr = stmt.getCondition();
    conditionExpr.accept(exprVisitor);
    result = result.addExceptions(exprVisitor.getResult(), hierarchy);*/
  }

  @Override
  public void caseNopStmt(@Nonnull JNopStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseRetStmt(@Nonnull JRetStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseReturnStmt(@Nonnull JReturnStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseReturnVoidStmt(@Nonnull JReturnVoidStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseSwitchStmt(@Nonnull JSwitchStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseThrowStmt(@Nonnull JThrowStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void defaultCaseStmt(@Nonnull Stmt stmt) {}

  public ExceptionInferResult getResult() {
    return this.result;
  }
}
