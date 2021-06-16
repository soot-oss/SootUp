package de.upb.swt.soot.core.jimple.visitor;

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

import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.expr.AbstractConditionExpr;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.Expr;
import de.upb.swt.soot.core.jimple.common.ref.Ref;
import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.*;
import javax.annotation.Nonnull;

/**
 * Replace old use(Value) of a Stmt with a new use(Value)
 *
 * @author Zun Wang
 */
public class ReplaceUseStmtVisitor extends AbstractStmtVisitor<Stmt> {

  @Nonnull private final Value oldUse;
  @Nonnull private final Value newUse;

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
    ReplaceUseExprVisitor exprVisitor = new ReplaceUseExprVisitor(oldUse, newUse);
    invokeExpr.accept(exprVisitor);
    if (!exprVisitor.getResult().equivTo(invokeExpr)) {
      setResult(stmt.withInvokeExpr((AbstractInvokeExpr) exprVisitor.getResult()));
    } else {
      defaultCaseStmt(stmt);
    }
  }

  @Override
  public void caseAssignStmt(@Nonnull JAssignStmt stmt) {
    Value rValue = stmt.getRightOp();
    Value newRValue = null;

    if (rValue instanceof Immediate) {
      if ((newUse instanceof Immediate) && rValue.equivTo(oldUse)) newRValue = newUse;

    } else if (rValue instanceof Ref) {

      ReplaceUseRefVisitor refVisitor = new ReplaceUseRefVisitor(oldUse, newUse);
      ((Ref) rValue).accept(refVisitor);
      if (!refVisitor.getResult().equivTo(rValue)) {
        newRValue = refVisitor.getResult();
      }

    } else if (rValue instanceof Expr) {

      ReplaceUseExprVisitor exprVisitor = new ReplaceUseExprVisitor(oldUse, newUse);
      ((Expr) rValue).accept(exprVisitor);
      if (!exprVisitor.getResult().equivTo(rValue)) {
        newRValue = exprVisitor.getResult();
      }
    }
    if (newRValue != null) {
      setResult(stmt.withRValue(newRValue));
    } else {
      defaultCaseStmt(stmt);
    }
  }

  @Override
  public void caseIdentityStmt(@Nonnull JIdentityStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseEnterMonitorStmt(@Nonnull JEnterMonitorStmt stmt) {
    if (newUse instanceof Immediate && stmt.getOp().equivTo(oldUse)) {
      setResult(stmt.withOp((Immediate) newUse));
    } else {
      defaultCaseStmt(stmt);
    }
  }

  @Override
  public void caseExitMonitorStmt(@Nonnull JExitMonitorStmt stmt) {
    if (newUse instanceof Immediate && stmt.getOp().equivTo(oldUse)) {
      setResult(stmt.withOp((Immediate) newUse));
    } else {
      defaultCaseStmt(stmt);
    }
  }

  @Override
  public void caseGotoStmt(@Nonnull JGotoStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseIfStmt(@Nonnull JIfStmt stmt) {
    Expr condition = (Expr) stmt.getCondition();
    ReplaceUseExprVisitor exprVisitor = new ReplaceUseExprVisitor(oldUse, newUse);
    condition.accept(exprVisitor);
    if (!exprVisitor.getResult().equivTo(condition)) {
      setResult(stmt.withCondition((AbstractConditionExpr) exprVisitor.getResult()));
    } else {
      defaultCaseStmt(stmt);
    }
  }

  @Override
  public void caseNopStmt(@Nonnull JNopStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseRetStmt(@Nonnull JRetStmt stmt) {
    if (stmt.getStmtAddress().equivTo(oldUse)) {
      setResult(stmt.withStmtAddress(newUse));
    } else {
      defaultCaseStmt(stmt);
    }
  }

  @Override
  public void caseReturnStmt(@Nonnull JReturnStmt stmt) {
    if (stmt.getOp().equivTo(oldUse)) {
      setResult(stmt.withReturnValue((Immediate) newUse));
    } else {
      defaultCaseStmt(stmt);
    }
  }

  @Override
  public void caseReturnVoidStmt(@Nonnull JReturnVoidStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseSwitchStmt(@Nonnull JSwitchStmt stmt) {
    if (newUse instanceof Immediate && stmt.getKey().equivTo(oldUse)) {
      setResult(stmt.withKey((Immediate) newUse));
    } else {
      defaultCaseStmt(stmt);
    }
  }

  @Override
  public void caseThrowStmt(@Nonnull JThrowStmt stmt) {
    if (stmt.getOp().equivTo(oldUse)) {
      setResult(stmt.withOp((Immediate) newUse));
    } else {
      defaultCaseStmt(stmt);
    }
  }

  public void defaultCaseStmt(@Nonnull Stmt stmt) {
    setResult(stmt);
  }
}
