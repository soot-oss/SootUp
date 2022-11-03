package de.upb.swt.soot.callgraph.spark.solver;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2002-2021 Ondrej Lhotak, Kadiray Karakaya and others
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

import de.upb.swt.soot.callgraph.spark.pag.PointerAssignmentGraph;
import de.upb.swt.soot.callgraph.spark.pag.nodes.VariableNode;
import java.util.HashSet;
import java.util.Set;

public class TopologicalSorter {

  private final PointerAssignmentGraph pag;
  private boolean ignoreTypes;
  private int nextFinishNumber = 1;
  private Set<VariableNode> visited;

  public TopologicalSorter(PointerAssignmentGraph pag, boolean ignoreTypes) {
    this.pag = pag;
    this.ignoreTypes = ignoreTypes;
    this.visited = new HashSet<>();
  }

  public void sort() {
    for (VariableNode node : pag.getVariableNodes()) {
      dfsVisit(node);
    }
  }

  private void dfsVisit(VariableNode node) {
    if (!visited.contains(node)) {
      visited.add(node);
      Set<VariableNode> successors = pag.getSimpleEdges().get(node);
      if (successors != null) {
        for (VariableNode element : successors) {
          if (ignoreTypes || pag.getTypeHierarchy().canCast(node.getType(), element.getType())) {
            dfsVisit(element);
          }
        }
      }
      node.setFinishingNumber(nextFinishNumber++);
    }
  }
}
