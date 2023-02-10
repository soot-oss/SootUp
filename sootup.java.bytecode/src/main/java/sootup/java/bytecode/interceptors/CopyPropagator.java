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
import java.util.*;
import javax.annotation.Nonnull;
import sootup.core.graph.StmtGraph;
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

/** @author Zun Wang */
public class CopyPropagator implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View<?> view) {
    final StmtGraph<?> stmtGraph = builder.getStmtGraph();
    for (Stmt stmt : Lists.newArrayList(stmtGraph)) {
      for (Value use : stmt.getUses()) {
        if (use instanceof Local) {
          List<Stmt> defsOfUse = ((Local) use).getDefsForLocalUse(stmtGraph, stmt);

          if (isPropagable(defsOfUse)) {
            AbstractDefinitionStmt<?, ?> defStmt = (AbstractDefinitionStmt<?, ?>) defsOfUse.get(0);
            Value rhs = defStmt.getRightOp();
            // if rhs is a constant, then replace use, if it is possible
            if (rhs instanceof Constant) {
              replaceUse(builder, stmt, use, rhs);
            }
            // if rhs is a cast expr with a ref type and its op is 0 (IntConstant or LongConstant)
            // then replace use, if it is possible
            else if (rhs instanceof JCastExpr && rhs.getType() instanceof ReferenceType) {
              Value op = ((JCastExpr) rhs).getOp();
              if ((op instanceof IntConstant && op.equals(IntConstant.getInstance(0)))
                  || (op instanceof LongConstant && op.equals(LongConstant.getInstance(0)))) {
                replaceUse(builder, stmt, use, NullConstant.getInstance());
              }
            }
            // if rhs is a local, then replace use, if it is possible
            else if (rhs instanceof Local && !rhs.equivTo(use)) {
              replaceUse(builder, stmt, use, rhs);
            }
          }
        }
      }
    }
  }

  private void replaceUse(
      @Nonnull Body.BodyBuilder builder, @Nonnull Stmt stmt, Value use, Value rhs) {
    Stmt newStmt = stmt.withNewUse(use, rhs);
    // TODO: [ms] check if the following check could be obsolete as checks are already done?
    if (!stmt.equivTo(newStmt)) {
      builder.replaceStmt(stmt, newStmt);
    }
  }

  private boolean isPropagable(List<Stmt> defsOfUse) {
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
