/* Soot - a J*va Optimization Framework
 * Copyright (C) 2003 Ondrej Lhotak
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package de.upb.soot.jimple.visitor;

import de.upb.soot.jimple.Local;
import de.upb.soot.jimple.Value;
import de.upb.soot.jimple.common.constant.Constant;
import de.upb.soot.jimple.common.expr.AbstractInvokeExpr;
import de.upb.soot.jimple.common.expr.Expr;
import de.upb.soot.jimple.common.expr.JCastExpr;
import de.upb.soot.jimple.common.expr.JNewArrayExpr;
import de.upb.soot.jimple.common.expr.JNewExpr;
import de.upb.soot.jimple.common.expr.JNewMultiArrayExpr;
import de.upb.soot.jimple.common.ref.AbstractInstanceFieldRef;
import de.upb.soot.jimple.common.ref.ArrayRef;
import de.upb.soot.jimple.common.ref.CaughtExceptionRef;
import de.upb.soot.jimple.common.ref.IdentityRef;
import de.upb.soot.jimple.common.ref.StaticFieldRef;
import de.upb.soot.jimple.common.stmt.JAssignStmt;
import de.upb.soot.jimple.common.stmt.JIdentityStmt;
import de.upb.soot.jimple.common.stmt.JInvokeStmt;
import de.upb.soot.jimple.common.stmt.JReturnStmt;
import de.upb.soot.jimple.common.stmt.JReturnVoidStmt;
import de.upb.soot.jimple.common.stmt.JThrowStmt;
import de.upb.soot.jimple.common.stmt.Stmt;
import de.upb.soot.jimple.common.type.ArrayType;
import de.upb.soot.jimple.common.type.RefType;
import javassist.compiler.ast.CastExpr;

public abstract class PointerStmtVisitor extends AbstractStmtVisitor {
  Stmt statement;

  /** A statement of the form l = constant; */
  protected abstract void caseAssignConstStmt(Value dest, Constant c);

  /** A statement of the form l = v; */
  protected abstract void caseCopyStmt(Local dest, Local src);

  /** A statement of the form l = (cl) v; */
  protected void caseCastStmt(Local dest, Local src, JCastExpr c) {
    // default is to just ignore the cast
    caseCopyStmt(dest, src);
  }

  /** An identity statement assigning a parameter to a local. */
  protected abstract void caseIdentityStmt(Local dest, IdentityRef src);

  /** A statement of the form l1 = l2.f; */
  protected abstract void caseLoadStmt(Local dest, AbstractInstanceFieldRef src);

  /** A statement of the form l1.f = l2; */
  protected abstract void caseStoreStmt(AbstractInstanceFieldRef dest, Local src);

  /** A statement of the form l1 = l2[i]; */
  protected abstract void caseArrayLoadStmt(Local dest, ArrayRef src);

  /** A statement of the form l1[i] = l2; */
  protected abstract void caseArrayStoreStmt(ArrayRef dest, Local src);

  /** A statement of the form l = cl.f; */
  protected abstract void caseGlobalLoadStmt(Local dest, StaticFieldRef src);

  /** A statement of the form cl.f = l; */
  protected abstract void caseGlobalStoreStmt(StaticFieldRef dest, Local src);

  /** A return statement. e is null if a non-reference type is returned. */
  protected abstract void caseReturnStmt(Local val);

  /** A return statement returning a constant. */
  protected void caseReturnConstStmt(Constant val) {
    // default is uninteresting
    caseUninterestingStmt(statement);
  }

  /** Any type of new statement (NewStmt, NewArrayStmt, NewMultiArrayStmt) */
  protected abstract void caseAnyNewStmt(Local dest, Expr e);

  /** A new statement */
  protected void caseNewStmt(Local dest, JNewExpr e) {
    caseAnyNewStmt(dest, e);
  }

  /** A newarray statement */
  protected void caseNewArrayStmt(Local dest, JNewArrayExpr e) {
    caseAnyNewStmt(dest, e);
  }

  /** A anewarray statement */
  protected void caseNewMultiArrayStmt(Local dest, JNewMultiArrayExpr e) {
    caseAnyNewStmt(dest, e);
  }

  /** A method invocation. dest is null if there is no reference type return value. */
  protected abstract void caseInvokeStmt(Local dest, AbstractInvokeExpr e);

  /** A throw statement */
  protected void caseThrowStmt(Local thrownException) {
    caseUninterestingStmt(statement);
  }

  /** A catch statement */
  protected void caseCatchStmt(Local dest, CaughtExceptionRef cer) {
    caseUninterestingStmt(statement);
  }

  /** Any other statement */
  protected void caseUninterestingStmt(Stmt s) {
  };

  @Override
  public final void caseAssignStmt(JAssignStmt s) {
    statement = s;
    Value lhs = s.getLeftOp();
    Value rhs = s.getRightOp();
    if (!(lhs.getType() instanceof RefType) && !(lhs.getType() instanceof ArrayType)) {
      if (rhs instanceof AbstractInvokeExpr) {
        caseInvokeStmt(null, (AbstractInvokeExpr) rhs);
        return;
      }
      caseUninterestingStmt(s);
      return;
    }
    if (rhs instanceof AbstractInvokeExpr) {
      caseInvokeStmt((Local) lhs, (AbstractInvokeExpr) rhs);
      return;
    }
    if (lhs instanceof Local) {
      if (rhs instanceof Local) {
        caseCopyStmt((Local) lhs, (Local) rhs);
      } else if (rhs instanceof AbstractInstanceFieldRef) {
        caseLoadStmt((Local) lhs, (AbstractInstanceFieldRef) rhs);
      } else if (rhs instanceof ArrayRef) {
        caseArrayLoadStmt((Local) lhs, (ArrayRef) rhs);
      } else if (rhs instanceof StaticFieldRef) {
        caseGlobalLoadStmt((Local) lhs, (StaticFieldRef) rhs);
      } else if (rhs instanceof JNewExpr) {
        caseNewStmt((Local) lhs, (JNewExpr) rhs);
      } else if (rhs instanceof JNewArrayExpr) {
        caseNewArrayStmt((Local) lhs, (JNewArrayExpr) rhs);
      } else if (rhs instanceof JNewMultiArrayExpr) {
        caseNewMultiArrayStmt((Local) lhs, (JNewMultiArrayExpr) rhs);
      } else if (rhs instanceof CastExpr) {
        JCastExpr r = (JCastExpr) rhs;
        Value rv = r.getOp();
        if (rv instanceof Constant) {
          caseAssignConstStmt(lhs, (Constant) rv);
        } else {
          caseCastStmt((Local) lhs, (Local) rv, r);
        }
      } else if (rhs instanceof Constant) {
        caseAssignConstStmt(lhs, (Constant) rhs);
      } else {
        throw new RuntimeException("unhandled stmt " + s);
      }
    } else if (lhs instanceof AbstractInstanceFieldRef) {
      if (rhs instanceof Local) {
        caseStoreStmt((AbstractInstanceFieldRef) lhs, (Local) rhs);
      } else if (rhs instanceof Constant) {
        caseAssignConstStmt(lhs, (Constant) rhs);
      } else {
        throw new RuntimeException("unhandled stmt " + s);
      }
    } else if (lhs instanceof ArrayRef) {
      if (rhs instanceof Local) {
        caseArrayStoreStmt((ArrayRef) lhs, (Local) rhs);
      } else if (rhs instanceof Constant) {
        caseAssignConstStmt(lhs, (Constant) rhs);
      } else {
        throw new RuntimeException("unhandled stmt " + s);
      }
    } else if (lhs instanceof StaticFieldRef) {
      if (rhs instanceof Local) {
        caseGlobalStoreStmt((StaticFieldRef) lhs, (Local) rhs);
      } else if (rhs instanceof Constant) {
        caseAssignConstStmt(lhs, (Constant) rhs);
      } else {
        throw new RuntimeException("unhandled stmt " + s);
      }
    } else if (rhs instanceof Constant) {
      caseAssignConstStmt(lhs, (Constant) rhs);
    } else {
      throw new RuntimeException("unhandled stmt " + s);
    }
  }

  @Override
  public final void caseReturnStmt(JReturnStmt s) {
    statement = s;
    Value op = s.getOp();
    if (op.getType() instanceof RefType || op.getType() instanceof ArrayType) {
      if (op instanceof Constant) {
        caseReturnConstStmt((Constant) op);
      } else {
        caseReturnStmt((Local) op);
      }
    } else {
      caseReturnStmt((Local) null);
    }
  }

  @Override
  public final void caseReturnVoidStmt(JReturnVoidStmt s) {
    statement = s;
    caseReturnStmt((Local) null);
  }

  @Override
  public final void caseInvokeStmt(JInvokeStmt s) {
    statement = s;
    caseInvokeStmt(null, s.getInvokeExpr());
  }

  @Override
  public final void caseIdentityStmt(JIdentityStmt s) {
    statement = s;
    Value lhs = s.getLeftOp();
    Value rhs = s.getRightOp();
    if (!(lhs.getType() instanceof RefType) && !(lhs.getType() instanceof ArrayType)) {
      caseUninterestingStmt(s);
      return;
    }
    Local llhs = (Local) lhs;
    if (rhs instanceof CaughtExceptionRef) {
      caseCatchStmt(llhs, (CaughtExceptionRef) rhs);
    } else {
      IdentityRef rrhs = (IdentityRef) rhs;
      caseIdentityStmt(llhs, rrhs);
    }
  }

  @Override
  public final void caseThrowStmt(JThrowStmt s) {
    statement = s;
    caseThrowStmt((Local) s.getOp());
  }
}
