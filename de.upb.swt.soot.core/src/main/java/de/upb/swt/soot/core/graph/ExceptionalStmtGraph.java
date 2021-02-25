package de.upb.swt.soot.core.graph;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Zun Wang
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

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import javax.annotation.Nonnull;

/** @author Zun Wang */
public class ExceptionalStmtGraph extends MutableStmtGraph {

  @Nonnull private ArrayList<List<Stmt>> exceptionalPreds = new ArrayList<>();
  @Nonnull private ArrayList<List<Stmt>> exceptionalSuccs = new ArrayList<>();

  private Map<Stmt, List<Stmt>> beginStmtToTraps;
  private Map<Stmt, Stmt> beginStmtToEndStmt;

  /** creates an empty instance of ExceptionalStmtGraph */
  public ExceptionalStmtGraph() {
    super();
    exceptionalPreds = new ArrayList<>();
    exceptionalSuccs = new ArrayList<>();
  }

  /** creates a mutable copy(!) of originalStmtGraph with exceptional info */
  public ExceptionalStmtGraph(@Nonnull StmtGraph oriStmtGraph) {
    super(oriStmtGraph);
    setTraps(oriStmtGraph.getTraps());

    // if there're traps, then iterate original StmtGraph to set up exceptional-Preds and Succs
    if (!oriStmtGraph.getTraps().isEmpty()) {
      int size = oriStmtGraph.nodes().size();

      // initialize exceptionalPreds and exceptionalSuccs
      for (int i = 0; i < size; i++) {
        exceptionalPreds.add(Collections.emptyList());
        exceptionalSuccs.add(Collections.emptyList());
      }

      // Map: key: beginStmt  value: exceptional successors of beginStmt
      beginStmtToTraps = getBeginStmtToTraps(oriStmtGraph.getTraps());
      // Map beginStmt and endStmt pair for each trap.
      beginStmtToEndStmt = getBeginStmtToEndStmt(oriStmtGraph.getTraps());

      // This map is using for collecting predecessors for each handlerStmts
      HashMap<Stmt, List<Stmt>> handlerStmtToPreds = new HashMap<>();
      oriStmtGraph
          .getTraps()
          .forEach(trap -> handlerStmtToPreds.put(trap.getHandlerStmt(), new ArrayList<>()));

      Iterator<Stmt> it = oriStmtGraph.iterator();

      // initial trap block identifier
      Stmt trapBegin = null;
      Stmt trapEnd = null;

      Integer idx;

      while (it.hasNext()) {
        Stmt stmt = it.next();
        idx = stmtToIdx.get(stmt);

        // set exceptional successors for the stmt
        if (trapBegin != null && trapEnd != null) {
          // reach trap block boundary
          if (stmt == trapEnd) {
            trapBegin = null;
            trapEnd = null;
          } else {
            List<Stmt> handlerStmts = beginStmtToTraps.get(trapBegin);
            exceptionalSuccs.set(idx, handlerStmts);
            handlerStmts.forEach(handlerstmt -> handlerStmtToPreds.get(handlerstmt).add(stmt));
          }
          // reach a new trap block
        } else if (beginStmtToTraps.containsKey(stmt)) {
          trapBegin = stmt;
          trapEnd = beginStmtToEndStmt.get(stmt);
          List<Stmt> handlerStmts = beginStmtToTraps.get(trapBegin);
          exceptionalSuccs.set(idx, handlerStmts);
          handlerStmts.forEach(handlerstmt -> handlerStmtToPreds.get(handlerstmt).add(stmt));
        }
      }

      // set exceptional predecessors for the stmt which is a handlerStmt
      for (Stmt handlerStmt : handlerStmtToPreds.keySet()) {
        Integer index = stmtToIdx.get(handlerStmt);
        exceptionalPreds.set(index, handlerStmtToPreds.get(handlerStmt));
      }
    }
  }

  @Nonnull
  public List<Stmt> exceptionalPredecessors(@Nonnull Stmt stmt) {
    Integer idx = getNodeIdx(stmt);
    List<Stmt> stmts = exceptionalPreds.get(idx);
    if (stmts == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(stmts);
  }

  @Nonnull
  public List<Stmt> exceptionalSuccessors(@Nonnull Stmt stmt) {
    Integer idx = getNodeIdx(stmt);
    List<Stmt> stmts = exceptionalSuccs.get(idx);
    if (stmts == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(stmts);
  }

  /**
   * Using the information of traps get the exceptional successors(handlerStmt) for the stmts which
   * are in corresponding trap block. Use beginStmt of a trap to identify the trap block. A stmt is
   * a trap block either it is the beginStmt of this trap or it is behind the beginStmt before the
   * endStmt.
   *
   * @param traps a list of traps
   * @return a map with key: beginStmt of a trap, value: exceptional successors
   */
  private Map<Stmt, List<Stmt>> getBeginStmtToTraps(List<Trap> traps) {

    Map<Stmt, List<Stmt>> bStmtToTraps = new HashMap<>();

    for (Trap trap : traps) {
      List<Stmt> trapHandlerStmts = new ArrayList<>();
      trapHandlerStmts.add(trap.getHandlerStmt());

      Deque<Stmt> queue = new ArrayDeque<>(trapHandlerStmts);
      while (!queue.isEmpty()) {
        Stmt first = queue.removeFirst();
        for (Trap t : traps) {
          if (first == t.getBeginStmt()) {
            Stmt handlerStmt = t.getHandlerStmt();
            trapHandlerStmts.add(handlerStmt);
            queue.add(handlerStmt);
          }
        }
      }
      bStmtToTraps.put(trap.getBeginStmt(), trapHandlerStmts);
    }
    return bStmtToTraps;
  }

  /**
   * Build this map for identify the trap block. A trap block is from beginStmt(inclusive) to
   * endStmt(exclusive).
   *
   * @param traps a list of traps
   * @return a map with key: beginStmt of a trap, value: endStmt of a trap
   */
  private Map<Stmt, Stmt> getBeginStmtToEndStmt(List<Trap> traps) {
    Map<Stmt, Stmt> bStmtToeStmt = new HashMap<>();
    traps.forEach(trap -> bStmtToeStmt.put(trap.getBeginStmt(), trap.getEndStmt()));
    return bStmtToeStmt;
  }
}
