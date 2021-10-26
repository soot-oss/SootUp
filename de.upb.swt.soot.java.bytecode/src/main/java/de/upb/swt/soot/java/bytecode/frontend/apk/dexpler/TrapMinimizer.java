package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2018 Raja Vallée-Rai and others
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

import de.upb.swt.soot.core.graph.ExceptionalStmtGraph;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.java.core.toolkits.exceptions.TrapTransformer;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Transformer that splits traps for Dalvik whenever a statements within the trap cannot reach the trap's handler.
 * 
 * Before: trap from label1 to label2 with handler
 * 
 * label1: stmt1 ----> handler stmt2 stmt3 ----> handler label2:
 * 
 * After: trap from label1 to label2 with handler trap from label3 to label4 with handler
 * 
 * label1: stmt1 ----> handler label2: stmt2 label3: stmt3 ----> handler label4:
 *
 * @author Alexandre Bartel
 */
public class TrapMinimizer extends TrapTransformer {

  @Nonnull
  private static final TrapMinimizer INSTANCE = new TrapMinimizer();


  public static TrapMinimizer getInstance() {
    return INSTANCE;
  }

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {
    // If we have less then two traps, there's nothing to do here
    if (builder.getTraps().size() == 0) {
      return;
    }

    ExceptionalStmtGraph eug = builder.getStmtGraph();
    Set<Stmt> stmtsWithMonitor = getStmtsWithMonitor(eug);

    Map<Trap, List<Trap>> replaceTrapBy = new HashMap<Trap, List<Trap>>(builder.getTraps().size());
    boolean updateTrap = false;
    for (Trap tr : builder.getTraps()) {
      List<Trap> newTraps = new ArrayList<Trap>(); // will contain the new
      // traps
      Stmt firstTrapStmt = tr.getBeginStmt(); // points to the first unit
      // in the trap
      boolean goesToHandler = false; // true if there is an edge from the
      // unit to the handler of the
      // current trap
      updateTrap = false;
      for (Stmt u = tr.getBeginStmt(); u != tr.getEndStmt(); u = builder.getStmts().getSuccOf(u)) {
        if (goesToHandler) {
          goesToHandler = false;
        } else {
          // if the previous unit has no exceptional edge to the
          // handler,
          // update firstTrapStmt to point to the current unit
          firstTrapStmt = u;
        }

        // If this is the catch-all block and the current unit has an,
        // active monitor, we need to keep the block
        if (tr.getExceptionType().getClassName().equals("java.lang.Throwable") && stmtsWithMonitor.contains(u)) {
          goesToHandler = true;
        }

        // check if the current unit has an edge to the current trap's
        // handler
        if (!goesToHandler) {
          if (new DalvikThrowAnalysis().mightThrow(u).catchableAs(tr.getExceptionType())) {
            // We need to be careful here. The ExceptionalStmtGraph
            // will
            // always give us an edge from the predecessor of the
            // excepting
            // unit to the handler. This predecessor, however, does
            // not need
            // to be inside the new minimized catch block.
            for (ExceptionDest<Stmt> ed : eug.getExceptionDests(u)) {
              if (ed.getTrap() == tr) {
                goesToHandler = true;
                break;
              }
            }
          }
        }

        if (!goesToHandler) {
          // if the current unit does not have an edge to the current
          // trap's handler,
          // add a new trap starting at firstTrapStmt ending at the
          // unit before the
          // current unit 'u'.
          updateTrap = true;
          if (firstTrapStmt == u) {
            // updateTrap to true
            continue;
          }
          Trap t = Jimple.v().newTrap(tr.getException(), firstTrapStmt, u, tr.getHandlerStmt());
          newTraps.add(t);
        } else {
          // if the current unit has an edge to the current trap's
          // handler,
          // add a trap if the current trap has been updated before
          // and if the
          // next unit is outside the current trap.
          if (builder.getStmts().getSuccOf(u) == tr.getEndStmt() && updateTrap) {
            Trap t = Jimple.v().newTrap(tr.getException(), firstTrapStmt, tr.getEndStmt(), tr.getHandlerStmt());
            newTraps.add(t);
          }
        }
      }
      // if updateTrap is true, the current trap has to be replaced by the
      // set of newly created traps
      // (this set can be empty if the trap covers only instructions that
      // cannot throw any exceptions)
      if (updateTrap) {
        replaceTrapBy.put(tr, newTraps);
      }
    }

    // replace traps where necessary
    for (Trap k : replaceTrapBy.keySet()) {
      builder.getTraps().insertAfter(replaceTrapBy.get(k), k); // we must keep
      // the order
      builder.getTraps().remove(k);
    }

  }

}
