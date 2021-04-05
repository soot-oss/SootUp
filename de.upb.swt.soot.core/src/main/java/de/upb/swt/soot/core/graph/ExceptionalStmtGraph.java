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
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

/** @author Zun Wang */
public final class ExceptionalStmtGraph extends StmtGraph {

  @Nonnull private final MutableExceptionalStmtGraph exceptionalStmtGraph;

  public ExceptionalStmtGraph(@Nonnull MutableExceptionalStmtGraph graph) {
    this.exceptionalStmtGraph = graph;
  }

  @Override
  public Stmt getStartingStmt() {
    return exceptionalStmtGraph.getStartingStmt();
  }

  @Nonnull
  @Override
  public Set<Stmt> nodes() {
    return exceptionalStmtGraph.nodes();
  }

  @Override
  public boolean containsNode(@Nonnull Stmt stmt) {
    return exceptionalStmtGraph.containsNode(stmt);
  }

  @Nonnull
  @Override
  public List<Stmt> predecessors(@Nonnull Stmt stmt) {
    return exceptionalStmtGraph.predecessors(stmt);
  }

  @Nonnull
  public List<Stmt> exceptionalPredecessors(@Nonnull Stmt stmt) {
    return exceptionalStmtGraph.exceptionalPredecessors(stmt);
  }

  @Nonnull
  @Override
  public List<Stmt> successors(@Nonnull Stmt stmt) {
    return exceptionalStmtGraph.successors(stmt);
  }

  @Nonnull
  public List<Stmt> exceptionalSuccessors(@Nonnull Stmt stmt) {
    return exceptionalStmtGraph.exceptionalSuccessors(stmt);
  }

  @Nonnull
  public List<Trap> getDestTraps(@Nonnull Stmt stmt) {
    return exceptionalStmtGraph.getDestTraps(stmt);
  }

  @Override
  public int inDegree(@Nonnull Stmt stmt) {
    return exceptionalStmtGraph.degree(stmt);
  }

  @Override
  public int outDegree(@Nonnull Stmt stmt) {
    return exceptionalStmtGraph.outDegree(stmt);
  }

  @Override
  public boolean hasEdgeConnecting(@Nonnull Stmt source, @Nonnull Stmt target) {
    return exceptionalStmtGraph.hasEdgeConnecting(source, target);
  }

  @Nonnull
  @Override
  public List<Trap> getTraps() {
    return exceptionalStmtGraph.getTraps();
  }
}
