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
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractBinopExpr;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;

public class Aggregator implements BodyInterceptor {

  // TODO: [ms] the onlyStackVars flag kind of enable/disables ***everything*** that does
  // something in this Interceptor.. check with old soot again (see usage in big if)
  boolean onlyStackVars;

  public Aggregator() {
    this(false);
  }

  public Aggregator(boolean onlyStackVars) {
    this.onlyStackVars = onlyStackVars;
  }

  /**
   * Traverse the statements in the given body, looking for aggregation possibilities; that is,
   * given a def d and a use u, d has no other uses, u has no other defs, collapse d and u.
   *
   * <p>option: only-stack-locals; if this is true, only aggregate variables starting with $
   */
  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View<?> view) {

    StmtGraph<?> graph = builder.getStmtGraph();
    List<Stmt> stmts = builder.getStmts();

    for (Stmt stmt : Lists.newArrayList(stmts)) {
      if (stmt instanceof JAssignStmt) {
        final JAssignStmt<?, ?> assignStmt = (JAssignStmt<?, ?>) stmt;
        Value lhs = assignStmt.getLeftOp();
        if (lhs instanceof Local) {
          Local lhsLocal = (Local) lhs;
          if (onlyStackVars && !lhsLocal.getName().startsWith("$stack")) {
            continue;
          }
          for (Value val : assignStmt.getUses()) {
            if (val instanceof Local) {
              List<AbstractDefinitionStmt<Local, Value>> defs = ((Local) val).getDefsOfLocal(stmts);
              if (defs.size() == 1) {
                Stmt relevantDef = defs.get(0);
                List<Stmt> path = graph.getExtendedBasicBlockPathBetween(relevantDef, stmt);

                boolean cantAggr = false;
                boolean propagatingInvokeExpr = false;
                boolean propagatingFieldRef = false;
                boolean propagatingArrayRef = false;
                List<JFieldRef> fieldRefList = new ArrayList<>();

                List<Value> localsUsed = new ArrayList<>();
                for (Stmt pathStmt : path) {
                  List<Value> allDefs = pathStmt.getDefs();
                  for (Value def : allDefs) {
                    if (def instanceof Local) {
                      localsUsed.add(def);
                    } else if (def instanceof AbstractInstanceInvokeExpr) {
                      propagatingInvokeExpr = true;
                    } else if (def instanceof JArrayRef) {
                      propagatingArrayRef = true;
                    } else if (def instanceof JFieldRef) {
                      propagatingFieldRef = true;
                      fieldRefList.add((JFieldRef) def);
                    }
                  }
                }
                for (Stmt pathStmt : path) {
                  if (pathStmt != stmt && pathStmt != relevantDef) {
                    for (Value stmtDef : pathStmt.getDefs()) {
                      if (localsUsed.contains(stmtDef)) {
                        cantAggr = true;
                        break;
                      }
                      if (propagatingInvokeExpr || propagatingFieldRef || propagatingArrayRef) {
                        if (stmtDef instanceof JFieldRef) {
                          if (propagatingInvokeExpr) {
                            cantAggr = true;
                            break;
                          } else if (propagatingFieldRef) {
                            // Can't aggregate a field access if passing a definition of a field
                            // with the same name, because they might be aliased
                            for (JFieldRef fieldRef : fieldRefList) {
                              if (fieldRef.equals((JFieldRef) stmtDef)) {
                                cantAggr = true;
                                break;
                              }
                            }
                          }
                        } else if (stmtDef instanceof JArrayRef) {
                          if (propagatingInvokeExpr || propagatingArrayRef) {
                            // Cannot aggregate an invoke expr past an array write and cannot
                            // aggregate an array read past a write
                            cantAggr = true;
                            break;
                          }
                        }
                      }
                    }
                  }
                  // Check for intervening side effects due to method calls
                  if (propagatingInvokeExpr || propagatingFieldRef || propagatingArrayRef) {
                    for (final Value value : stmt.getUses()) {
                      if (pathStmt == stmt && value == lhs) {
                        break;
                      }
                      if (value instanceof AbstractInstanceInvokeExpr
                          || (propagatingInvokeExpr
                              && (value instanceof JFieldRef || value instanceof JArrayRef))) {
                        cantAggr = true;
                        break;
                      }
                    }
                  }
                }

                if (cantAggr) {
                  continue;
                }

                Value aggregatee = ((JAssignStmt<?, ?>) relevantDef).getRightOp();
                JAssignStmt<?, ?> newStmt = null;
                if (assignStmt.getRightOp() instanceof AbstractBinopExpr) {
                  AbstractBinopExpr rightOp = (AbstractBinopExpr) assignStmt.getRightOp();
                  if (rightOp.getOp1() == val) {
                    AbstractBinopExpr newBinopExpr = rightOp.withOp1((Immediate) aggregatee);
                    newStmt =
                        new JAssignStmt<>(
                            assignStmt.getLeftOp(), newBinopExpr, assignStmt.getPositionInfo());
                  } else if (rightOp.getOp2() == val) {
                    AbstractBinopExpr newBinopExpr = rightOp.withOp2((Immediate) aggregatee);
                    newStmt =
                        new JAssignStmt<>(
                            assignStmt.getLeftOp(), newBinopExpr, assignStmt.getPositionInfo());
                  }
                } else {
                  newStmt = assignStmt.withRValue(aggregatee);
                }
                if (newStmt != null) {
                  builder.replaceStmt(stmt, newStmt);
                  if (graph.getStartingStmt() == relevantDef) {
                    Stmt newStartingStmt = builder.getStmtGraph().successors(relevantDef).get(0);
                    builder.setStartingStmt(newStartingStmt);
                  }
                  builder.removeStmt(relevantDef);
                }
              }
            }
          }
        }
      }
    }
  }
}
