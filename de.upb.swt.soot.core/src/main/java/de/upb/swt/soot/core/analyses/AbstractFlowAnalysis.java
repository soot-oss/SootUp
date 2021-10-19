package de.upb.swt.soot.core.analyses;

import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * An abstract class providing a metaframework for carrying out dataflow analysis. This class
 * provides common methods and fields required by the BranchedFlowAnalysis and FlowAnalysis abstract
 * classes.
 *
 * @param <A> abstraction type
 */
public abstract class AbstractFlowAnalysis<A> {
  /** Maps graph nodes to IN sets. */
  protected Map<Stmt, A> unitToBeforeFlow;

  /** Filtered: Maps graph nodes to IN sets. */
  protected Map<Stmt, A> filterUnitToBeforeFlow = Collections.emptyMap();

  /** The graph being analysed. */
  protected StmtGraph graph;

  /** Constructs a flow analysis on the given <code>DirectedGraph</code>. */
  public AbstractFlowAnalysis(StmtGraph graph) {
    unitToBeforeFlow = new IdentityHashMap<Stmt, A>(graph.nodes().size() * 2 + 1);
    this.graph = graph;
  }

  /** Returns the flow object corresponding to the initial values for each graph node. */
  protected abstract A newInitialFlow();

  /**
   * Returns the initial flow value for entry/exit graph nodes.
   *
   * <p>This is equal to {@link #newInitialFlow()}
   */
  protected A entryInitialFlow() {
    return newInitialFlow();
  }

  /** Determines whether <code>entryInitialFlow()</code> is applied to trap handlers. */
  protected boolean treatTrapHandlersAsEntries() {
    return false;
  }

  /** Returns true if this analysis is forwards. */
  protected abstract boolean isForward();

  /**
   * Compute the merge of the <code>in1</code> and <code>in2</code> sets, putting the result into
   * <code>out</code>. The behavior of this function depends on the implementation ( it may be
   * necessary to check whether <code>in1</code> and <code>in2</code> are equal or aliased ). Used
   * by the doAnalysis method.
   */
  protected abstract void merge(A in1, A in2, A out);

  /**
   * Merges in1 and in2 into out, just before node succNode. By default, this method just calls
   * merge(A,A,A), ignoring the node.
   */
  protected void merge(Stmt succNode, A in1, A in2, A out) {
    merge(in1, in2, out);
  }

  /** Creates a copy of the <code>source</code> flow object in <code>dest</code>. */
  protected abstract void copy(A source, A dest);

  /**
   * Carries out the actual flow analysis. Typically called from a concrete FlowAnalysis's
   * constructor.
   */
  protected abstract void doAnalysis();

  /** Accessor function returning value of IN set for s. */
  public A getFlowBefore(Stmt s) {
    return unitToBeforeFlow.get(s);
  }

  /** Merges in into inout, just before node succNode. */
  protected void mergeInto(Stmt succNode, A inout, A in) {
    A tmp = newInitialFlow();
    merge(succNode, inout, in, tmp);
    copy(tmp, inout);
  }
}
