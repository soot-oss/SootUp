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
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.expr.AbstractBinopExpr;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.ref.JFieldRef;
import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.BodyUtils;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class Aggregator implements BodyInterceptor {

  /**
   * Traverse the statements in the given body, looking for aggregation possibilities; that is,
   * given a def d and a use u, d has no other uses, u has no other defs, collapse d and u.
   *
   * <p>option: only-stack-locals; if this is true, only aggregate variables starting with $
   */
  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {

    StmtGraph graph = builder.getStmtGraph();
    List<Stmt> stmts = builder.getStmts();
    // TODO[MN]: config parameter
    boolean onlyStackVars = false;

    builder.enableDeferredStmtGraphChanges();
    for (Stmt stmt : stmts) {
      if (stmt instanceof JAssignStmt) {
        JAssignStmt assignStmt = (JAssignStmt) stmt;
        Value lhs = assignStmt.getLeftOp();
        if (lhs instanceof Local) {
          Local lhsLocal = (Local) lhs;
          if (!(onlyStackVars && !lhsLocal.getName().startsWith("$"))) {
            for (Value val : assignStmt.getUses()) {
              if (val instanceof Local) {
                List<Stmt> defs = BodyUtils.getDefsOfLocal((Local) val, stmts);
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
                                if (isSameField(((JFieldRef) stmtDef), fieldRef)) {
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

                  Value aggregatee = ((JAssignStmt) relevantDef).getRightOp();
                  Stmt newStmt = null;
                  if (assignStmt.getRightOp() instanceof AbstractBinopExpr) {
                    AbstractBinopExpr rightOp = (AbstractBinopExpr) assignStmt.getRightOp();
                    if (rightOp.getOp1() == val) {
                      AbstractBinopExpr newBinopExpr = rightOp.withOp1(aggregatee);
                      newStmt =
                          new JAssignStmt(
                              assignStmt.getLeftOp(), newBinopExpr, assignStmt.getPositionInfo());
                    } else if (rightOp.getOp2() == val) {
                      AbstractBinopExpr newBinopExpr = rightOp.withOp2(aggregatee);
                      newStmt =
                          new JAssignStmt(
                              assignStmt.getLeftOp(), newBinopExpr, assignStmt.getPositionInfo());
                    }
                  } else {
                    newStmt = ((JAssignStmt) stmt).withRValue(aggregatee);
                  }
                  if (newStmt != null) {
                    builder.replaceStmt(stmt, newStmt);
                    JNopStmt nopStmt = new JNopStmt(stmt.getPositionInfo());
                    builder.replaceStmt(relevantDef, nopStmt);
                  }
                }
              }
            }
          }
        }
      }
    }

    builder.commitDeferredStmtGraphChanges();
  }

  private static boolean isSameField(JFieldRef ref1, JFieldRef ref2) {
    if (ref1 == ref2) {
      return true;
    }
    return ref1.getFieldSignature().equals(ref2.getFieldSignature());
  }
}
