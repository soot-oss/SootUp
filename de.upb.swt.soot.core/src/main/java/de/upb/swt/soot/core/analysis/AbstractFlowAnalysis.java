package de.upb.swt.soot.core.analysis;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2021 Raja Vallee-Rai, Kadiray Karakaya and others
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
  /** Maps stmts to IN sets. */
  protected Map<Stmt, A> stmtToBeforeFlow;

  /** Filtered: Maps stmts to IN sets. */
  protected Map<Stmt, A> filterStmtToBeforeFlow = Collections.emptyMap();

  /** The graph being analysed. */
  protected StmtGraph graph;

  /** Constructs a flow analysis on the given <code>DirectedGraph</code>. */
  protected AbstractFlowAnalysis(StmtGraph graph) {
    stmtToBeforeFlow = new IdentityHashMap<>(graph.nodes().size() * 2 + 1);
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
    return stmtToBeforeFlow.get(s);
  }

  /** Merges in into inout, just before node succNode. */
  protected void mergeInto(Stmt succNode, A inout, A in) {
    A tmp = newInitialFlow();
    merge(succNode, inout, in, tmp);
    copy(tmp, inout);
  }
}
