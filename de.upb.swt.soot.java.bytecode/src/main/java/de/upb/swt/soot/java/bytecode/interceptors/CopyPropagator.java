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
import de.upb.swt.soot.core.jimple.common.stmt.AbstractDefinitionStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.*;
import javax.annotation.Nonnull;

public class CopyPropagator implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {
    // Create a map with
    // key: local in the left hand side of a definitionStmt
    // value: times of the corresponding local as definition
    Map<Local, Integer> localToDefCount = new HashMap<>();
    List<Stmt> stmts = builder.getStmts();
    for (Stmt stmt : stmts) {
      if (stmt instanceof AbstractDefinitionStmt
          && ((AbstractDefinitionStmt) stmt).getLeftOp() instanceof Local) {
        Local local = (Local) ((AbstractDefinitionStmt) stmt).getLeftOp();
        if (!localToDefCount.containsKey(local)) {
          localToDefCount.put(local, 1);
        } else {
          Integer oldCount = localToDefCount.get(local);
          localToDefCount.replace(local, oldCount + 1);
        }
      }
    }

    Iterator<Stmt> stmtsIt = builder.getStmtGraph().iterator();
    while (stmtsIt.hasNext()) {
      Stmt stmt = stmtsIt.next();
      for (Value use : stmt.getUses()) {
        if (use instanceof Local) {
          List<Stmt> defsOfUse = InterceptorUtils.getDefsForLocalUse(builder, (Local) use, stmt);

          // If local is defined just one time, then the propagation of this local available.
          boolean propagable = false;
          if (defsOfUse.size() == 1) {
            propagable = true;

            // If local is defined two or more times, and at each time the local is assigned the
            // same constant value,
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

          if (propagable) {
            AbstractDefinitionStmt defStmt = (AbstractDefinitionStmt) defsOfUse.get(0);
            if (defStmt.getRightOp() instanceof Constant) {}
          }
        }
      }
    }
  }

  // ******************assist_functions*************************

}
