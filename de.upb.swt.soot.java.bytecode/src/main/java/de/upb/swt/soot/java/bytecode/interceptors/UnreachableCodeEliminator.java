package de.upb.swt.soot.java.bytecode.interceptors;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Christian Brüggemann, Zun Wang
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
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.*;
import javax.annotation.Nonnull;

/**
 * A BodyInterceptor that removes all unreachable stmts from the given Body.
 *
 * @author Zun Wang
 */
public class UnreachableCodeEliminator implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {

    ExceptionalStmtGraph graph = builder.getStmtGraph();

    // collect all reachable stmts
    Set<Stmt> reachableStmts = new HashSet<>();
    Deque<Stmt> queue = new ArrayDeque<>();
    queue.add(graph.getStartingStmt());

    while (!queue.isEmpty()) {
      Stmt stmt = queue.removeFirst();
      reachableStmts.add(stmt);
      for (Stmt succ : graph.successors(stmt)) {
        if (!reachableStmts.contains(succ)) {
          queue.addLast(succ);
        }
      }
      for (Stmt esucc : graph.exceptionalSuccessors(stmt)) {
        if (!reachableStmts.contains(esucc)) {
          queue.addLast(esucc);
        }
      }
    }

    // remove unreachable stmts from StmtGraph
    builder.enableDeferredStmtGraphChanges();
    for (Stmt stmt : graph.nodes()) {
      if (!reachableStmts.contains(stmt)) {
        builder.removeStmt(stmt);
      }
    }
    builder.disableAndCommitDeferredStmtGraphChanges();

    // cleanup invalid traps
    List<Trap> traps = new ArrayList<>(builder.getTraps());
    for (Trap trap : traps) {
      if (trap.getBeginStmt() == trap.getEndStmt()
          || !reachableStmts.contains(trap.getHandlerStmt())) {
        builder.removeTrap(trap);
      }
    }
  }
}
