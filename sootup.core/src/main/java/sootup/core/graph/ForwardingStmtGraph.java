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
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ClassType;

/**
 * forwarding implementation for encapsulating a StmtGraph.
 *
 * @author Markus Schmidt
 */
public class ForwardingStmtGraph<V extends BasicBlock<V>> extends StmtGraph<V> {

  @NonNull protected final StmtGraph<V> backingGraph;

  public ForwardingStmtGraph(@NonNull StmtGraph<V> backingGraph) {
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
  public List<BasicBlock<?>> getTailStmtBlocks() {
    return backingGraph.getTailStmtBlocks();
  }

  @Override
  public BasicBlock<?> getBlockOf(@NonNull Stmt stmt) {
    return backingGraph.getBlockOf(stmt);
  }

  @NonNull
  @Override
  public Collection<Stmt> getNodes() {
    return backingGraph.getNodes();
  }

  @NonNull
  @Override
  public Collection<? extends BasicBlock<?>> getBlocks() {
    return backingGraph.getBlocks();
  }

  @NonNull
  @Override
  public List<? extends BasicBlock<?>> getBlocksSorted() {
    return backingGraph.getBlocksSorted();
  }

  @Override
  public boolean containsNode(@NonNull Stmt node) {
    return backingGraph.containsNode(node);
  }

  @Override
  @NonNull
  public List<Stmt> predecessors(@NonNull Stmt stmt) {
    return backingGraph.predecessors(stmt);
  }

  @NonNull
  @Override
  public List<Stmt> exceptionalPredecessors(@NonNull Stmt node) {
    return backingGraph.exceptionalPredecessors(node);
  }

  @Override
  @NonNull
  public List<Stmt> successors(@NonNull Stmt stmt) {
    return backingGraph.successors(stmt);
  }

  @NonNull
  @Override
  public Map<ClassType, Stmt> exceptionalSuccessors(@NonNull Stmt node) {
    return backingGraph.exceptionalSuccessors(node);
  }

  @Override
  public int degree(@NonNull Stmt stmt) {
    return backingGraph.degree(stmt);
  }

  @Override
  public int inDegree(@NonNull Stmt stmt) {
    return backingGraph.inDegree(stmt);
  }

  @Override
  public int outDegree(@NonNull Stmt stmt) {
    return backingGraph.outDegree(stmt);
  }

  @Override
  public boolean hasEdgeConnecting(@NonNull Stmt from, @NonNull Stmt to) {
    return backingGraph.hasEdgeConnecting(from, to);
  }

  @NonNull
  @Override
  public Iterator<Stmt> iterator() {
    return backingGraph.iterator();
  }

  @Override
  public void removeExceptionalFlowFromAllBlocks(
      @NonNull ClassType exceptionType, @NonNull Stmt exceptionHandlerStmt) {
    backingGraph.removeExceptionalFlowFromAllBlocks(exceptionType, exceptionHandlerStmt);
  }
}
