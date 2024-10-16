package sootup.core.graph;

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
import java.util.*;
import javax.annotation.Nonnull;
import sootup.core.jimple.basic.Trap;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ClassType;

/**
 * forwarding implementation for encapsulating a StmtGraph.
 *
 * @author Markus Schmidt
 */
public class ForwardingStmtGraph<V extends BasicBlock<V>> extends StmtGraph<V> {

  @Nonnull protected final StmtGraph<V> backingGraph;

  public ForwardingStmtGraph(@Nonnull StmtGraph<V> backingGraph) {
    this.backingGraph = backingGraph;
  }

  @Override
  public Stmt getStartingStmt() {
    return backingGraph.getStartingStmt();
  }

  @Override
  public BasicBlock<?> getStartingStmtBlock() {
    return backingGraph.getStartingStmtBlock();
  }

  @Override
  public BasicBlock<?> getBlockOf(@Nonnull Stmt stmt) {
    return backingGraph.getBlockOf(stmt);
  }

  @Nonnull
  @Override
  public Collection<Stmt> getNodes() {
    return backingGraph.getNodes();
  }

  @Nonnull
  @Override
  public Collection<? extends BasicBlock<?>> getBlocks() {
    return backingGraph.getBlocks();
  }

  @Nonnull
  @Override
  public List<? extends BasicBlock<?>> getBlocksSorted() {
    return backingGraph.getBlocksSorted();
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

  @Nonnull
  @Override
  public List<Stmt> exceptionalPredecessors(@Nonnull Stmt node) {
    return backingGraph.exceptionalPredecessors(node);
  }

  @Override
  @Nonnull
  public List<Stmt> successors(@Nonnull Stmt stmt) {
    return backingGraph.successors(stmt);
  }

  @Nonnull
  @Override
  public Map<ClassType, Stmt> exceptionalSuccessors(@Nonnull Stmt node) {
    return backingGraph.exceptionalSuccessors(node);
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
  public Iterator<Stmt> iterator() {
    return backingGraph.iterator();
  }

  @Nonnull
  @Override
  public List<Trap> buildTraps() {
    return backingGraph.buildTraps();
  }

  @Override
  public void removeExceptionalFlowFromAllBlocks(
      @Nonnull ClassType exceptionType, @Nonnull Stmt exceptionHandlerStmt) {
    backingGraph.removeExceptionalFlowFromAllBlocks(exceptionType, exceptionHandlerStmt);
  }
}
