package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Immutable implementation for a StmtGraph. It encapsulates a StmtGraph so its not modifiable.
 *
 * @author Markus Schmidt
 */
public final class ImmutableStmtGraph extends StmtGraph {

  @Nonnull private final MutableStmtGraph backingGraph;

  public ImmutableStmtGraph(MutableStmtGraph backingGraph) {
    this.backingGraph = backingGraph;
  }

  public static ImmutableStmtGraph copyOf(@Nonnull StmtGraph stmtGraph) {

    if (stmtGraph instanceof ImmutableStmtGraph) {
      return (ImmutableStmtGraph) stmtGraph;
    }

    MutableStmtGraph graph = MutableStmtGraph.copyOf(stmtGraph);

    if (graph.getEntryPoint() == null) {
      throw new RuntimeException("Starting Stmt can not be null.");
    }

    return new ImmutableStmtGraph(graph);
  }

  @Override
  @Nonnull
  public Stmt getEntryPoint() {
    return backingGraph.getEntryPoint();
  }

  @Override
  public Set<Stmt> nodes() {
    return backingGraph.nodes();
  }

  @Override
  @Nonnull
  public List<Stmt> adjacentNodes(@Nonnull Stmt stmt) {
    return backingGraph.adjacentNodes(stmt);
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
}
