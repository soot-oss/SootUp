package sootup.java.bytecode.interceptors;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Christian Brüggemann
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

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ReferenceType;
import sootup.core.views.View;

/**
 * The CopyPropagator performs cascaded copy propagation. If the propagator encounters situations of
 * the form: A: a = ...; ... B: x = a; ... C: ... = ... x; where a and x are each defined only once
 * (at A and B, respectively), then it can propagate immediately without checking between B and C
 * for redefinitions of a. In this case the propagator is global. Otherwise, if a has multiple
 * definitions then the propagator checks for redefinitions and propagates copies only within
 * extended basic blocks.
 *
 * @author Zun Wang
 */
public class CopyPropagator implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View view) {
    MutableStmtGraph stmtGraph = builder.getStmtGraph();
    for (Stmt stmt : Lists.newArrayList(stmtGraph)) {
      for (Value use : stmt.getUses()) {
        if (use instanceof Local) {
          List<Stmt> defsOfUse = ((Local) use).getDefsForLocalUse(stmtGraph, stmt);

          if (isPropatabable(defsOfUse)) {
            AbstractDefinitionStmt defStmt = (AbstractDefinitionStmt) defsOfUse.get(0);
            Value rhs = defStmt.getRightOp();
            // if rhs is a constant, then replace use, if it is possible
            if (rhs instanceof Constant && !stmt.containsInvokeExpr()) {
              replaceUse(stmtGraph, stmt, use, rhs);
            }
            // if rhs is a cast expr with a ref type and its op is 0 (IntConstant or LongConstant)
            // then replace use, if it is possible
            else if (rhs instanceof JCastExpr && rhs.getType() instanceof ReferenceType) {
              Value op = ((JCastExpr) rhs).getOp();
              if ((op instanceof IntConstant && op.equals(IntConstant.getInstance(0)))
                  || (op instanceof LongConstant && op.equals(LongConstant.getInstance(0)))) {
                replaceUse(stmtGraph, stmt, use, NullConstant.getInstance());
              }
            }
            // if rhs is a local, then replace use, if it is possible
            else if (rhs instanceof Local && !rhs.equivTo(use)) {
              replaceUse(stmtGraph, stmt, use, rhs);
            }
          }
        }
      }
    }
  }

  private void replaceUse(
      @Nonnull MutableStmtGraph graph, @Nonnull Stmt stmt, @Nonnull Value use, @Nonnull Value rhs) {
    if (!use.equivTo(rhs)) { // TODO: ms: check if rhs!=use would be enough
      Stmt newStmt = stmt.withNewUse(use, rhs);
      if (newStmt != null) {
        graph.replaceNode(stmt, newStmt);
      }
    }
  }

  private boolean isPropatabable(@Nonnull List<Stmt> defsOfUse) {
    // If local is defined just one time, then the propagation of this local available.
    boolean isPropagateable = false;
    if (defsOfUse.size() == 1) {
      isPropagateable = true;

      // If local is defined two or more times, and each defStmt in form :
      // defLocal = constant and all constants are same,
      // then the propagation of this local available.

    } else if (defsOfUse.size() > 1) {
      Constant con = null;
      for (Stmt defStmt : defsOfUse) {
        if (defStmt instanceof JAssignStmt
            && ((JAssignStmt) defStmt).getRightOp() instanceof Constant) {
          Constant rhs = (Constant) ((JAssignStmt) defStmt).getRightOp();
          if (con == null) {
            con = rhs;
          } else if (rhs.equals(con)) {
            isPropagateable = true;
          } else {
            isPropagateable = false;
            break;
          }
        } else {
          isPropagateable = false;
          break;
        }
      }
    }
    return isPropagateable;
  }
}
