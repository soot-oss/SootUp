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
import de.upb.swt.soot.core.jimple.common.stmt.*;
import java.util.*;

/**
 * Interface for control flow graphs on Jimple Stmts. A StmtGraph is directed and connected (except
 * for traphandlers - those are not connected to the unexceptional flow via StmtGraph). Its directed
 * edges represent flows between Stmts. If the edge starts in a branching Stmt there is an edge for
 * each flow to the target Stmt. This can include duplicate flows to the same target e.g. for
 * JSwitchStmt, so that every label has its own flow to a target.
 *
 * @author Markus Schmidt
 */
public abstract class StmtGraphImpl implements StmtGraph {

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (!(o instanceof StmtGraph)) {
      return false;
    }
    StmtGraph otherGraph = (StmtGraph) o;

    if (getStartingStmt() != otherGraph.getStartingStmt()) {
      return false;
    }

    Collection<Stmt> nodes = nodes();
    final Collection<Stmt> otherNodes = otherGraph.nodes();
    if (nodes.size() != otherNodes.size()) {
      return false;
    }

    if (!getTraps().equals(otherGraph.getTraps())) {
      return false;
    }

    for (Stmt node : nodes) {
      if (!otherNodes.contains(node) || !successors(node).equals(otherGraph.successors(node))) {
        return false;
      }
    }

    return true;
  }
}
