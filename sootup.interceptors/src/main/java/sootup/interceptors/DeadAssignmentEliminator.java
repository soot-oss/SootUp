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

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import sootup.analysis.intraprocedural.reachingdefs.ReachingDefs;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.MethodModifier;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.*;
import sootup.core.views.View;

/**
 * This interceptor eliminates assignment statements to locals whose values are not subsequently
 * used, unless evaluating the right-hand side of the assignment may cause side-effects. Complexity
 * is linear with respect to the statements.
 *
 * @author Marcus Nachtigall
 */
public class DeadAssignmentEliminator implements BodyInterceptor {
  boolean eliminateOnlyStackLocals;

  public DeadAssignmentEliminator() {
    this(false);
  }

  public DeadAssignmentEliminator(boolean eliminateOnlyStackLocals) {
    this.eliminateOnlyStackLocals = eliminateOnlyStackLocals;
  }

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View view) {
    MutableStmtGraph stmtGraph = builder.getStmtGraph();
    Map<Stmt, List<Stmt>> reachingDefs = (new ReachingDefs(stmtGraph)).getReachingDefs();
    // refactor.. why already here - getNodes as well
    List<Stmt> stmts = builder.getStmts();
    Deque<Stmt> deque = new ArrayDeque<>(stmts.size());

    // Make a first pass through the statements, noting the statements we must absolutely keep
    boolean isStatic = MethodModifier.isStatic(builder.getModifiers());
    boolean allEssential = true;
    boolean containsInvoke = false;
    Local thisLocal = null;

    for (Iterator<Stmt> iterator = stmtGraph.getNodes().iterator(); iterator.hasNext(); ) {
      Stmt stmt = iterator.next();
      boolean isEssential = true;

      if (stmt instanceof JAssignStmt) {
        JAssignStmt assignStmt = (JAssignStmt) stmt;
        Value lhs = assignStmt.getLeftOp();
        Value rhs = assignStmt.getRightOp();

        // Stmt is of the form a = a which is useless
        if (lhs == rhs && lhs instanceof Local) {
          continue;
        }

        if (lhs instanceof Local
            && (!eliminateOnlyStackLocals
                || ((Local) lhs).getName().startsWith("$")
                || lhs.getType() instanceof NullType)) {
          isEssential = false;

          if (!containsInvoke) {
            // performance optimization: to not repeat containsInvokeExpr()
            containsInvoke = assignStmt.containsInvokeExpr();
          }

          if (rhs instanceof JCastExpr) {
            // CastExpr: can trigger ClassCastException, but null-casts never fail
            JCastExpr castExpr = (JCastExpr) rhs;
            Type type = castExpr.getType();
            Value value = castExpr.getOp();
            isEssential = !(value instanceof NullConstant) && type instanceof ReferenceType;
          } else if (rhs instanceof AbstractInvokeExpr
              || rhs instanceof JArrayRef
              || rhs instanceof JNewExpr
              || rhs instanceof JNewArrayExpr
              || rhs instanceof JNewMultiArrayExpr) {
            // InvokeExpr: can have side effects (like throwing a null pointer exception)
            // JArrayRef: can have side effects (like throwing a null pointer exception)
            // JNewExpr: can trigger class initialization
            // JNewArrayExpr: can throw exception
            // JNewMultiArrayExpr: can throw exception
            isEssential = true;
          } else if (rhs instanceof JFieldRef) {
            // can trigger class initialization
            isEssential = true;

            if (rhs instanceof JInstanceFieldRef) {
              JInstanceFieldRef instanceFieldRef = (JInstanceFieldRef) rhs;
              if (!isStatic && thisLocal == null) {
                thisLocal = Body.getThisLocal(stmtGraph);
              }

              // Any JInstanceFieldRef may have side effects, unless the base is reading from 'this'
              // in a non-static method
              isEssential = (isStatic || thisLocal != instanceFieldRef.getBase());
            }
          } else if (rhs instanceof JDivExpr || rhs instanceof JRemExpr) {
            AbstractBinopExpr expr = (AbstractBinopExpr) rhs;
            Type type1 = expr.getOp1().getType();
            Type type2 = expr.getOp2().getType();

            // Can trigger a division by zero
            boolean type2Int =
                type2 instanceof PrimitiveType && type2.equals(PrimitiveType.getInt());
            isEssential =
                type2Int
                    || type1 instanceof PrimitiveType
                        && (type1.equals(PrimitiveType.getInt())
                            || type1.equals(PrimitiveType.getLong()))
                    || type2 instanceof PrimitiveType && type2.equals(PrimitiveType.getLong())
                    || type1 instanceof UnknownType
                    || type2 instanceof UnknownType;

            if (isEssential && type2Int) {
              Value value = expr.getOp2();
              if (value instanceof IntConstant) {
                IntConstant intConstant = (IntConstant) value;
                isEssential = (intConstant.getValue() == 0);
              } else {
                // [ms] oh the irony..
                isEssential = true; // could be 0, we don't know
              }
            }
          }
        }
      }

      if (isEssential) {
        deque.addFirst(stmt);
      }

      allEssential &= isEssential;
    }

    if (!containsInvoke && allEssential) {
      return;
    }

    // Add all the statements which are used to compute values for the essential statements,
    // recursively
    Map<LValue, Collection<Stmt>> allDefs = Body.collectDefs(stmtGraph.getNodes());

    Set<Stmt> essentialStmts = new HashSet<>(stmts.size());
    while (!deque.isEmpty()) {
      Stmt stmt = deque.removeFirst();
      if (essentialStmts.add(stmt)) {
        for (Iterator<Value> iterator = stmt.getUses().iterator(); iterator.hasNext(); ) {
          Value value = iterator.next();
          if (value instanceof Local) {
            Local local = (Local) value;
            Collection<Stmt> defs = allDefs.get(local);
            List<Stmt> reachableDefs = reachingDefs.get(stmt);
            defs = defs.stream().filter(reachableDefs::contains).collect(Collectors.toList());
            if (defs != null) {
              deque.addAll(defs);
            }
          }
        }
      }
    }

    // Remove the dead statements from the stmtGraph
    for (Stmt stmt : stmts) {
      if (!essentialStmts.contains(stmt)) {
        if (stmtGraph.containsNode(stmt)) {
          stmtGraph.removeNode(stmt);
          builder.removeDefLocalsOf(stmt);
        }
      }
    }

    if (!containsInvoke) {
      return;
    }

    Map<Value, Collection<Stmt>> essentialUses = Body.collectUses(essentialStmts);
    // Eliminate dead assignments from invokes such as x = f(), where x is no longer used
    List<JAssignStmt> postProcess = new ArrayList<>();
    for (Stmt stmt : stmts) {
      if (stmt instanceof JAssignStmt) {
        JAssignStmt assignStmt = (JAssignStmt) stmt;
        if (assignStmt.containsInvokeExpr()) {
          // find at least one use of Value which is in an essential stmt
          boolean deadAssignment = true;

          for (Iterator<Value> iterator = assignStmt.getUsesAndDefs().iterator();
              iterator.hasNext(); ) {
            Value value = iterator.next();
            if (!(value instanceof LValue)) {
              continue;
            }
            final Collection<Stmt> stmtsWithValuesUse = essentialUses.get(value);
            if (stmtsWithValuesUse != null) {
              deadAssignment = false;
              break;
            }
          }
          if (deadAssignment) {
            postProcess.add(assignStmt);
          }
        }
      }
    }

    // change JAssignStmt+InvokeExpr where the lhs is not used/essential to an JInvokeStmt
    for (JAssignStmt assignStmt : postProcess) {
      // Transform it into a simple invoke
      Stmt newInvoke =
          Jimple.newInvokeStmt(assignStmt.getInvokeExpr().get(), assignStmt.getPositionInfo());
      stmtGraph.replaceNode(assignStmt, newInvoke);
      builder.removeDefLocalsOf(assignStmt);
    }
  }
}
