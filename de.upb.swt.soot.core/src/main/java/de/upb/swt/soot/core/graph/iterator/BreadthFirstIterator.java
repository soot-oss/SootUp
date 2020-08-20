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
 * Iterates over a given StmtGraph (which is connected, so all Stmt nodes are reached) in Breadth
 * First Order.
 *
 * @author Markus Schmidt
 */
public class BreadthFirstIterator extends StmtGraphIterator {

  @Nonnull private final Queue<Stmt> q = new ArrayDeque<>();

  BreadthFirstIterator(@Nonnull StmtGraph graph) {
    this(graph, graph.getStartingStmt());
  }

  BreadthFirstIterator(@Nonnull StmtGraph graph, @Nonnull Stmt startingStmt) {
    super(graph);
    addToContainer(startingStmt);
    alreadyInsertedNodes.add(startingStmt);
  }

  @Override
  public boolean hasNext() {
    return !q.isEmpty();
  }

  @Override
  protected void addToContainer(@Nonnull Stmt stmt) {
    q.add(stmt);
  }

  @Override
  protected Stmt removeFromContainer() {
    return q.remove();
  }
}
