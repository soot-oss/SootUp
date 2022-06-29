package de.upb.swt.soot.core.graph;

/**
 * This class helps changing a StmtGraph instance while iterating over the original and
 * circumventing possible ConcurrentModificationExceptions
 *
 * @author Markus Schmidt
 */
public class DeferredMutableBlockStmtGraph extends MutableBlockStmtGraph {

  private MutableStmtGraph backingGraph;

  public DeferredMutableBlockStmtGraph(MutableStmtGraph graph) {
    backingGraph = graph;
  }

  public void rollback() {}

  public void commit() {}
}
