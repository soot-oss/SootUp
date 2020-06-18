package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Immutable implementation for a StmtGraph. It encapsulates a StmtGraph so its not modifiable.
 *
 * @author Markus Schmidt
 */
public final class ImmutableStmtGraph implements StmtGraph {

  private MutableStmtGraph backingGraph;

  public ImmutableStmtGraph(MutableStmtGraph backingGraph) {
    this.backingGraph = backingGraph;
  }

  public static ImmutableStmtGraph copyOf(@Nonnull StmtGraph stmtGraph) {

    if (stmtGraph instanceof ImmutableStmtGraph) {
      return (ImmutableStmtGraph) stmtGraph;
    }

    MutableStmtGraph graph = MutableStmtGraph.copyOf(stmtGraph);

    return new ImmutableStmtGraph(graph);
  }

  @Override
  public Set<Stmt> nodes() {
    return backingGraph.nodes();
  }

  @Override
  public boolean isDirected() {
    return backingGraph.isDirected();
  }

  @Override
  public boolean allowsSelfLoops() {
    return backingGraph.allowsSelfLoops();
  }

  @Override
  @Nonnull
  public Set<Stmt> adjacentNodes(Stmt stmt) {
    return backingGraph.adjacentNodes(stmt);
  }

  @Override
  @Nonnull
  public Set<Stmt> predecessors(Stmt stmt) {
    return backingGraph.predecessors(stmt);
  }

  @Override
  @Nonnull
  public Set<Stmt> successors(Stmt stmt) {
    return backingGraph.successors(stmt);
  }

  @Override
  public int degree(Stmt stmt) {
    return backingGraph.degree(stmt);
  }

  @Override
  public int inDegree(Stmt stmt) {
    return backingGraph.inDegree(stmt);
  }

  @Override
  public int outDegree(Stmt stmt) {
    return backingGraph.outDegree(stmt);
  }

  @Override
  public boolean hasEdgeConnecting(Stmt stmt, Stmt n1) {
    return backingGraph.hasEdgeConnecting(stmt, n1);
  }
}
