package sootup.interceptors;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.*;
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

  static final IntConstant zeroIntConstInstance = IntConstant.getInstance(0);
  static final LongConstant zeroLongConstInstance = LongConstant.getInstance(0);

  @Override
  public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {
    MutableControlFlowGraph controlFlowGraph = builder.getControlFlowGraph();
    for (Stmt stmt : Lists.newArrayList(controlFlowGraph)) {
      Stmt newStmt = stmt;
      Set<Value> valueList = new HashSet<>(newStmt.getUses());
      for (Value use : valueList) {
        if (!(use instanceof Local)) {
          continue;
        }

        List<Stmt> defsOfUse = ((Local) use).getDefsForLocalUse(controlFlowGraph, newStmt);
        if (!isPropatabable(defsOfUse)) {
          continue;
        }

        AbstractDefinitionStmt defStmt = (AbstractDefinitionStmt) defsOfUse.get(0);
        Value rhs = defStmt.getRightOp();
        // if rhs is a constant, then replace use, if it is possible
        if (rhs instanceof Constant) {
          newStmt = replaceUse(controlFlowGraph, newStmt, use, rhs);
          // Simplifying trivial binary expression further for copy propagation

          if (newStmt instanceof JAssignStmt) {
            Value newRhs = ((JAssignStmt) newStmt).getRightOp();
            Constant foldedRhs = Evaluator.getConstantValueOf(newRhs);
            if (foldedRhs != null) {
              JAssignStmt modifiedStmt = ((JAssignStmt) newStmt).withRValue(foldedRhs);
              controlFlowGraph.replaceNode(newStmt, modifiedStmt);
            }
          }
        }

        // if rhs is a cast expr with a ref type and its op is 0 (IntConstant or LongConstant)
        // then replace use, if it is possible
        else if (rhs instanceof JCastExpr && rhs.getType() instanceof ReferenceType) {
          Value op = ((JCastExpr) rhs).getOp();

          if (zeroIntConstInstance.equals(op) || zeroLongConstInstance.equals(op)) {
            newStmt = replaceUse(controlFlowGraph, newStmt, use, NullConstant.getInstance());
          }
        }
        // if rhs is a local, then replace use, if it is possible
        else if (rhs instanceof Local && !rhs.equivTo(use)) {
          Local m = (Local) rhs;
          if (use != m) {
            Integer defCount = m.getDefs(controlFlowGraph.getStmts()).size();
            if (defCount == 0) {
              throw new IllegalStateException("Local `" + m + "' is used without a definition!");
            } else if (defCount == 1) {
              newStmt = replaceUse(controlFlowGraph, newStmt, use, rhs);
              continue;
            }

            List<Stmt> path = controlFlowGraph.getExtendedBasicBlockPathBetween(defStmt, newStmt);
            if (path == null) {
              // no path in the extended basic block
              continue;
            }
            {
              boolean isRedefined = false;
              Iterator<Stmt> pathIt = path.iterator();
              // Skip first node
              pathIt.next();
              // Make sure that m is not redefined along path
              while (pathIt.hasNext()) {
                Stmt s = (Stmt) pathIt.next();
                if (newStmt == s) {
                  // Don't look at the last statement
                  // since it is evaluated after the uses.
                  break;
                }
                if (s instanceof AbstractDefinitionStmt) {
                  if (((AbstractDefinitionStmt) s).getLeftOp() == m) {
                    isRedefined = true;
                    break;
                  }
                }
              }

              if (isRedefined) {
                continue;
              }
            }
            newStmt = replaceUse(controlFlowGraph, newStmt, use, rhs);
          }
        }
      }
    }
  }

  private Stmt replaceUse(
      @NonNull MutableControlFlowGraph graph,
      @NonNull Stmt stmt,
      @NonNull Value use,
      @NonNull Value rhs) {
    if (rhs != use) {
      Stmt newStmt = stmt.withNewUse(use, rhs);
      if (newStmt != stmt) {
        graph.replaceNode(stmt, newStmt);
      }
      return newStmt;
    }
    return stmt;
  }

  private boolean isPropatabable(@NonNull List<Stmt> defsOfUse) {
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
