package de.upb.swt.soot.java.bytecode.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 John Jorgensen, Zun Wang
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
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.JTrap;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.*;
import javax.annotation.Nonnull;

/* @author Zun Wang **/
public class TrapTightener implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {
    StmtGraph graph = builder.getStmtGraph();
    ExceptionalStmtGraph exceptionalGraph = new ExceptionalStmtGraph(graph);
    List<Stmt> stmtsInPrintOrder = builder.getStmts();

    // TODO: not finished
    Set<Stmt> monitoredStmts = monitoredStmts();
    List<Trap> traps = builder.getTraps();
    List<Trap> newTraps = new ArrayList<>();
    for (Trap trap : traps) {
      boolean isCatchAll = trap.getExceptionType().getClassName().equals("java.lang.Throwable");

      // determine the initial trap-scope
      Stmt trapBegin = trap.getBeginStmt();
      Stmt firstUnstrappedStmt = trap.getEndStmt();
      int idx = stmtsInPrintOrder.indexOf(firstUnstrappedStmt);
      Stmt trapEnd = stmtsInPrintOrder.get(idx - 1);
      // initialize a new trap-scope
      Stmt newTrapBegin = null;
      Stmt newTrapEnd = null;
      // set begin of new trap-scope
      for (int i = stmtsInPrintOrder.indexOf(trapBegin); i < stmtsInPrintOrder.size(); i++) {
        Stmt s = stmtsInPrintOrder.get(i);
        if (mightThrow(exceptionalGraph, s, trap)) {
          newTrapBegin = s;
          break;
        }
        // If trap is a catch-all block and the current stmt has an active monitor, we need to keep
        // the block
        if (isCatchAll && monitoredStmts.contains(s)) {
          if (newTrapBegin == null) {
            newTrapBegin = s;
          }
          break;
        }
      }
      // set end of new trap-scope
      // if new trap begin is null, then trap should be empty trap, so don't need to set trap end
      if (newTrapBegin != null) {
        for (int i = stmtsInPrintOrder.indexOf(trapEnd); i >= 0; i--) {
          Stmt s = stmtsInPrintOrder.get(i);
          if (mightThrow(exceptionalGraph, s, trap)) {
            newTrapEnd = s;
            break;
          }
          // If trap is a catch-all block and the current stmt has an active monitor, we need to
          // keep the block
          if (isCatchAll && monitoredStmts.contains(s)) {
            newTrapEnd = s;
            break;
          }
        }
      }
      Trap newTrap = null;
      if (newTrapBegin != null && (newTrapBegin != trapBegin || newTrapEnd != trapEnd)) {
        if (newTrapEnd != trapEnd) {
          int id = stmtsInPrintOrder.indexOf(newTrapEnd);
          firstUnstrappedStmt = stmtsInPrintOrder.get(id + 1);
        }
        newTrap =
            new JTrap(
                trap.getExceptionType(), newTrapBegin, firstUnstrappedStmt, trap.getHandlerStmt());
      }
      if (newTrap != null) {
        newTraps.add(newTrap);
      } else if (newTrapBegin != null) {
        newTraps.add(trap);
      }
    }
    builder.setTraps(newTraps);
  }

  private Set<Stmt> monitoredStmts() {
    HashMap<Stmt, List<Stmt>> monitoredStmtsMap = new HashMap<>();
    return monitoredStmtsMap.keySet();
  }

  /**
   * Check whether trap-destinations of the given stmt contain the given trap.
   *
   * @param graph is a exceptional StmtGraph
   * @param stmt is a stmt in the given graph
   * @param trap is a given trap
   * @return If trap-destinations of the given stmt contain the given trap, return true, otherwise
   *     return false
   */
  private boolean mightThrow(ExceptionalStmtGraph graph, Stmt stmt, Trap trap) {
    for (Trap dest : graph.getDestTrap(stmt)) {
      if (dest == trap) {
        return true;
      }
    }
    return false;
  }
}
