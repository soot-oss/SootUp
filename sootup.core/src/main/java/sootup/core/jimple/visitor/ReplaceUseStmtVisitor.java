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

import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.LValue;
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
public class ReplaceUseStmtVisitor extends AbstractStmtVisitor {

  @NonNull protected final Value oldUse;
  @NonNull protected final Value newUse;

  final ReplaceUseExprVisitor exprVisitor = new ReplaceUseExprVisitor();
  final ReplaceUseRefVisitor refVisitor = new ReplaceUseRefVisitor();
  protected Stmt result = null;

  public ReplaceUseStmtVisitor(@NonNull Value oldUse, @NonNull Value newUse) {
    this.oldUse = oldUse;
    this.newUse = newUse;
  }

  @Override
  public void caseBreakpointStmt(@NonNull JBreakpointStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseInvokeStmt(@NonNull JInvokeStmt stmt) {
    Expr invokeExpr = stmt.getInvokeExpr().get();
    exprVisitor.init(oldUse, newUse);
    invokeExpr.accept(exprVisitor);

    if (exprVisitor.getResult() != invokeExpr) {
      setResult(stmt.withInvokeExpr((AbstractInvokeExpr) exprVisitor.getResult()));
    } else {
      defaultCaseStmt(stmt);
    }
  }

  @Override
  public void caseAssignStmt(@NonNull JAssignStmt stmt) {
    // uses on the def side.. e.g. a base in an JArrayRef but NOT with a simple Local!
    final Value leftOp = stmt.getLeftOp();
    if (leftOp instanceof Ref) {
      refVisitor.init(oldUse, newUse);
      ((Ref) leftOp).accept(refVisitor);
      if (refVisitor.getResult() != leftOp) {
        stmt = stmt.withVariable((LValue) refVisitor.getResult());
      }
    }

    // rhs
    Value rValue = stmt.getRightOp();
    if (rValue == oldUse) {
      stmt = stmt.withRValue(newUse);
    } else if (rValue instanceof Ref) {
      try {
        refVisitor.init(oldUse, newUse);
        ((Ref) rValue).accept(refVisitor);
        if (refVisitor.getResult() != rValue) {
          stmt = stmt.withRValue(refVisitor.getResult());
        }
      } catch (ClassCastException cce) {
        // can not replace that local by another Value
      }

    } else if (rValue instanceof Expr) {
      exprVisitor.init(oldUse, newUse);
      ((Expr) rValue).accept(exprVisitor);
      if (exprVisitor.getResult() != rValue) {
        stmt = stmt.withRValue(exprVisitor.getResult());
      }
    }

    defaultCaseStmt(stmt);
  }

  @Override
  public void caseIdentityStmt(@NonNull JIdentityStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseEnterMonitorStmt(@NonNull JEnterMonitorStmt stmt) {
    if (stmt.getOp() == oldUse) {
      setResult(stmt.withOp((Immediate) newUse));
    } else {
      defaultCaseStmt(stmt);
    }
  }

  @Override
  public void caseExitMonitorStmt(@NonNull JExitMonitorStmt stmt) {
    if (stmt.getOp() == oldUse) {
      setResult(stmt.withOp((Immediate) newUse));
    } else {
      defaultCaseStmt(stmt);
    }
  }

  @Override
  public void caseGotoStmt(@NonNull JGotoStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseIfStmt(@NonNull JIfStmt stmt) {
    Expr conditionExpr = stmt.getCondition();
    exprVisitor.init(oldUse, newUse);
    conditionExpr.accept(exprVisitor);
    if (exprVisitor.getResult() != conditionExpr) {
      setResult(stmt.withCondition((AbstractConditionExpr) exprVisitor.getResult()));
    } else {
      defaultCaseStmt(stmt);
    }
  }

  @Override
  public void caseNopStmt(@NonNull JNopStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseRetStmt(@NonNull JRetStmt stmt) {
    if (stmt.getStmtAddress() == oldUse) {
      setResult(stmt.withStmtAddress(newUse));
    } else {
      defaultCaseStmt(stmt);
    }
  }

  @Override
  public void caseReturnStmt(@NonNull JReturnStmt stmt) {
    if (stmt.getOp() == oldUse) {
      setResult(stmt.withReturnValue((Immediate) newUse));
    } else {
      defaultCaseStmt(stmt);
    }
  }

  @Override
  public void caseReturnVoidStmt(@NonNull JReturnVoidStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseSwitchStmt(@NonNull JSwitchStmt stmt) {
    if (stmt.getKey() == oldUse) {
      setResult(stmt.withKey((Immediate) newUse));
    } else {
      defaultCaseStmt(stmt);
    }
  }

  @Override
  public void caseThrowStmt(@NonNull JThrowStmt stmt) {
    if (stmt.getOp() == oldUse) {
      setResult(stmt.withOp((Immediate) newUse));
    } else {
      defaultCaseStmt(stmt);
    }
  }

  public void defaultCaseStmt(@NonNull Stmt stmt) {
    setResult(stmt);
  }

  public Stmt getResult() {
    return result;
  }

  protected void setResult(Stmt result) {
    this.result = result;
  }
}
