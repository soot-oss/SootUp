package de.upb.soot.graph;

import de.upb.soot.core.Body;
import de.upb.soot.jimple.common.stmt.IStmt;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999 Patrice Pominville, Raja Vallee-Rai
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

import java.util.HashMap;
import java.util.List;

/**
 * Represents a CFG where the nodes are IStmt instances, and where no edges are included to account for control flow
 * associated with exceptions.
 *
 * @see IStmt
 * @see AbstractStmtGraph
 */
public class BriefStmtGraph extends AbstractStmtGraph {

  /**
   * Constructs a BriefUnitGraph given a Body instance.
   *
   * @param body
   *          The underlying body we want to make a graph for.
   */
  public BriefStmtGraph(Body body) {
    super(body);
    int size = orderedStmts.size();
    stmtToSuccs = new HashMap<>(size * 2 + 1, 0.7f);
    stmtToPreds = new HashMap<>(size * 2 + 1, 0.7f);
    buildUnexceptionalEdges(stmtToSuccs, stmtToPreds);
    buildHeadsAndTails();
  }

}
