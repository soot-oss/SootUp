package de.upb.swt.soot.java.bytecode.interceptors;
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

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.Constant;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.constant.LongConstant;
import de.upb.swt.soot.core.jimple.common.constant.NullConstant;
import de.upb.swt.soot.core.jimple.common.expr.JCastExpr;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractDefinitionStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.BodyUtils;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.ReferenceType;
import de.upb.swt.soot.core.views.View;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** @author Zun Wang */
public class CopyPropagator implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder, @Nullable View view) {
    for (Stmt stmt : builder.getStmtGraph()) {
      for (Value use : stmt.getUses()) {
        if (use instanceof Local) {
          List<Stmt> defsOfUse =
              BodyUtils.getDefsForLocalUse(builder.getStmtGraph(), (Local) use, stmt);

          if (isPropagable(defsOfUse)) {
            AbstractDefinitionStmt defStmt = (AbstractDefinitionStmt) defsOfUse.get(0);
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

  private void replaceUse(@Nonnull Body.BodyBuilder builder, Stmt stmt, Value use, Value rhs) {
    Stmt newStmt = BodyUtils.withNewUse(stmt, use, rhs);
    if (!stmt.equals(newStmt)) {
      builder.replaceStmt(stmt, newStmt);
    }
  }

  private boolean isPropagable(List<Stmt> defsOfUse) {
    // If local is defined just one time, then the propagation of this local available.
    boolean propagable = false;
    if (defsOfUse.size() == 1) {
      propagable = true;

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
            propagable = true;
          } else {
            propagable = false;
            break;
          }
        } else {
          propagable = false;
          break;
        }
      }
    }
    return propagable;
  }
}
