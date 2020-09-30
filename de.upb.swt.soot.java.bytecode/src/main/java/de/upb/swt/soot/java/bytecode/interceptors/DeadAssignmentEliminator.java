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
import de.upb.swt.soot.core.graph.ImmutableStmtGraph;
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.InvokeExprBox;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.LongConstant;
import de.upb.swt.soot.core.jimple.common.constant.NullConstant;
import de.upb.swt.soot.core.jimple.common.expr.*;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.ref.JFieldRef;
import de.upb.swt.soot.core.jimple.common.ref.JInstanceFieldRef;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractDefinitionStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
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

  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {
    Body.BodyBuilder builder = Body.builder(originalBody);
    StmtGraph originalGraph = originalBody.getStmtGraph();
    final ImmutableStmtGraph stmtGraph = originalBody.getStmtGraph();
    List<Stmt> stmts = originalBody.getStmts();

    Deque<Stmt> deque = new ArrayDeque<Stmt>(originalBody.getStmts().size());
    boolean allEssential = true;
    boolean checkInvoke = false;
    boolean isStatic = false; // TODO: boolean isStatic = originalBody.getMethod.isStatic();
    Local thisLocal = null;
    boolean eliminateOnlyStackLocals = false; // TODO: boolean eliminateOnlyStackLocals = PhaseOptions.getBoolen(options, "only-stack-variable");

    for (Iterator<Stmt> iterator = stmtGraph.nodes().iterator(); iterator.hasNext();) {
      Stmt stmt = iterator.next();
      boolean isEssential = true;

      if (stmt instanceof JNopStmt){
        // Do not remove nop if it is used for a Trap which is at the very end of the code
        boolean removeNop = iterator.hasNext();

        if (!removeNop){
          removeNop = true;
          for (Trap trap : originalBody.getTraps()){
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
            Type type = castExpr.getType(); // TODO: I am not sure whether this is the correct method. Old soot calls getCastType(), which does not exist here
            Value value = castExpr.getOp();
            isEssential = !(value instanceof NullConstant) && type instanceof ReferenceType; // TODO: Old soot checks type instanceof RefLikeType. ReferenceType is something different there
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
                thisLocal = originalBody.getThisLocal();
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
                || type1 instanceof UnknownType || type2 instanceof UnknownType; // TODO: this is kinda ugly

            if(isEssential && type2Int){
              Value value = expr.getOp2();
              if(value instanceof LongConstant){
                LongConstant longConstant = (LongConstant) value;
                isEssential = (longConstant.getValue() == 0);
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
      collectDefs(originalBody); // TODO: old soot calls final LocalDefs localDefs = LocalDefs.Factory.newLocalDefs(b); instead

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
        collectUses(originalBody);  // TODO: old soot calls final LocalUses localUses = LocalUses.Factory.newLocalUses(b, localDefs); instead
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
                if(originalBody.getStmts().contains(use)){    // TODO: this should be the update list of still available statements, right?
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
          // TODO old Soot called newInvoke.addAllTagsOf(assigneStmt) - we don't need anything similar, right?
          builder.replaceStmt(assignStmt, newInvoke);
        }
      }
    }

    return builder.build();
  }

  private void collectDefs(Body body){
    for(Stmt stmt : body.getStmts()){
      List<Value> defs = stmt.getDefs();
      for(Value value : defs){
        if(value instanceof Local){
          List<Stmt> stmts = allDefs.get((Local) value);
          stmts.add(stmt);
          allDefs.put((Local) value, stmts);
        }
      }
    }
  }

  private void collectUses(Body body){
    for(Stmt stmt : body.getStmts()){
      List<Value> uses = stmt.getUses();
      for(Value value : uses){
        if(value instanceof Local){
          List<Stmt> stmts = allUses.get((Local) value);
          stmts.add(stmt);
          allUses.put((Local) value, stmts);
        }
      }
    }
  }
}
