package de.upb.swt.soot.core.graph;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Markus Schmidt
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
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.BranchingStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JIfStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JSwitchStmt;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * graph structure which keeps node and edge insertion order to store information about successive
 * stmts in edges. Ordered edges are needed because, this stores the target information of {@link
 * BranchingStmt}s so that in conditional branches (e.g. JSwicthStmt or JIfStmt ) we can associate
 * the i-th item with the i-th branch case. In a StmtGraph it is not allowed to have unconnected
 * Nodes.
 *
 * <pre>
 *  Stmt stmt1, stmt2;
 *  ...
 *  MutableStmtGraph graph = new MutableStmtGraph();
 *  graph.setEntryPoint(stmt1);
 *  graph.addNode(stmt1);
 *  graph.addNode(stmt2);
 *  graph.putEdge(stmt1, stmt2);
 * </pre>
 *
 * @author Markus Schmidt
 */

// TODO: [ms] FIXME: zero or one stmts in body
public class MutableStmtGraph extends StmtGraph {

  @Nonnull protected final ArrayList<List<Stmt>> predecessors;
  @Nonnull protected final ArrayList<List<Stmt>> successors;
  @Nonnull protected final Map<Stmt, Integer> stmtToIdx;
  private int nextFreeId = 0;

  @Nullable protected Stmt startingStmt;
  @Nonnull protected List<Trap> traps = Collections.emptyList();

  /** creates an empty instance of MutableStmtGraph */
  public MutableStmtGraph() {
    predecessors = new ArrayList<>();
    successors = new ArrayList<>();
    stmtToIdx = new HashMap<>();
  }

  /** creates a mutable copy(!) of originalStmtGraph */
  public MutableStmtGraph(@Nonnull StmtGraph originalStmtGraph) {
    setStartingStmt(originalStmtGraph.getStartingStmt());

    final Set<Stmt> nodes = originalStmtGraph.nodes();
    final int nodeSize = nodes.size();
    predecessors = new ArrayList<>(nodeSize);
    successors = new ArrayList<>(nodeSize);
    stmtToIdx = new HashMap<>(nodeSize);

    for (Stmt node : nodes) {
      int idx = addNode(node);

      final List<Stmt> pred = originalStmtGraph.predecessors(node);
      predecessors.set(idx, new ArrayList<>(pred));

      final List<Stmt> succ = originalStmtGraph.successors(node);
      successors.set(idx, new ArrayList<>(succ));
    }
  }

  public StmtGraph unmodifiableStmtGraph() {
    return new ForwardingStmtGraph(this);
  }

  public void setStartingStmt(@Nonnull Stmt firstStmt) {
    this.startingStmt = firstStmt;
  }

  public void setTraps(@Nonnull List<Trap> traps) {
    this.traps = traps;
  }

  @Override
  @Nonnull
  public List<Trap> getTraps() {
    return traps;
  }

  @Nullable
  public Stmt getStartingStmt() {
    return startingStmt;
  }

  public int addNode(@Nonnull Stmt node) {
    final int idx = nextFreeId++;
    stmtToIdx.put(node, idx);
    predecessors.add(
        new ArrayList<>(1)); // [ms] hint: wastes an entry if its a TrapHandler or the first Stmt

    final int calculatedSuccessorSize;
    if (node instanceof JSwitchStmt) {
      calculatedSuccessorSize = ((JSwitchStmt) node).getValueCount();
    } else if (node instanceof JIfStmt) {
      calculatedSuccessorSize = 2;
    }
    /* memory- vs runtime+
      else if( node instanceof JReturnStmt || node instanceof JThrowStmt ){
      successors.add( Collections.emptyList());
      return idx;
    }*/
    else {
      calculatedSuccessorSize = 1;
    }

    // sets successors at successors[idx]
    successors.add(new ArrayList<>(calculatedSuccessorSize));
    return idx;
  }

  private int getNodeIdx(@Nonnull Stmt node) {
    Integer idx = stmtToIdx.get(node);
    if (idx == null) {
      throw new RuntimeException("'" + node + "' is currently not a Node in this StmtGraph.");
    }
    return idx;
  }

  private int getNodeIdxOrCreate(@Nonnull Stmt node) {
    Integer idx = stmtToIdx.get(node);
    if (idx == null) {
      idx = addNode(node);
    }
    return idx;
  }

  public boolean containsNode(@Nonnull Stmt node) {
    return stmtToIdx.containsKey(node);
  }

  public void removeEdge(@Nonnull Stmt from, @Nonnull Stmt to) {
    int fromIdx = getNodeIdx(from);
    int toIdx = getNodeIdx(to);

    final List<Stmt> pred = predecessors.get(toIdx);
    if (pred != null) {
      pred.remove(from);
      if (degree(to) == 0) {
        stmtToIdx.remove(to);
      }
    }
    final List<Stmt> succ = successors.get(fromIdx);
    if (succ != null) {
      succ.remove(to);
      if (degree(from) == 0) {
        stmtToIdx.remove(from);
      }
    }
  }

  public void setEdges(@Nonnull Stmt from, @Nonnull List<Stmt> targets) {
    int fromIdx = getNodeIdxOrCreate(from);

    targets.forEach(
        target -> {
          if (!containsNode(target)) {
            if (from == target) {
              throw new RuntimeException("A Stmt can't flow to itself.");
            }
            addNode(target);
          }
        });

    // cleanup existing edges to the successors of *from* Stmt before replacing it with the new list
    // of successors
    successors(from).forEach(succ -> predecessors.get(getNodeIdx(succ)).remove(from));

    // add *from* Stmt as predecessor to every *target* Stmt
    for (Stmt target : targets) {
      predecessors.get(getNodeIdxOrCreate(target)).add(from);
    }
    // set list of successors
    successors.set(fromIdx, targets);
  }

  public void putEdge(@Nonnull Stmt from, @Nonnull Stmt to) {

    int fromIdx = getNodeIdxOrCreate(from);
    int toIdx = getNodeIdxOrCreate(to);

    predecessors.get(toIdx).add(from);
    successors.get(fromIdx).add(to);
  }

  @Override
  @Nonnull
  public Set<Stmt> nodes() {
    return Collections.unmodifiableSet(stmtToIdx.keySet());
  }

  @Override
  @Nonnull
  public List<Stmt> predecessors(@Nonnull Stmt node) {
    int nodeIdx = getNodeIdx(node);
    final List<Stmt> stmts = predecessors.get(nodeIdx);
    if (stmts == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(stmts);
  }

  @Override
  @Nonnull
  public List<Stmt> successors(@Nonnull Stmt node) {
    int nodeIdx = getNodeIdx(node);
    final List<Stmt> stmts = successors.get(nodeIdx);

    if (stmts == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(stmts);
  }

  @Override
  public int inDegree(@Nonnull Stmt node) {
    int nodeIdx = getNodeIdx(node);
    final List<Stmt> stmts = predecessors.get(nodeIdx);
    return stmts == null ? 0 : stmts.size();
  }

  @Override
  public int outDegree(@Nonnull Stmt node) {
    int nodeIdx = getNodeIdx(node);
    final List<Stmt> stmts = successors.get(nodeIdx);
    return stmts == null ? 0 : stmts.size();
  }

  @Override
  public boolean hasEdgeConnecting(@Nonnull Stmt from, @Nonnull Stmt to) {
    int fromIdx = getNodeIdx(from);
    final List<Stmt> stmts = successors.get(fromIdx);
    return stmts != null && stmts.contains(to);
  }

  /**
   * Replace a stmt in StmtGraph with a new stmt and adapts the existing incoming and outgoing flows
   * to the new stmt
   *
   * @param oldStmt a stmt which is already in the StmtGraph
   * @param newStmt a new stmt which will replace the old stmt
   */
  public void replaceNode(@Nonnull Stmt oldStmt, @Nonnull Stmt newStmt) {

    if (oldStmt.getSuccessorCount() != newStmt.getSuccessorCount()) {
      throw new RuntimeException(
          "You can only use replaceNode if newStmt has the same amount of branches/outgoing flows.");
    }

    if (oldStmt == startingStmt) {
      startingStmt = newStmt;
    }
    final Integer integer = stmtToIdx.get(oldStmt);
    if (integer == null) {
      throw new RuntimeException("The StmtGraph does not contain" + oldStmt);
    }
    int idx = integer;
    stmtToIdx.remove(oldStmt, idx);
    stmtToIdx.put(newStmt, idx);

    for (Stmt pred : predecessors.get(idx)) {
      List<Stmt> succsList = successors.get(stmtToIdx.get(pred));
      int succIdx = succsList.indexOf(oldStmt);
      succsList.set(succIdx, newStmt);
    }

    for (Stmt succ : successors.get(idx)) {
      List<Stmt> predsList = predecessors.get(stmtToIdx.get(succ));
      int predIdx = predsList.indexOf(oldStmt);
      predsList.set(predIdx, newStmt);
    }
  }

  /**
   * Remove a node from the graph. The succs and preds after the node removing don't have any
   * connection to each other. the removal of b in "a->b->c" does NOT connect "a->c"
   *
   * @param node a stmt to be removed from the StmtGraph
   */
  public void removeNode(@Nonnull Stmt node) {

    final Integer integer = stmtToIdx.get(node);
    if (integer == null) {
      return;
    }
    final int nodeIdx = integer;
    // remove node from index map
    stmtToIdx.remove(node);

    // unset startingstmt if node is currently the startingstmt
    if (startingStmt == node) {
      startingStmt = null;
    }

    // remove node from successor list of nodes predecessors
    final List<Stmt> preds = predecessors.get(nodeIdx);
    preds.forEach(pred -> successors.get(getNodeIdx(pred)).remove(node));
    // invalidate entry for node itself to allow gc
    predecessors.set(nodeIdx, null);

    // remove node from the predecessor list of a nodes successors
    final List<Stmt> succs = successors.get(nodeIdx);
    succs.forEach(succ -> predecessors.get(getNodeIdx(succ)).remove(node));
    // invalidate entry for node itself to allow gc
    successors.set(nodeIdx, null);
  }
}
