package sootup.core.graph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2021 Raja Vallee-Rai, Zun Wang
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
import sootup.core.jimple.basic.Trap;
import sootup.core.jimple.common.stmt.Stmt;

/** @author Zun Wang */
public class BackwardsStmtGraph extends ForwardingStmtGraph {

  public BackwardsStmtGraph(@Nonnull StmtGraph<?> stmtGraph) {
    super(stmtGraph);
  }

  @Override
  public Stmt getStartingStmt() {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  public List<Stmt> getStartingStmts() {
    return backingGraph.getTails();
  }

  @Nonnull
  @Override
  public Collection<Stmt> getNodes() {
    return Collections.unmodifiableCollection(backingGraph.getNodes());
  }

  @Override
  public boolean containsNode(@Nonnull Stmt node) {
    return backingGraph.containsNode(node);
  }

  @Nonnull
  @Override
  public List<Stmt> predecessors(@Nonnull Stmt node) {
    return successors(node);
  }

  @Nonnull
  @Override
  public List<Stmt> successors(@Nonnull Stmt node) {
    return predecessors(node);
  }

  @Override
  public int inDegree(@Nonnull Stmt node) {
    return backingGraph.outDegree(node);
  }

  @Override
  public int outDegree(@Nonnull Stmt node) {
    return backingGraph.inDegree(node);
  }

  @Override
  public boolean hasEdgeConnecting(@Nonnull Stmt source, @Nonnull Stmt target) {
    return backingGraph.hasEdgeConnecting(target, source);
  }

  @Nonnull
  @Override
  public List<Trap> getTraps() {
    return backingGraph.getTraps();
  }
}
