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
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.InvokeExprBox;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.constant.LongConstant;
import de.upb.swt.soot.core.jimple.common.constant.NullConstant;
import de.upb.swt.soot.core.jimple.common.expr.*;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.ref.JFieldRef;
import de.upb.swt.soot.core.jimple.common.ref.JInstanceFieldRef;
import de.upb.swt.soot.core.jimple.common.ref.JThisRef;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JIdentityStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.*;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * This interceptor eliminates assignment statements to locals whose values are not subsequently used, unless evaluating the right-hand side of the assignment may cause side-effects.
 * Complexity is linear with respect to the statements.
 *
 * @author Marcus Nachtigall
 */
public class DeadAssignmentEliminator implements BodyInterceptor {

  Map<Local, List<Stmt>> allDefs = new HashMap<>();
  Map<Local, List<Stmt>> allUses = new HashMap<>();

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {
    boolean eliminateOnlyStackLocals = false; // TODO: in old soot (Line 96): boolean eliminateOnlyStackLocals = PhaseOptions.getBoolen(options, "only-stack-variable"); what should I call instead?
    StmtGraph stmtGraph = builder.getStmtGraph();
    List<Stmt> stmts = builder.getStmts();
    Deque<Stmt> deque = new ArrayDeque<>(stmts.size());

    boolean isStatic = false; // TODO: in old soot: boolean isStatic = originalBody.getMethod.isStatic(); (Line 113)
    boolean allEssential = true;
    boolean checkInvoke = false;
    Local thisLocal = null;

    for (Iterator<Stmt> iterator = stmtGraph.nodes().iterator(); iterator.hasNext();) {
      Stmt stmt = iterator.next();
      boolean isEssential = true;

      if (stmt instanceof JNopStmt){
        // Do not remove nop if it is used for a Trap which is at the very end of the code
        boolean removeNop = iterator.hasNext();

        if (!removeNop){
          removeNop = true;
          for (Trap trap : builder.getTraps()){
            if (trap.getEndStmt() == stmt){
              removeNop = false;
              break;
            }
          }
        }

        if (removeNop) {
          iterator.remove();
          continue;
        }
      } else if (stmt instanceof JAssignStmt){
        JAssignStmt assignStmt = (JAssignStmt) stmt;
        Value lhs = assignStmt.getLeftOp();
        Value rhs = assignStmt.getRightOp();

        // Stmt is of the form a = a which is useless
        if(lhs == rhs && lhs instanceof Local){
          iterator.remove();
          continue;
        }

        if(lhs instanceof Local && (!eliminateOnlyStackLocals || ((Local) lhs).getName().startsWith("$") || lhs.getType() instanceof NullType)){
          isEssential = false;

          if(!checkInvoke){
            checkInvoke = assignStmt.containsInvokeExpr();
          }

          if(rhs instanceof JCastExpr){
            // CastExpr: can trigger ClassCastException, but null-casts never fail
            JCastExpr castExpr = (JCastExpr) rhs;
            Type type = castExpr.getType(); // TODO: I am not sure whether this is the correct method. Old soot calls getCastType() (Line 166), which does not exist here
            Value value = castExpr.getOp();
            isEssential = !(value instanceof NullConstant) && type instanceof ReferenceType; // TODO: Old soot checks type instanceof RefLikeType (Line 168). ReferenceType is something different there
          } else if (rhs instanceof InvokeExprBox || rhs instanceof JArrayRef || rhs instanceof JNewExpr || rhs instanceof JNewArrayExpr || rhs instanceof JNewMultiArrayExpr){
            // InvokeExprBox: can have side effects (like throwing a null pointer exception)
            // JArrayRef: can have side effects (like throwing a null pointer exception)
            // JNewExpr: can trigger class initialization
            // JNewArrayExpr: can throw exception
            // JNewMultiArrayExpr: can throw exception
            isEssential = true;
          } else if (rhs instanceof JFieldRef){
            // can trigger class initialization
            isEssential = true;

            if(rhs instanceof JInstanceFieldRef){
              JInstanceFieldRef instanceFieldRef = (JInstanceFieldRef) rhs;
              if(!isStatic && thisLocal == null){
                for(Stmt s : builder.getStmts()){  // TODO: for loop replaces originalBody.getThisLocal() from old soot (Line 185)
                  if (s instanceof JIdentityStmt && ((JIdentityStmt) s).getRightOp() instanceof JThisRef) {
                    thisLocal = (Local) (((JIdentityStmt) s).getLeftOp());
                    break;
                  }
                }
              }

              // Any JInstanceFieldRef may have side effects, unless the base is reading from 'this' in a non-static method
              isEssential = (isStatic || thisLocal != instanceFieldRef.getBase());
            }
          } else if (rhs instanceof JDivExpr || rhs instanceof JRemExpr){
            AbstractBinopExpr expr = (AbstractBinopExpr) rhs;
            Type type1 = expr.getOp1().getType();
            Type type2 = expr.getOp2().getType();

            // Can trigger a division by zero
            boolean type2Int = type2 instanceof PrimitiveType && ((PrimitiveType) type2).getName().equals(PrimitiveType.getInt().getName());

            isEssential = type2Int || type1 instanceof PrimitiveType && ((PrimitiveType) type1).getName().equals(PrimitiveType.getInt().getName())
                || type1 instanceof PrimitiveType && ((PrimitiveType) type1).getName().equals(PrimitiveType.getLong().getName())
                || type2 instanceof PrimitiveType && ((PrimitiveType) type2).getName().equals(PrimitiveType.getLong().getName())
                || type1 instanceof UnknownType || type2 instanceof UnknownType; // FIXME: this is kinda ugly

            if(isEssential && type2Int){
              Value value = expr.getOp2();
              if(value instanceof IntConstant){
                IntConstant intConstant = (IntConstant) value;
                isEssential = (intConstant.getValue() == 0);
              } else {
                isEssential = true; // could be 0, we don't know
              }
            }
          }
        }
      }

      if(isEssential){
        deque.addFirst(stmt);
      }

      allEssential &= isEssential;
    }

    if(checkInvoke || !allEssential){
      // Add all the statements which are used to compute values for the essential statements, recursively
      collectDefs(builder.getStmts()); // TODO: old soot calls final LocalDefs localDefs = LocalDefs.Factory.newLocalDefs(b); instead

      if(!allEssential){
        Set<Stmt> essential = new HashSet<>(stmts.size());
        while(!deque.isEmpty()){
          Stmt stmt = deque.removeFirst();
          if(essential.add(stmt)){
            for(Value value : stmt.getUses()){
              if(value instanceof Local){
                Local local = (Local) value;
                List<Stmt> defs = allDefs.get(local);
                if(defs != null){
                  deque.addAll(defs);
                }
              }
            }
          }
        }
        // Remove the dead statements
        stmts.retainAll(essential);
      }

      if(checkInvoke){
        collectUses(builder.getStmts());  // TODO: old soot calls final LocalUses localUses = LocalUses.Factory.newLocalUses(b, localDefs); instead
        //Eliminate dead assignments from invokes such as x = f(), where x is no longer used
        List<JAssignStmt> postProcess = new ArrayList<>();
        for(Stmt stmt : stmts){
          if(stmt instanceof JAssignStmt){
            JAssignStmt assignStmt = (JAssignStmt) stmt;
            if(assignStmt.containsInvokeExpr()){
              // Just find one use of local which is essential
              boolean deadAssignment = true;
              Local local = (Local) assignStmt.getRightOp();
              for(Stmt use : allUses.get(local)){
                if(builder.getStmts().contains(use)){
                  deadAssignment = false;
                  break;
                }
              }
              if(deadAssignment){
                postProcess.add(assignStmt);
              }
            }
          }
        }

        for(JAssignStmt assignStmt : postProcess){
          // Transform it into a simple invoke
          Stmt newInvoke = Jimple.newInvokeStmt(assignStmt.getInvokeExpr(), assignStmt.getPositionInfo());
          // TODO old Soot called newInvoke.addAllTagsOf(assigneStmt) - do we need anything similar?
          builder.replaceStmt(assignStmt, newInvoke);
        }
      }
    }

    builder.commitDeferredStmtGraphChanges();
  }

  private void collectDefs(List<Stmt> stmts){
    for(Stmt stmt : stmts){
      List<Value> defs = stmt.getDefs();
      for(Value value : defs){
        if(value instanceof Local){
          List<Stmt> localDefs = allDefs.get(value);
          localDefs.add(stmt);
          allDefs.put((Local) value, localDefs);
        }
      }
    }
  }

  private void collectUses(List<Stmt> stmts){
    for(Stmt stmt : stmts){
      List<Value> uses = stmt.getUses();
      for(Value value : uses){
        if(value instanceof Local){
          List<Stmt> localUses = allUses.get(value);
          localUses.add(stmt);
          allUses.put((Local) value, localUses);
        }
      }
    }
  }
}
