package sootup.core.jimple.visitor;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Zun Wang
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

import javax.annotation.Nonnull;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractConditionExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.Expr;
import sootup.core.jimple.common.ref.*;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.*;

/**
 * Replace old use(Value) of a Stmt with a new use(Value)
 *
 * @author Zun Wang
 */
public class ReplaceUseStmtVisitor extends AbstractStmtVisitor<Stmt> {

  @Nonnull private final Value oldUse;
  @Nonnull private final Value newUse;

  final ReplaceUseExprVisitor exprVisitor = new ReplaceUseExprVisitor();
  final ReplaceUseRefVisitor refVisitor = new ReplaceUseRefVisitor();

  public ReplaceUseStmtVisitor(@Nonnull Value oldUse, @Nonnull Value newUse) {
    this.oldUse = oldUse;
    this.newUse = newUse;
  }

  @Override
  public void caseBreakpointStmt(@Nonnull JBreakpointStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseInvokeStmt(@Nonnull JInvokeStmt stmt) {
    Expr invokeExpr = stmt.getInvokeExpr();
    exprVisitor.init(oldUse, newUse);
    invokeExpr.accept(exprVisitor);

    if (exprVisitor.getResult() != invokeExpr) {
      setResult(stmt.withInvokeExpr((AbstractInvokeExpr) exprVisitor.getResult()));
    } else {
      defaultCaseStmt(stmt);
    }
  }

  @Override
  public void caseAssignStmt(@Nonnull JAssignStmt<?, ?> stmt) {

    // uses on the def side..
    final Value leftOp = stmt.getLeftOp();
    if (leftOp instanceof Ref) {
      refVisitor.init(oldUse, newUse);
      ((Ref) leftOp).accept(refVisitor);
      if (refVisitor.getResult() != leftOp) {
        setResult(stmt.withVariable(refVisitor.getResult()));
      }
    }

    // rhs
    Value rValue = stmt.getRightOp();
    if (rValue instanceof Immediate) {
      if (rValue == oldUse) {
        setResult(stmt.withRValue(newUse));
      }
    } else if (rValue instanceof Ref) {
      if (rValue == oldUse) {
        setResult(stmt.withRValue(newUse));
      } else {
        refVisitor.init(oldUse, newUse);
        ((Ref) rValue).accept(refVisitor);
        if (refVisitor.getResult() != rValue) {
          setResult(stmt.withRValue(refVisitor.getResult()));
        }
      }
    } else if (rValue instanceof Expr) {
      exprVisitor.init(oldUse, newUse);
      ((Expr) rValue).accept(exprVisitor);
      if (exprVisitor.getResult() != rValue) {
        setResult(stmt.withRValue(exprVisitor.getResult()));
      }
    } else {
      setResult(stmt);
    }
  }

  @Override
  public void caseIdentityStmt(@Nonnull JIdentityStmt<?> stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseEnterMonitorStmt(@Nonnull JEnterMonitorStmt stmt) {
    setResult(stmt.withOp((Immediate) newUse));
  }

  @Override
  public void caseExitMonitorStmt(@Nonnull JExitMonitorStmt stmt) {
    setResult(stmt.withOp((Immediate) newUse));
  }

  @Override
  public void caseGotoStmt(@Nonnull JGotoStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseIfStmt(@Nonnull JIfStmt stmt) {
    Expr conditionExpr = stmt.getCondition();
    exprVisitor.init(oldUse, newUse);
    conditionExpr.accept(exprVisitor);
    if (exprVisitor.getResult() != conditionExpr) {
      setResult(stmt.withCondition((AbstractConditionExpr) exprVisitor.getResult()));
    } else {
      setResult(stmt);
    }
  }

  @Override
  public void caseNopStmt(@Nonnull JNopStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseRetStmt(@Nonnull JRetStmt stmt) {
    setResult(stmt.withStmtAddress(newUse));
  }

  @Override
  public void caseReturnStmt(@Nonnull JReturnStmt stmt) {
    setResult(stmt.withReturnValue((Immediate) newUse));
  }

  @Override
  public void caseReturnVoidStmt(@Nonnull JReturnVoidStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseSwitchStmt(@Nonnull JSwitchStmt stmt) {
    setResult(stmt.withKey((Immediate) newUse));
  }

  @Override
  public void caseThrowStmt(@Nonnull JThrowStmt stmt) {
    setResult(stmt.withOp((Immediate) newUse));
  }

  public void defaultCaseStmt(@Nonnull Stmt stmt) {
    setResult(stmt);
  }
}
