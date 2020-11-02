package de.upb.swt.soot.core.graph.iterator;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Markus Schmidt
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
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import javax.annotation.Nonnull;

/**
 * The StmtGraphIterator is the base class for iterating over a StmtGraph.
 *
 * @author Markus Schmidt
 */
public abstract class StmtGraphIterator implements Iterator<Stmt> {

  @Nonnull private final StmtGraph graph;
  @Nonnull protected final Set<Stmt> alreadyInsertedNodes;

  protected StmtGraphIterator(@Nonnull StmtGraph graph) {
    this.graph = graph;
    alreadyInsertedNodes = new HashSet<>(graph.nodes().size(), 1);
  }

  /** creates a Breadth First Search Iterator */
  public static BreadthFirstIterator bfs(@Nonnull StmtGraph stmtGraph) {
    return new BreadthFirstIterator(stmtGraph);
  }

  /** creates a Depth First Search Iterator */
  public static DepthFirstIterator dfs(@Nonnull StmtGraph stmtGraph) {
    return new DepthFirstIterator(stmtGraph);
  }

  @Nonnull
  protected StmtGraph getGraph() {
    return graph;
  }

  /** inserts stmt into a container to remember that it needs to be iterated */
  protected abstract void addToContainer(@Nonnull Stmt stmt);

  /** removes stmt from the remembering container */
  protected abstract Stmt removeFromContainer();

  @Override
  public Stmt next() {

    Stmt stmt = removeFromContainer();
    final List<Stmt> successors = getGraph().successors(stmt);
    for (Stmt succ : successors) {
      if (!alreadyInsertedNodes.contains(succ)) {
        addToContainer(succ);
        alreadyInsertedNodes.add(succ);
      }
    }
    return stmt;
  }
}
