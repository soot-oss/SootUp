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
import de.upb.swt.soot.core.jimple.common.stmt.*;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** @author Markus Schmidt */
// [ms] possible performance improvement: on instantiation assign: Collection.singleTonList()
// directly and not on demand -> change type of sucessors/predecessors to List<Stmt> this removes
// additional checks in successor()/predecessor()
public class ImmutableStmtGraph extends StmtGraph {

  @Nonnull
  protected final Map<Stmt, Integer>
      nodeToIndex; // maps a Stmt to its elementIndex in successors[] and predecessors[]

  @Nonnull
  private final Object[]
      successors; // these Arrays consists of elements of type Stmt and List<Stmt>
  // to reduce memory consumption we assign Stmt if there is only one item in the list.
  // if the element is requested we upgrade the element to the returned list - "lazy loading"
  @Nonnull private final Object[] predecessors;

  @Nullable private final Stmt startingStmt;
  @Nonnull private final List<Trap> traps;

  /** creates an immutable copy of the given stmtGraph. */
  protected ImmutableStmtGraph(StmtGraph originalStmtGraph) {
    final Set<Stmt> nodes = originalStmtGraph.nodes();
    final int nodeSize = nodes.size();
    nodeToIndex = new HashMap<>(nodeSize);
    predecessors = new Object[nodeSize];
    successors = new Object[nodeSize];

    startingStmt = originalStmtGraph.getStartingStmt();
    traps = new ArrayList<>(originalStmtGraph.getTraps());

    int idx = 0;
    for (Stmt node : nodes) {
      nodeToIndex.put(node, idx);

      final List<Stmt> pred = originalStmtGraph.predecessors(node);
      switch (pred.size()) {
        case 0:
          predecessors[idx] = Collections.emptyList();
          break;
        case 1:
          predecessors[idx] = pred.get(0);
          break;
        default:
          predecessors[idx] = Collections.unmodifiableList(new ArrayList<>(pred));
      }

      final List<Stmt> succ = originalStmtGraph.successors(node);
      switch (succ.size()) {
        case 0:
          successors[idx] = Collections.emptyList();
          break;
        case 1:
          successors[idx] = succ.get(0);
          break;
        default:
          successors[idx] = Collections.unmodifiableList(new ArrayList<>(succ));
      }
      idx++;
    }
  }

  /**
   * creates an immutable copy of the given stmtGraph if necessary and validates the starting Stmt
   */
  public static ImmutableStmtGraph copyOf(StmtGraph stmtGraph) {
    if (stmtGraph instanceof ImmutableStmtGraph) {
      return (ImmutableStmtGraph) stmtGraph;
    }

    if (stmtGraph.nodes().size() > 0) {
      final Stmt startingStmt = stmtGraph.getStartingStmt();
      if (startingStmt == null) {
        throw new RuntimeException("The starting Stmt must be set.");
      }
      if (!stmtGraph.nodes().contains(startingStmt)) {
        throw new RuntimeException(
            "The starting Stmt '" + startingStmt + "' must exist in the StmtGraph.");
      }
    }
    return new ImmutableStmtGraph(stmtGraph);
  }

  @Override
  public Stmt getStartingStmt() {
    return startingStmt;
  }

  @Nonnull
  @Override
  public Set<Stmt> nodes() {
    return Collections.unmodifiableSet(nodeToIndex.keySet());
  }

  @Override
  public boolean containsNode(@Nonnull Stmt node) {
    return nodeToIndex.containsKey(node);
  }

  @Nonnull
  @Override
  public List<Stmt> predecessors(@Nonnull Stmt node) {
    final Integer idx = nodeToIndex.get(node);
    if (idx == null) {
      throw new RuntimeException("The given Stmt is not a node in the Graph.");
    }
    if (predecessors[idx] instanceof Stmt) {
      final List<Stmt> stmts = Collections.singletonList((Stmt) predecessors[idx]);
      predecessors[idx] = stmts;
      return stmts;
    }
    return (List<Stmt>) predecessors[idx];
  }

  @Nonnull
  @Override
  public List<Stmt> successors(@Nonnull Stmt node) {
    final Integer idx = nodeToIndex.get(node);
    if (idx == null) {
      throw new RuntimeException("The given Stmt is not a node in the Graph.");
    }
    if (successors[idx] instanceof Stmt) {
      final List<Stmt> stmts = Collections.singletonList((Stmt) successors[idx]);
      successors[idx] = stmts;
      return stmts;
    }
    return (List<Stmt>) successors[idx];
  }

  @Override
  public int inDegree(@Nonnull Stmt node) {
    return predecessors(node).size();
  }

  @Override
  public int outDegree(@Nonnull Stmt node) {
    return successors(node).size();
  }

  @Override
  public boolean hasEdgeConnecting(@Nonnull Stmt source, @Nonnull Stmt target) {
    return successors(source).contains(target);
  }

  @Nonnull
  @Override
  public List<Trap> getTraps() {
    return Collections.unmodifiableList(traps);
  }
}
