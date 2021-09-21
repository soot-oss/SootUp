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

import de.upb.swt.soot.core.graph.ExceptionalStmtGraph;
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.JIdentityStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import java.util.*;
import java.util.stream.IntStream;

/** Analysis that provides an implementation of the LocalDefs interface. */
public class SimpleLocalDefs implements LocalDefs {

  private final LocalDefs def;

  private static class StaticSingleAssignment implements LocalDefs {
    final Map<Local, List<Stmt>> result;

    StaticSingleAssignment(Local[] locals, List<Stmt>[] stmtList) {
      final int N = locals.length;
      assert (N == stmtList.length);

      this.result = new HashMap<>((N * 3) / 2 + 7);
      for (int i = 0; i < N; i++) {
        List<Stmt> curr = stmtList[i];
        if (!curr.isEmpty()) {
          assert (curr.size() == 1);
          result.put(locals[i], curr);
        }
      }
    }

    @Override
    public List<Stmt> getDefsOfAt(Local l, Stmt s) {
      List<Stmt> lst = result.get(l);
      // singleton-lists are immutable
      return lst != null ? lst : Collections.emptyList();
    }

    @Override
    public List<Stmt> getDefsOf(Local l) {
      return getDefsOfAt(l, null);
    }
  } // end inner class StaticSingleAssignment

  private static class FlowAssignment extends ForwardFlowAnalysis<FlowAssignment.FlowBitSet>
      implements LocalDefs {

    class FlowBitSet extends BitSet {
      private static final long serialVersionUID = -8348696077189400377L;

      FlowBitSet() {
        super(universe.length);
      }

      List<Stmt> asList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex < fromIndex || universe.length < toIndex) {
          throw new IndexOutOfBoundsException();
        }
        if (fromIndex == toIndex) {
          return Collections.emptyList();
        }

        if (fromIndex == toIndex - 1) {
          if (this.get(fromIndex)) {
            return Collections.singletonList(universe[fromIndex]);
          } else {
            return Collections.emptyList();
          }
        }

        int i = this.nextSetBit(fromIndex);
        if (i < 0 || i >= toIndex) {
          return Collections.emptyList();
        }
        if (i == toIndex - 1) {
          return Collections.singletonList(universe[i]);
        }

        List<Stmt> elements = new ArrayList<Stmt>(toIndex - i);
        for (; ; ) {
          int endOfRun = Math.min(toIndex, this.nextClearBit(i + 1));
          do {
            elements.add(universe[i++]);
          } while (i < endOfRun);
          if (i >= toIndex) {
            break;
          }
          i = this.nextSetBit(i + 1);
          if (i < 0 || i >= toIndex) {
            break;
          }
        }
        return elements;
      }
    }

    final Map<Local, Integer> locals;
    final List<Stmt>[] stmtList;
    final int[] localRange;
    final Stmt[] universe;

    private Map<Stmt, Integer> indexOfStmt;

    FlowAssignment(
        StmtGraph graph, Local[] locals, List<Stmt>[] stmtList, int numStmts, boolean omitSSA) {
      super(graph);
      this.stmtList = stmtList;
      this.universe = new Stmt[numStmts];
      this.indexOfStmt = new HashMap<>(numStmts);
      final int N = locals.length;
      this.locals = new HashMap<>((N * 3) / 2 + 7);
      this.localRange = new int[N + 1];

      for (int j = 0, i = 0; i < N; this.localRange[++i] = j) {
        List<Stmt> currUnitList = stmtList[i];
        if (currUnitList.isEmpty()) {
          continue;
        }

        this.locals.put(locals[i], i);

        if (currUnitList.size() >= 2) {
          for (Stmt u : currUnitList) {
            this.indexOfStmt.put(u, j);
            this.universe[j++] = u;
          }
        } else if (omitSSA) {
          this.universe[j++] = currUnitList.get(0);
        }
      }
      assert (localRange[N] == numStmts);

      doAnalysis();

      this.indexOfStmt = null; // release memory
    }

    @Override
    protected boolean omissible(Stmt stmt) {
      final List<Value> defs = stmt.getDefs();
      if (!defs.isEmpty()) { // avoid temporary creation of iterators (more like micro-tuning)
        for (Value v : defs) {
          if (v instanceof Local) {
            Local l = (Local) v;
            int lno = locals.get(l); // [kk] instead of l.getNumber()
            return (localRange[lno] == localRange[lno + 1]);
          }
        }
      }
      return true;
    }

    @Override
    protected Flow getFlow(Stmt from, Stmt to) {
      // QND
      if (to instanceof JIdentityStmt && graph instanceof ExceptionalStmtGraph) {
        ExceptionalStmtGraph g = (ExceptionalStmtGraph) graph;
        if (!g.exceptionalPredecessors(to).isEmpty()) {
          // look if there is a real exception edge
          for (Trap trap : g.getDestTraps(from)) {
            if (trap != null && trap.getHandlerStmt() == to) {
              return Flow.IN;
            }
          }
        }
      }
      return Flow.OUT;
    }

    @Override
    protected void flowThrough(FlowBitSet in, Stmt stmt, FlowBitSet out) {
      copy(in, out);

      // reassign all definitions
      for (Value v : stmt.getDefs()) {
        if (v instanceof Local) {
          Local l = (Local) v;
          int lno = locals.get(l);
          int from = localRange[lno];
          int to = localRange[1 + lno];

          if (from == to) {
            continue;
          }

          assert (from <= to);

          if (to - from == 1) {
            // special case: this local has only one def point
            out.set(from);
          } else {
            out.clear(from, to);
            out.set(indexOfStmt.get(stmt));
          }
        }
      }
    }

    @Override
    protected void copy(FlowBitSet source, FlowBitSet dest) {
      if (dest != source) {
        dest.clear();
        dest.or(source);
      }
    }

    @Override
    protected FlowBitSet newInitialFlow() {
      return new FlowBitSet();
    }

    @Override
    protected void mergeInto(Stmt succNode, FlowBitSet inout, FlowBitSet in) {
      inout.or(in);
    }

    @Override
    protected void merge(FlowBitSet in1, FlowBitSet in2, FlowBitSet out) {
      throw new UnsupportedOperationException("should never be called");
    }

    @Override
    public List<Stmt> getDefsOfAt(Local l, Stmt s) {
      Integer lno = locals.get(l);
      if (lno == null) {
        return Collections.emptyList();
      }

      int from = localRange[lno];
      int to = localRange[lno + 1];
      assert (from <= to);

      if (from == to) {
        assert (stmtList[lno].size() == 1);
        // both singletonList is immutable
        return stmtList[lno];
      } else {
        return getFlowBefore(s).asList(from, to);
      }
    }

    @Override
    public List<Stmt> getDefsOf(Local l) {
      List<Stmt> defs = new ArrayList<>();
      for (Stmt s : graph) {
        List<Stmt> defsOf = getDefsOfAt(l, s);
        if (defsOf != null) {
          defs.addAll(defsOf);
        }
      }
      return defs;
    }
  } // end inner class FlowAssignment

  /** @param body */
  public SimpleLocalDefs(Body body) {
    this(body, FlowAnalysisMode.Automatic);
  }

  public SimpleLocalDefs(Body body, FlowAnalysisMode mode) {
    this(body.getStmtGraph(), body.getLocals(), mode);
  }

  SimpleLocalDefs(StmtGraph graph, Collection<Local> locals, FlowAnalysisMode mode) {
    this(graph, locals.toArray(new Local[locals.size()]), mode);
  }

  SimpleLocalDefs(StmtGraph graph, Local[] locals, boolean omitSSA) {
    this(graph, locals, omitSSA ? FlowAnalysisMode.OmitSSA : FlowAnalysisMode.Automatic);
  }

  SimpleLocalDefs(StmtGraph graph, Local[] locals, FlowAnalysisMode mode) {
    this.def = init(graph, locals, mode);
  }

  private LocalDefs init(StmtGraph graph, Local[] locals, FlowAnalysisMode mode) {
    Map<Local, Integer> localToNum = new HashMap<>();
    IntStream.range(0, locals.length).forEach(i -> localToNum.put(locals[i], i));

    List<Stmt>[] stmtList = new List[locals.length];
    Arrays.fill(stmtList, Collections.emptyList());

    final boolean omitSSA = (mode == FlowAnalysisMode.OmitSSA);
    boolean doFlowAnalsis = omitSSA;

    int units = 0;

    // collect all def points
    for (Stmt stmt : graph) {
      for (Value v : stmt.getDefs()) {
        if (v instanceof Local) {
          Local l = (Local) v;
          int lno = localToNum.get(l);

          switch (stmtList[lno].size()) {
            case 0:
              stmtList[lno] = Collections.singletonList(stmt);
              if (omitSSA) {
                units++;
              }
              break;
            case 1:
              if (!omitSSA) {
                units++;
              }
              stmtList[lno] = new ArrayList<Stmt>(stmtList[lno]);
              doFlowAnalsis = true;
              // fallthrough
            default:
              stmtList[lno].add(stmt);
              units++;
              break;
          }
        }
      }
    }

    if (doFlowAnalsis && mode != FlowAnalysisMode.FlowInsensitive) {
      return new FlowAssignment(graph, locals, stmtList, units, omitSSA);
    } else {
      return new StaticSingleAssignment(locals, stmtList);
    }
  }

  @Override
  public List<Stmt> getDefsOfAt(Local l, Stmt s) {
    return def.getDefsOfAt(l, s);
  }

  @Override
  public List<Stmt> getDefsOf(Local l) {
    return def.getDefsOf(l);
  }
}
