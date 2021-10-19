package de.upb.swt.soot.core.analyses;

import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.common.stmt.JGotoStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.RandomAccess;
import java.util.Set;

/**
 * An abstract class providing a framework for carrying out dataflow analysis. Subclassing either BackwardFlowAnalysis or
 * ForwardFlowAnalysis and providing implementations for the abstract methods will allow Soot to compute the corresponding
 * flow analysis.
 */
public abstract class FlowAnalysis<A> extends AbstractFlowAnalysis<A> {
  public enum Flow {
    IN {
      @Override
      <F> F getFlow(Entry<?, F> e) {
        return e.inFlow;
      }
    },
    OUT {
      @Override
      <F> F getFlow(Entry<?, F> e) {
        return e.outFlow;
      }
    };

    abstract <F> F getFlow(Entry<?, F> e);
  }

  static class Entry<D, F> {
    final D data;
    int number;

    /**
     * This Entry is part of a real scc.
     */
    boolean isRealStronglyConnected;

    Entry<D, F>[] in;
    Entry<D, F>[] out;
    F inFlow;
    F outFlow;

    @SuppressWarnings("unchecked")
    Entry(D u, Entry<D, F> pred) {
      in = new Entry[] { pred };
      data = u;
      isRealStronglyConnected = false;
    }

    @Override
    public String toString() {
      return data == null ? "" : data.toString();
    }

    public void setNumber(int n) {
      number = n;
    }

    public int getNumber() {
      return number;
    }
  }

  static enum Orderer {
    INSTANCE;

    /**
     * Creates a new {@code Entry} graph based on a {@code DirectedGraph}. This includes pseudo topological order, local
     * access for predecessors and successors, a graph entry-point, a {@code Numberable} interface and a real strongly
     * connected component marker.
     *
     * @param g
     * @param gv
     * @param entryFlow
     * @return
     */
    <F> List<Entry<Stmt, F>> newUniverse(StmtGraph g, GraphView gv, F entryFlow, boolean isForward) {
      final int n = g.nodes().size();

      Deque<Entry<Stmt, F>> s = new ArrayDeque<Entry<Stmt, F>>(n);
      List<Entry<Stmt, F>> universe = new ArrayList<Entry<Stmt, F>>(n);
      Map<Stmt, Entry<Stmt, F>> visited = new HashMap<Stmt, Entry<Stmt, F>>(((n + 1) * 4) / 3);

      // out of universe node
      Entry<Stmt, F> superEntry = new Entry<Stmt, F>(null, null);

      Collection<Stmt> entries = null;
      Collection<Stmt> actualEntries = gv.getEntries(g);

      if (!actualEntries.isEmpty()) {
        // normal cases: there is at least
        // one return statement for a backward analysis
        // or one entry statement for a forward analysis
        entries = actualEntries;
      } else {
        // cases without any entry statement

        if (isForward) {
          // case of a forward flow analysis on
          // a method without any entry point
          throw new RuntimeException("error: no entry point for method in forward analysis");
        } else {
          // case of backward analysis on
          // a method which potentially has
          // an infinite loop and no return statement
          entries = new ArrayList<Stmt>();

          // a single head is expected
          assert g.getEntrypoints().size() == 1;
          Stmt head = g.getEntrypoints().stream().findFirst().get();

          Set<Stmt> visitedNodes = new HashSet<Stmt>();
          List<Stmt> workList = new ArrayList<Stmt>();
          Stmt current = null;

          // collect all 'goto' statements to catch the 'goto'
          // from the infinite loop
          workList.add(head);
          while (!workList.isEmpty()) {
            current = workList.remove(0);
            visitedNodes.add(current);

            // only add 'goto' statements
            if (current instanceof JGotoStmt) {
              entries.add(current);
            }

            for (Stmt next : g.successors(current)) {
              if (visitedNodes.contains(next)) {
                continue;
              }
              workList.add(next);
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
      Entry<Stmt, F>[] sv = new Entry[g.nodes().size()];
      int[] si = new int[g.nodes().size()];
      int index = 0;

      int i = 0;
      Entry<Stmt, F> v = superEntry;

      for (;;) {
        if (i < v.out.length) {
          Entry<Stmt, F> w = v.out[i++];

          // an unvisited child node
          if (w.number == Integer.MIN_VALUE) {
            w.number = s.size();
            s.add(w);

            visitEntry(visited, w, gv.getOut(g, w.data));

            // save old
            si[index] = i;
            sv[index] = v;
            index++;

            i = 0;
            v = w;
          }
        } else {
          if (index == 0) {
            assert universe.size() <= g.nodes().size();
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

    @SuppressWarnings("unchecked")
    private <D, F> Entry<D, F>[] visitEntry(Map<D, Entry<D, F>> visited, Entry<D, F> v, List<D> out) {
      int n = out.size();
      Entry<D, F>[] a = new Entry[n];

      assert (out instanceof RandomAccess);

      for (int i = 0; i < n; i++) {
        a[i] = getEntryOf(visited, out.get(i), v);
      }

      return v.out = a;
    }

    private <D, F> Entry<D, F> getEntryOf(Map<D, Entry<D, F>> visited, D d, Entry<D, F> v) {
      // either we reach a new node or a merge node, the latter one is rare
      // so put and restore should be better that a lookup
      // putIfAbsent would be the ideal strategy

      // add and restore if required
      Entry<D, F> newEntry = new Entry<D, F>(d, v);
      Entry<D, F> oldEntry = visited.put(d, newEntry);

      // no restore required
      if (oldEntry == null) {
        return newEntry;
      }

      // false prediction, restore the entry
      visited.put(d, oldEntry);

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

    private <D, F> void sccPop(Deque<Entry<D, F>> s, Entry<D, F> v) {
      int min = v.number;
      for (Entry<D, F> e : v.out) {
        assert e.number > Integer.MIN_VALUE;
        min = Math.min(min, e.number);
      }

      // not our SCC
      if (min != v.number) {
        v.number = min;
        return;
      }

      // we only want real SCCs (size > 1)
      Entry<D, F> w = s.removeLast();
      w.number = Integer.MAX_VALUE;
      if (w == v) {
        return;
      }

      w.isRealStronglyConnected = true;
      for (;;) {
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

  enum InteractionFlowHandler {
    NONE, FORWARD {
      @Override
      public <A> void handleFlowIn(FlowAnalysis<A> a, Stmt s) {
        beforeEvent(stop(s), a, s);
      }

      @Override
      public <A> void handleFlowOut(FlowAnalysis<A> a, Stmt s) {
        afterEvent(InteractionHandler.v(), a, s);
      }
    },
    BACKWARD {
      @Override
      public <A> void handleFlowIn(FlowAnalysis<A> a, Stmt s) {
        afterEvent(stop(s), a, s);
      }

      @Override
      public <A> void handleFlowOut(FlowAnalysis<A> a, Stmt s) {
        beforeEvent(InteractionHandler.v(), a, s);
      }
    };

    <A> void beforeEvent(InteractionHandler i, FlowAnalysis<A> a, Stmt s) {
      A savedFlow = a.filterUnitToBeforeFlow.get(s);
      if (savedFlow == null) {
        savedFlow = a.newInitialFlow();
      }
      a.copy(a.unitToBeforeFlow.get(s), savedFlow);
      i.handleBeforeAnalysisEvent(new FlowInfo<A, Stmt>(savedFlow, s, true));
    }

    <A> void afterEvent(InteractionHandler i, FlowAnalysis<A> a, Stmt s) {
      A savedFlow = a.filterUnitToAfterFlow.get(s);
      if (savedFlow == null) {
        savedFlow = a.newInitialFlow();
      }
      a.copy(a.unitToAfterFlow.get(s), savedFlow);
      i.handleAfterAnalysisEvent(new FlowInfo<A, Stmt>(savedFlow, s, false));
    }

    InteractionHandler stop(Object s) {
      InteractionHandler h = InteractionHandler.v();
      List<?> stopList = h.getStopUnitList();
      if (stopList != null && stopList.contains(s)) {
        h.handleStopAtNodeEvent(s);
      }
      return h;
    }

    public <A> void handleFlowIn(FlowAnalysis<A> a, Stmt s) {
    }

    public <A> void handleFlowOut(FlowAnalysis<A> a, Stmt s) {
    }
  }

  enum GraphView {
    BACKWARD {
      @Override
      Collection<Stmt> getEntries(StmtGraph g) {
        return g.getTails();
      }

      @Override
      Collection<Stmt> getOut(StmtGraph g, Stmt s) {
        return g.successors(s);
      }
    },
    FORWARD {
      @Override
      Collection<Stmt> getEntries(StmtGraph g) {
        return g.getEntrypoints();
      }

      @Override
      Collection<Stmt> getOut(StmtGraph g, Stmt s) {
        return g.successors(s);
      }
    };

    abstract Collection<Stmt> getEntries(StmtGraph g);

    abstract Collection<Stmt> getOut(StmtGraph g, Stmt s);
  }

  /** Maps graph nodes to OUT sets. */
  protected Map<Stmt, A> unitToAfterFlow;

  /** Filtered: Maps graph nodes to OUT sets. */
  protected Map<Stmt, A> filterUnitToAfterFlow = Collections.emptyMap();

  /** Constructs a flow analysis on the given <code>DirectedGraph</code>. */
  public FlowAnalysis(StmtGraph graph) {
    super(graph);

    unitToAfterFlow = new IdentityHashMap<Stmt, A>(graph.nodes().size() * 2 + 1);
  }

  /**
   * Given the merge of the <code>out</code> sets, compute the <code>in</code> set for <code>s</code> (or in to out,
   * depending on direction).
   *
   * This function often causes confusion, because the same interface is used for both forward and backward flow analyses.
   * The first parameter is always the argument to the flow function (i.e. it is the "in" set in a forward analysis and the
   * "out" set in a backward analysis), and the third parameter is always the result of the flow function (i.e. it is the
   * "out" set in a forward analysis and the "in" set in a backward analysis).
   *
   * @param in
   *          the input flow
   * @param d
   *          the current node
   * @param out
   *          the returned flow
   **/
  protected abstract void flowThrough(A in, Stmt d, A out);

  /** Accessor function returning value of OUT set for s. */

  public A getFlowAfter(Stmt s) {
    A a = unitToAfterFlow.get(s);
    return a == null ? newInitialFlow() : a;
  }

  @Override
  public A getFlowBefore(Stmt s) {
    A a = unitToBeforeFlow.get(s);
    return a == null ? newInitialFlow() : a;
  }

  private void initFlow(Iterable<Entry<Stmt, A>> universe, Map<Stmt, A> in, Map<Stmt, A> out) {
    assert universe != null;
    assert in != null;
    assert out != null;

    // If a node has only a single in-flow, the in-flow is always equal
    // to the out-flow if its predecessor, so we use the same object.
    // this saves memory and requires less object creation and copy calls.

    // Furthermore a node can be marked as omissible, this allows us to use
    // the same "flow-set" for out-flow and in-flow. A merge node with within
    // a real scc cannot be omitted, as it could cause endless loops within
    // the fixpoint-iteration!

    for (Entry<Stmt, A> n : universe) {
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

      // for legacy api
      in.put(n.data, n.inFlow);
      out.put(n.data, n.outFlow);
    }
  }

  /**
   * If a flow node can be omitted return <code>true</code>, otherwise <code>false</code>. There is no guarantee a node will
   * be omitted. A omissible node does not influence the result of an analysis.
   *
   * If you are unsure, don't overwrite this method
   *
   * @param n
   *          the node to check
   * @return <code>false</code>
   */
  protected boolean omissible(Stmt n) {
    return false;
  }

  /**
   * You can specify which flow set you would like to use of node {@code from}
   *
   * @param from
   * @param mergeNode
   * @return Flow.OUT
   */
  protected Flow getFlow(Stmt from, Stmt mergeNode) {
    return Flow.OUT;
  }

  private A getFlow(Entry<Stmt, A> o, Entry<Stmt, A> e) {
    return (o.inFlow == o.outFlow) ? o.outFlow : getFlow(o.data, e.data).getFlow(o);
  }

  private void meetFlows(Entry<Stmt, A> e) {
    assert e.in.length >= 1;

    if (e.in.length > 1) {
      boolean copy = true;
      for (Entry<Stmt, A> o : e.in) {
        A flow = getFlow(o, e);
        if (copy) {
          copy = false;
          copy(flow, e.inFlow);
        } else {
          mergeInto(e.data, e.inFlow, flow);
        }
      }
    }
  }

  final int doAnalysis(GraphView gv, InteractionFlowHandler ifh, Map<Stmt, A> inFlow, Map<Stmt, A> outFlow) {
    assert gv != null;
    assert ifh != null;

    ifh = Options.v().interactive_mode() ? ifh : InteractionFlowHandler.NONE;

    final List<Entry<Stmt, A>> universe = Orderer.INSTANCE.newUniverse(graph, gv, entryInitialFlow(), isForward());
    initFlow(universe, inFlow, outFlow);

    Queue<Entry<Stmt, A>> q = PriorityQueue.of(universe, true);

    // Perform fixed point flow analysis
    for (int numComputations = 0;; numComputations++) {
      Entry<Stmt, A> e = q.poll();
      if (e == null) {
        return numComputations;
      }

      meetFlows(e);

      // Compute beforeFlow and store it.
      ifh.handleFlowIn(this, e.data);
      boolean hasChanged = flowThrough(e);
      ifh.handleFlowOut(this, e.data);

      // Update queue appropriately
      if (hasChanged) {
        q.addAll(Arrays.asList(e.out));
      }
    }
  }

  private boolean flowThrough(Entry<Stmt, A> d) {
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
      // copy back the result, as it has changed
      copy(out, d.outFlow);
      return true;
    }

    // no back-references, just calculate "flowThrough"
    flowThrough(d.inFlow, d.data, d.outFlow);
    return true;
  }

}
