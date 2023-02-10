package sootup.java.bytecode.interceptors;
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
import java.util.*;
import javax.annotation.Nonnull;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;

/**
 * A BodyInterceptor that removes all unreachable stmts from the given Body.
 *
 * @author Zun Wang
 */
public class UnreachableCodeEliminator implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View<?> view) {

    StmtGraph<?> graph = builder.getStmtGraph();

    Deque<Stmt> queue = new ArrayDeque<>();
    queue.add(graph.getStartingStmt());

    // calculate all reachable stmts
    Set<Stmt> reachableStmts = new HashSet<>();
    while (!queue.isEmpty()) {
      Stmt stmt = queue.removeFirst();
      reachableStmts.add(stmt);
      for (Stmt succ : graph.getAllSuccessors(stmt)) {
        if (!reachableStmts.contains(succ)) {
          queue.add(succ);
        }
      }
    }

    // remove unreachable stmts from StmtGraph
    Queue<Stmt> removeQ = new ArrayDeque<>();
    for (Stmt stmt : graph.getNodes()) {
      if (!reachableStmts.contains(stmt)) {
        removeQ.add(stmt);
      }
    }

    for (Stmt stmt : removeQ) {
      builder.removeStmt(stmt);
    }
  }
}
