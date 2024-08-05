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

import java.util.*;
import javax.annotation.Nonnull;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.Stmt;

/**
 * An abstract class providing a framework for carrying out dataflow analysis. Subclassing either
 * BackwardFlowAnalysis or ForwardFlowAnalysis and providing implementations for the abstract
 * methods will allow Soot to compute the corresponding flow analysis.
 */
public abstract class FlowAnalysis<A> extends AbstractFlowAnalysis<A> {

  public enum Flow {
    IN {
      @Override
      <F> F getFlow(Entry<F> e) {
        return e.inFlow;
      }
    },
    OUT {
      @Override
      <F> F getFlow(Entry<F> e) {
        return e.outFlow;
      }
    };

    abstract <F> F getFlow(Entry<F> e);
  }

  static class Entry<F> {
    final Stmt data;
    int number;

    /** This Entry is part of a real scc. */
    boolean isRealStronglyConnected;

    Entry<F>[] in;
    Entry<F>[] out;
    F inFlow;
    F outFlow;

    @SuppressWarnings("unchecked")
    Entry(Stmt u, Entry<F> pred) {
      in = new Entry[] {pred};
      data = u;
      number = Integer.MIN_VALUE;
      isRealStronglyConnected = false;
    }

    @Override
    public String toString() {
      return data == null ? "" : data.toString();
    }
  }

  static class Orderer {
    /**
     * Creates a new {@code Entry} graph based on a {@code DirectedGraph}. This includes pseudo
     * topological order, local access for predecessors and successors, a graph entry-point, a
     * {@code Numberable} interface and a real strongly connected component marker.
     *
     * @param g
     * @param direction
     * @param entryFlow
     * @return
     */
    static <F> List<Entry<F>> newUniverse(
        @Nonnull StmtGraph<? extends BasicBlock<?>> g,
        @Nonnull AnalysisDirection direction,
        @Nonnull F entryFlow) {
      final int size = g.getNodes().size();
      final int n = size;

      Deque<Entry<F>> s = new ArrayDeque<>(n);
      List<Entry<F>> universe = new ArrayList<>(n);
      Map<Stmt, Entry<F>> visited = new HashMap<>(((n + 1) * 4) / 3);

      // out of universe node
      Entry<F> superEntry = new Entry<F>(null, null);

      List<Stmt> entries;
      List<Stmt> actualEntries = direction.getEntries(g);

      if (!actualEntries.isEmpty()) {
        // normal cases: there is at least
        // one return statement for a backward analysis
        // or one entry statement for a forward analysis
        entries = actualEntries;
      } else {
        // cases without any entry statement

        if (AnalysisDirection.FORWARD == direction) {
          // case of a forward flow analysis on
          // a method without any entry point
          throw new RuntimeException("error: no entry point for method in forward analysis");
        } else {
          // case of backward analysis on
          // a method which potentially has
          // an infinite loop and no return statement
          entries = new ArrayList<>();

          // a single head is expected
          final Collection<Stmt> entrypoints = g.getEntrypoints();
          assert entrypoints.size() == 1;
          Stmt head = entrypoints.iterator().next();

          // collect all 'goto' statements to catch the 'goto' from the infinite loop
          Set<Stmt> visitedNodes = new HashSet<>();
          List<Stmt> workList = new ArrayList<>();
          workList.add(head);
          for (Stmt currentStmt; !workList.isEmpty(); ) {
            currentStmt = workList.remove(0);
            visitedNodes.add(currentStmt);

            // only add 'goto' statements
            if (currentStmt instanceof JGotoStmt) {
              entries.add(currentStmt);
            }

            for (Stmt successor : g.successors(currentStmt)) {
              if (visitedNodes.contains(successor)) {
                continue;
              }
              workList.add(successor);
            }
          }

          //
          if (entries.isEmpty()) {
            throw new RuntimeException("error: backward analysis on an empty entry set.");
          }
        }
      }

      visitEntry(visited, superEntry, entries);
      superEntry.inFlow = entryFlow;
      superEntry.outFlow = entryFlow;

      @SuppressWarnings("unchecked")
      Entry<F>[] sv = new Entry[size];
      int[] si = new int[size];
      int index = 0;

      int i = 0;
      Entry<F> v = superEntry;

      while (true) {
        if (i < v.out.length) {
          Entry<F> w = v.out[i++];

          // an unvisited child node
          if (w.number == Integer.MIN_VALUE) {
            w.number = s.size();
            s.add(w);

            visitEntry(visited, w, direction.getOut(g, w.data));

            // save old
            si[index] = i;
            sv[index] = v;
            index++;

            i = 0;
            v = w;
          }
        } else {
          if (index == 0) {
            assert universe.size() <= size;
            Collections.reverse(universe);
            return universe;
          }

          universe.add(v);
          sccPop(s, v);

          // restore old
          index--;
          v = sv[index];
          i = si[index];
        }
      }
    }

    @Nonnull
    private static <D, F> Entry<F>[] visitEntry(
        Map<Stmt, Entry<F>> visited, Entry<F> v, List<Stmt> out) {
      final int n = out.size();
      @SuppressWarnings("unchecked")
      Entry<F>[] a = new Entry[n];

      assert (out instanceof RandomAccess);

      for (int i = 0; i < n; i++) {
        a[i] = getEntryOf(visited, out.get(i), v);
      }

      return v.out = a;
    }

    @Nonnull
    private static <F> Entry<F> getEntryOf(
        @Nonnull Map<Stmt, Entry<F>> visited, @Nonnull Stmt stmt, @Nonnull Entry<F> v) {
      // either we reach a new node or a merge node, the latter one is rare
      // so put and restore should be better that a lookup

      // add and restore if required
      Entry<F> newEntry = new Entry<>(stmt, v);
      Entry<F> oldEntry = visited.putIfAbsent(stmt, newEntry);

      // no restore required
      if (oldEntry == null) {
        return newEntry;
      }

      // adding self ref (real strongly connected with itself)
      if (oldEntry == v) {
        oldEntry.isRealStronglyConnected = true;
      }

      // merge nodes are rare, so this is ok
      int l = oldEntry.in.length;
      oldEntry.in = Arrays.copyOf(oldEntry.in, l + 1);
      oldEntry.in[l] = v;
      return oldEntry;
    }

    private static <D, F> void sccPop(@Nonnull Deque<Entry<F>> s, @Nonnull Entry<F> v) {
      int min = v.number;
      for (Entry<F> e : v.out) {
        assert e.number > Integer.MIN_VALUE;
        min = Math.min(min, e.number);
      }

      // not our SCC
      if (min != v.number) {
        v.number = min;
        return;
      }

      // we only want real SCCs (size > 1)
      Entry<F> w = s.removeLast();
      w.number = Integer.MAX_VALUE;
      if (w == v) {
        return;
      }

      w.isRealStronglyConnected = true;
      for (; ; ) {
        w = s.removeLast();
        assert w.number >= v.number;
        w.isRealStronglyConnected = true;
        w.number = Integer.MAX_VALUE;
        if (w == v) {
          assert w.in.length >= 2;
          return;
        }
      }
    }
  }

  enum AnalysisDirection {
    BACKWARD {
      @Override
      @Nonnull
      List<Stmt> getEntries(StmtGraph<? extends BasicBlock<?>> g) {
        return g.getTails();
      }

      @Override
      @Nonnull
      List<Stmt> getOut(StmtGraph<? extends BasicBlock<?>> g, Stmt s) {
        return g.predecessors(s);
      }
    },
    FORWARD {
      @Override
      @Nonnull
      List<Stmt> getEntries(StmtGraph<? extends BasicBlock<?>> g) {
        return (List<Stmt>) g.getEntrypoints();
      }

      @Override
      @Nonnull
      List<Stmt> getOut(StmtGraph<? extends BasicBlock<?>> g, Stmt s) {
        return g.successors(s);
      }
    };

    @Nonnull
    abstract List<Stmt> getEntries(StmtGraph<? extends BasicBlock<?>> g);

    @Nonnull
    abstract List<Stmt> getOut(StmtGraph<? extends BasicBlock<?>> g, Stmt s);
  }

  /** Maps graph nodes to OUT sets. */
  @Nonnull protected final Map<Stmt, A> stmtToAfterFlow;

  /** Filtered: Maps graph nodes to OUT sets. */
  @Nonnull protected Map<Stmt, A> filterStmtToAfterFlow;

  /** Constructs a flow analysis on the given <code>DirectedGraph</code>. */
  public FlowAnalysis(@Nonnull StmtGraph<? extends BasicBlock<?>> graph) {
    super(graph);
    this.stmtToAfterFlow = new IdentityHashMap<>(graph.getNodes().size() * 2 + 1);
    this.filterStmtToAfterFlow = Collections.emptyMap();
  }

  /**
   * Given the merge of the <code>out</code> sets, compute the <code>in</code> set for <code>s
   * </code> (or in to out, depending on direction).
   *
   * <p>This function often causes confusion, because the same interface is used for both forward
   * and backward flow analyses. The first parameter is always the argument to the flow function
   * (i.e. it is the "in" set in a forward analysis and the "out" set in a backward analysis), and
   * the third parameter is always the result of the flow function (i.e. it is the "out" set in a
   * forward analysis and the "in" set in a backward analysis).
   *
   * @param in the input flow
   * @param d the current node
   * @param out the returned flow
   */
  protected abstract void flowThrough(@Nonnull A in, Stmt d, @Nonnull A out);

  /** Accessor function returning value of OUT set for s. */
  public A getFlowAfter(@Nonnull Stmt s) {
    A a = stmtToAfterFlow.get(s);
    return a == null ? newInitialFlow() : a;
  }

  @Nonnull
  @Override
  public A getFlowBefore(@Nonnull Stmt s) {
    A a = stmtToBeforeFlow.get(s);
    return a == null ? newInitialFlow() : a;
  }

  private void initFlow(
      @Nonnull Iterable<Entry<A>> universe, @Nonnull Map<Stmt, A> in, @Nonnull Map<Stmt, A> out) {

    // If a node has only a single in-flow, the in-flow is always equal
    // to the out-flow if its predecessor, so we use the same object.
    // this saves memory and requires less object creation and copy calls.

    // Furthermore a node can be marked as omissible, this allows us to use
    // the same "flow-set" for out-flow and in-flow. A merge node with within
    // a real scc cannot be omitted, as it could cause endless loops within
    // the fixpoint-iteration!

    for (Entry<A> n : universe) {
      boolean omit = true;
      if (n.in.length > 1) {
        n.inFlow = newInitialFlow();

        // no merge points in loops
        omit = !n.isRealStronglyConnected;
      } else {
        assert n.in.length == 1 : "missing superhead";
        n.inFlow = getFlow(n.in[0], n);
        assert n.inFlow != null : "topological order is broken";
      }

      if (omit && omissible(n.data)) {
        // We could recalculate the graph itself but thats more expensive than
        // just falling through such nodes.
        n.outFlow = n.inFlow;
      } else {
        n.outFlow = newInitialFlow();
      }

      // for legacy api (ms: already a soot comment)
      in.put(n.data, n.inFlow);
      out.put(n.data, n.outFlow);
    }
  }

  /**
   * If a flow node can be omitted return <code>true</code>, otherwise <code>false</code>. There is
   * no guarantee a node will be omitted. A omissible node does not influence the result of an
   * analysis.
   *
   * <p>If you are unsure, don't overwrite this method
   *
   * @param stmt the node to check
   * @return <code>false</code>
   */
  protected boolean omissible(@Nonnull Stmt stmt) {
    return false;
  }

  /**
   * You can specify which flow set you would like to use of node {@code from}
   *
   * @param from
   * @param mergeNode
   * @return Flow.OUT
   */
  protected Flow getFlow(@Nonnull Stmt from, @Nonnull Stmt mergeNode) {
    return Flow.OUT;
  }

  private A getFlow(@Nonnull Entry<A> o, @Nonnull Entry<A> e) {
    return (o.inFlow == o.outFlow) ? o.outFlow : getFlow(o.data, e.data).getFlow(o);
  }

  private void meetFlows(@Nonnull Entry<A> entry) {
    assert entry.in.length >= 1;

    if (entry.in.length > 1) {
      boolean copy = true;
      for (Entry<A> o : entry.in) {
        A flow = getFlow(o, entry);
        if (copy) {
          copy = false;
          copy(flow, entry.inFlow);
        } else {
          mergeInto(entry.data, entry.inFlow, flow);
        }
      }
    }
  }

  final int execute(@Nonnull Map<Stmt, A> inFlow, @Nonnull Map<Stmt, A> outFlow) {

    final boolean isForward = isForward();
    final List<Entry<A>> universe =
        Orderer.newUniverse(
            graph,
            isForward ? AnalysisDirection.FORWARD : AnalysisDirection.BACKWARD,
            entryInitialFlow());
    initFlow(universe, inFlow, outFlow);

    Queue<Entry<A>> q = UniverseSortedPriorityQueue.of(universe);

    // Perform fixed point flow analysis
    for (int numComputations = 0; ; numComputations++) {
      Entry<A> e = q.poll();
      if (e == null) {
        return numComputations;
      }

      meetFlows(e);

      // Compute beforeFlow and store it.
      // ifh.handleFlowIn(this, e.data);
      boolean hasChanged = flowThrough(e);
      // ifh.handleFlowOut(this, e.data);

      // Update queue appropriately
      if (hasChanged) {
        q.addAll(Arrays.asList(e.out));
      }
    }
  }

  private boolean flowThrough(Entry<A> d) {
    // omitted, just fall through
    if (d.inFlow == d.outFlow) {
      assert !d.isRealStronglyConnected || d.in.length == 1;
      return true;
    }

    if (d.isRealStronglyConnected) {
      // A flow node that is influenced by at least one back-reference.
      // It's essential to check if "flowThrough" changes the result.
      // This requires the calculation of "equals", which itself
      // can be really expensive - depending on the used flow-model.
      // Depending on the "merge"+"flowThrough" costs, it can be cheaper
      // to fall through. Only nodes with real back-references always
      // need to be checked for changes
      A out = newInitialFlow();
      flowThrough(d.inFlow, d.data, out);
      if (out.equals(d.outFlow)) {
        return false;
      }
      // copy back the result, as it has changed (former: copyFreshToExisting)
      copy(out, d.outFlow);
      return true;
    }

    // no back-references, just calculate "flowThrough"
    flowThrough(d.inFlow, d.data, d.outFlow);
    return true;
  }

  /*
   * Copies a *fresh* copy of in to dest. The input is not referenced somewhere else. This allows
   * subclasses for a smarter and faster copying.
   *
   * @param in
   * @param dest
   *
  protected void copyFreshToExisting(A in, A dest) {
      if (in instanceof FlowSet && dest instanceof FlowSet) {
          FlowSet<?> fin = (FlowSet<?>) in;
          FlowSet fdest = (FlowSet) dest;
          fin.copyFreshToExisting(fdest);
      } else {
          copy(in, dest);
      }
  }
  */
}
