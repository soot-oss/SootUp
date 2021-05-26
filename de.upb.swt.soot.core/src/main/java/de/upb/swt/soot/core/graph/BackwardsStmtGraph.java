package de.upb.swt.soot.core.graph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2021 Raja Vallee-Rai, Zun Wang
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
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import javax.annotation.Nonnull;

/** @auther Zun Wang */
public class BackwardsStmtGraph extends StmtGraphImpl {

  @Nonnull private final ArrayList<List<Stmt>> predecessors = new ArrayList<>();
  @Nonnull private final ArrayList<List<Stmt>> successors = new ArrayList<>();
  @Nonnull private final List<Stmt> startingStmts = new ArrayList<>();
  @Nonnull private final Map<Stmt, Integer> stmtToIdx = new HashMap<>();
  @Nonnull private final List<Trap> traps;

  public BackwardsStmtGraph(@Nonnull StmtGraph stmtGraph) {
    Set<Stmt> nodes = stmtGraph.nodes();
    int idx = 0;
    for (Stmt node : nodes) {
      stmtToIdx.put(node, idx);
      List<Stmt> preds = stmtGraph.predecessors(node);
      this.successors.add(preds);
      List<Stmt> succs = stmtGraph.successors(node);
      this.predecessors.add(succs);
      if (succs.isEmpty()) {
        this.startingStmts.add(node);
      }
      idx++;
    }
    this.traps = stmtGraph.getTraps();
  }

  @Override
  public Stmt getStartingStmt() {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  public List<Stmt> getStartingStmts() {
    return this.startingStmts;
  }

  @Nonnull
  @Override
  public Set<Stmt> nodes() {
    return Collections.unmodifiableSet(stmtToIdx.keySet());
  }

  @Override
  public boolean containsNode(@Nonnull Stmt node) {
    return stmtToIdx.containsKey(node);
  }

  @Nonnull
  @Override
  public List<Stmt> predecessors(@Nonnull Stmt node) {
    if (!this.containsNode(node)) {
      throw new RuntimeException(
          "The stmt " + node.toString() + "is not in this BackwardingStmtGraph");
    }
    int idx = stmtToIdx.get(node);
    List<Stmt> stmts = predecessors.get(idx);

    return Collections.unmodifiableList(stmts);
  }

  @Nonnull
  @Override
  public List<Stmt> successors(@Nonnull Stmt node) {
    if (!this.containsNode(node)) {
      throw new RuntimeException(
          "The stmt " + node.toString() + "is not in this BackwardingStmtGraph");
    }
    int idx = stmtToIdx.get(node);
    List<Stmt> stmts = successors.get(idx);

    return Collections.unmodifiableList(stmts);
  }

  @Override
  public int inDegree(@Nonnull Stmt node) {
    return this.predecessors(node).size();
  }

  @Override
  public int outDegree(@Nonnull Stmt node) {
    return this.successors(node).size();
  }

  @Override
  public boolean hasEdgeConnecting(@Nonnull Stmt source, @Nonnull Stmt target) {
    List<Stmt> stmts = this.successors(source);
    if (!this.containsNode(target)) {
      throw new RuntimeException(
          "The stmt " + target.toString() + "is not in this BackwardingStmtGraph");
    }
    return stmts.contains(target);
  }

  @Nonnull
  @Override
  public List<Trap> getTraps() {
    return this.traps;
  }
}
