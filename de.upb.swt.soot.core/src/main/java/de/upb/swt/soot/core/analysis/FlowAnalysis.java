package de.upb.swt.soot.core.analysis;
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

import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Numberable;
import de.upb.swt.soot.core.jimple.common.stmt.JGotoStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;

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

    static class Entry<D, F> implements Numberable {
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
            number = Integer.MIN_VALUE;
            isRealStronglyConnected = false;
        }

        @Override
        public String toString() {
            return data == null ? "" : data.toString();
        }

        @Override
        public void setNumber(int n) {
            number = n;
        }

        @Override
        public int getNumber() {
            return number;
        }
    }

    enum Orderer {
        INSTANCE;

        /**
         * Creates a new {@code Entry} graph based on a {@code DirectedGraph}. This includes pseudo topological order, local
         * access for predecessors and successors, a graph entry-point, a {@code Numberable} interface and a real strongly
         * connected component marker.
         *
         * @param stmtGraph Stmt Graph
         * @param graphView Graph View
         * @param entryFlow entryFlow
         * @return universe
         */
        <F> List<Entry<Stmt, F>> newUniverse(StmtGraph stmtGraph, GraphView graphView, F entryFlow, boolean isForward) {
            final int n = stmtGraph.nodes().size();

            Deque<Entry<Stmt, F>> s = new ArrayDeque<>(n);
            List<Entry<Stmt, F>> universe = new ArrayList<>(n);
            Map<Stmt, Entry<Stmt, F>> visited = new HashMap<>(((n + 1) * 4) / 3);

            // out of universe node
            Entry<Stmt, F> superEntry = new Entry<>(null, null);

            List<Stmt> entries;
            List<Stmt> actualEntries = graphView.getEntries(stmtGraph);

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
                    entries = new ArrayList<>();

                    // a single head is expected
                    assert stmtGraph.getEntrypoints().size() == 1;
                    Stmt head = stmtGraph.getEntrypoints().iterator().next();

                    Set<Stmt> visitedNodes = new HashSet<>();
                    List<Stmt> workList = new ArrayList<>();
                    Stmt current;

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

                        for (Stmt next : stmtGraph.successors(current)) {
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

            Entry<Stmt, F>[] sv = new Entry[stmtGraph.nodes().size()];
            int[] si = new int[stmtGraph.nodes().size()];
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

                        visitEntry(visited, w, graphView.getOut(stmtGraph, w.data));

                        // save old
                        si[index] = i;
                        sv[index] = v;
                        index++;

                        i = 0;
                        v = w;
                    }
                } else {
                    if (index == 0) {
                        assert universe.size() <= stmtGraph.nodes().size();
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

        private <F> Entry<Stmt, F>[] visitEntry(Map<Stmt, Entry<Stmt, F>> visited, Entry<Stmt, F> v, List<Stmt> out) {
            int n = out.size();
            Entry<Stmt, F>[] a = new Entry[n];

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
            Entry<D, F> newEntry = new Entry<>(d, v);
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

    enum GraphView {
        BACKWARD {
            @Override
            List<Stmt> getEntries(StmtGraph g) {
                return new ArrayList<>(g.getTails());
            }

            @Override
            List<Stmt> getOut(StmtGraph g, Stmt s) {
                return g.predecessors(s);
            }
        },
        FORWARD {
            @Override
            List<Stmt> getEntries(StmtGraph g) {
                return new ArrayList<>(g.getEntrypoints());
            }

            @Override
            List<Stmt> getOut(StmtGraph g, Stmt s) {
                return g.successors(s);
            }
        };

        abstract List<Stmt> getEntries(StmtGraph g);

        abstract List<Stmt> getOut(StmtGraph g, Stmt s);
    }

    /** Maps graph nodes to OUT sets. */
    protected Map<Stmt, A> stmtToAfterFlow;

    /** Filtered: Maps graph nodes to OUT sets. */
    protected Map<Stmt, A> filterStmtToAfterFlow = Collections.emptyMap();

    /** Constructs a flow analysis on the given <code>DirectedGraph</code>. */
    protected FlowAnalysis(StmtGraph graph) {
        super(graph);

        stmtToAfterFlow = new IdentityHashMap<>(graph.nodes().size() * 2 + 1);
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
     * @param stmt
     *          the current node
     * @param out
     *          the returned flow
     **/
    protected abstract void flowThrough(A in, Stmt stmt, A out);

    /** Accessor function returning value of OUT set for s. */

    public A getFlowAfter(Stmt s) {
        A a = stmtToAfterFlow.get(s);
        return a == null ? newInitialFlow() : a;
    }

    @Override
    public A getFlowBefore(Stmt s) {
        A a = stmtToBeforeFlow.get(s);
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
                // We could recalculate the graph itself but that's more expensive than
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

    final int doAnalysis(GraphView graphView, Map<Stmt, A> inFlow, Map<Stmt, A> outFlow) {
        assert graphView != null;

        final List<Entry<Stmt, A>> universe = Orderer.INSTANCE.newUniverse(graph, graphView, entryInitialFlow(), isForward());
        initFlow(universe, inFlow, outFlow);

        Queue<Entry<Stmt, A>> q = new PriorityQueue<>(universe);

        // Perform fixed point flow analysis
        for (int numComputations = 0;; numComputations++) {
            Entry<Stmt, A> e = q.poll();
            if (e == null) {
                return numComputations;
            }

            meetFlows(e);

            // Compute beforeFlow and store it.
            // TODO: is ifh necessary?
            //ifh.handleFlowIn(this, e.data);
            boolean hasChanged = flowThrough(e);
            //ifh.handleFlowOut(this, e.data);

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
