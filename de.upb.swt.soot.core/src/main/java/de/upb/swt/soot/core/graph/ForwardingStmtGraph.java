package de.upb.swt.soot.core.graph;
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
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * forwarding implementation for encapsulating a StmtGraph.
 *
 * @author Markus Schmidt
 */
public final class ForwardingStmtGraph extends StmtGraph {

  @Nonnull private final StmtGraph backingGraph;

  public ForwardingStmtGraph(@Nonnull StmtGraph backingGraph) {
    this.backingGraph = backingGraph;
  }

  @Override
  public Stmt getStartingStmt() {
    return backingGraph.getStartingStmt();
  }

  @Nonnull
  @Override
  public Set<Stmt> nodes() {
    return backingGraph.nodes();
  }

  @Override
  public boolean containsNode(@Nonnull Stmt node) {
    return backingGraph.containsNode(node);
  }

  @Override
  @Nonnull
  public List<Stmt> predecessors(@Nonnull Stmt stmt) {
    return backingGraph.predecessors(stmt);
  }

  @Override
  @Nonnull
  public List<Stmt> successors(@Nonnull Stmt stmt) {
    return backingGraph.successors(stmt);
  }

  @Override
  public int degree(@Nonnull Stmt stmt) {
    return backingGraph.degree(stmt);
  }

  @Override
  public int inDegree(@Nonnull Stmt stmt) {
    return backingGraph.inDegree(stmt);
  }

  @Override
  public int outDegree(@Nonnull Stmt stmt) {
    return backingGraph.outDegree(stmt);
  }

  @Override
  public boolean hasEdgeConnecting(@Nonnull Stmt from, @Nonnull Stmt to) {
    return backingGraph.hasEdgeConnecting(from, to);
  }

  @Nonnull
  @Override
  public List<Trap> getTraps() {
    return backingGraph.getTraps();
  }
}
