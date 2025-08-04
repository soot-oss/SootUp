package sootup.analysis.intraprocedural;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 1999 Raja Vallee-Rai
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

import java.util.IdentityHashMap;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.Stmt;

/**
 * An abstract class providing a meta-framework for carrying out dataflow analysis. This class
 * provides common methods and fields required by the BranchedFlowAnalysis and FlowAnalysis abstract
 * classes.
 *
 * @param <F> abstraction type for the Facts
 */
public abstract class AbstractFlowAnalysis<F> {

  /** The graph being analysed. */
  protected final StmtGraph<? extends BasicBlock<?>> graph;

  /** Maps graph nodes to IN sets. */
  protected final Map<Stmt, F> stmtToBeforeFlow;

  /** Constructs a flow analysis on the given <code>StmtGraph</code>. */
  public AbstractFlowAnalysis(StmtGraph<? extends BasicBlock<?>> graph) {
    this.graph = graph;
    this.stmtToBeforeFlow = new IdentityHashMap<>(graph.getNodes().size() * 2 + 1);
  }

  /** Returns the flow object corresponding to the initial values for each graph node. */
  @NonNull
  protected abstract F newInitialFlow();

  /** Returns the initial flow value for entry/ exit graph nodes. */
  protected F entryInitialFlow() {
    return newInitialFlow();
  }

  /** Determines whether <code>entryInitialFlow()</code> is applied to trap handlers. */
  // FIXME: [ms] implement as an option
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
  protected abstract void merge(@NonNull F in1, @NonNull F in2, @NonNull F out);

  /**
   * Merges in1 and in2 into out, just before node succNode. By default, this method just calls
   * merge(A,A,A), ignoring the node.
   */
  protected void merge(@NonNull Stmt succNode, @NonNull F in1, @NonNull F in2, @NonNull F out) {
    merge(in1, in2, out);
  }

  /** Creates a copy of the <code>source</code> flow object in <code>dest</code>. */
  protected abstract void copy(@NonNull F source, @NonNull F dest);

  /**
   * Carries out the actual flow analysis. Typically called from a concrete FlowAnalysis's
   * constructor.
   */
  protected abstract void execute();

  /** Accessor function returning value of IN set for s. */
  @NonNull
  public F getFlowBefore(@NonNull Stmt s) {
    return stmtToBeforeFlow.get(s);
  }

  /** Merges in into inout, just before node succNode. */
  protected void mergeInto(@NonNull Stmt succNode, @NonNull F inout, @NonNull F in) {
    F tmp = newInitialFlow();
    merge(succNode, inout, in, tmp);
    copy(tmp, inout);
  }
}
