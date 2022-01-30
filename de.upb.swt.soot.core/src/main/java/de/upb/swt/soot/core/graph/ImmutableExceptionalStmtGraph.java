package de.upb.swt.soot.core.graph;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2021 Zun Wang
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
import de.upb.swt.soot.core.types.ClassType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

/** @author Zun Wang */
public final class ImmutableExceptionalStmtGraph extends ImmutableStmtGraph {

  @Nonnull private final Object[] exceptionalPreds;
  @Nonnull private final Object[] exceptionalSuccs;
  @Nonnull private final Object[] exceptionalDestinationTraps;

  public ImmutableExceptionalStmtGraph(@Nonnull StmtGraph graph) {
    super(graph);
    // initialize exceptionalPreds and exceptionalSuccs
    int size = graph.nodes().size();
    exceptionalPreds = new Object[size];
    exceptionalSuccs = new Object[size];
    exceptionalDestinationTraps = new Object[size];

    if (!graph.getTraps().isEmpty()) {
      MutableExceptionalStmtGraph mutableGraph = new MutableExceptionalStmtGraph(graph);
      for (Stmt stmt : this.nodeToIndex.keySet()) {
        int idx = this.nodeToIndex.get(stmt);

        List<Stmt> preds = mutableGraph.exceptionalPredecessors(stmt);
        switch (preds.size()) {
          case 0:
            exceptionalPreds[idx] = Collections.emptyList();
            break;
          case 1:
            exceptionalPreds[idx] = Collections.singletonList(preds.get(0));
            break;
          default:
            exceptionalPreds[idx] = Collections.unmodifiableList(new ArrayList<>(preds));
        }
        List<Stmt> succs = mutableGraph.exceptionalSuccessors(stmt);
        switch (succs.size()) {
          case 0:
            exceptionalSuccs[idx] = Collections.emptyList();
            break;
          case 1:
            exceptionalSuccs[idx] = Collections.singletonList(succs.get(0));
            break;
          default:
            exceptionalSuccs[idx] = Collections.unmodifiableList(new ArrayList<>(succs));
        }
        List<Trap> dests = mutableGraph.getDestTraps(stmt);
        switch (dests.size()) {
          case 0:
            exceptionalDestinationTraps[idx] = Collections.emptyList();
            break;
          case 1:
            exceptionalDestinationTraps[idx] = Collections.singletonList(dests.get(0));
            break;
          default:
            exceptionalDestinationTraps[idx] = Collections.unmodifiableList(new ArrayList<>(dests));
        }
      }
    }
  }

  @Nonnull
  public List<Stmt> exceptionalPredecessors(@Nonnull Stmt stmt) {
    final Integer idx = this.nodeToIndex.get(stmt);
    if (idx == null) {
      throw new RuntimeException("The given Stmt is not a node in the Graph.");
    }
    return (List<Stmt>) exceptionalPreds[idx];
  }

  @Nonnull
  public Map<ClassType, Stmt> exceptionalSuccessors(@Nonnull Stmt stmt) {
    final Integer idx = this.nodeToIndex.get(stmt);
    if (idx == null) {
      throw new RuntimeException("The given Stmt is not a node in the Graph.");
    }
    return (List<Stmt>) exceptionalSuccs[idx];
  }

  @Nonnull
  public List<Trap> getDestTraps(@Nonnull Stmt stmt) {
    final Integer idx = this.nodeToIndex.get(stmt);
    if (idx == null) {
      throw new RuntimeException("The given Stmt is not a node in the Graph.");
    }
    return (List<Trap>) exceptionalDestinationTraps[idx];
  }
}
