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
import de.upb.swt.soot.callgraph.spark.pag.nodes.Node;
import de.upb.swt.soot.callgraph.spark.pag.nodes.VariableNode;
import de.upb.swt.soot.callgraph.typehierarchy.TypeHierarchy;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Collapses VarNodes (green) forming strongly-connected components in the pointer assignment graph.
 */
public class SCCCollapser implements Collapser {
  private PointerAssignmentGraph pag;
  private boolean ignoreTypes;
  private HashSet<VariableNode> visited;
  private int numCollapsed = 0;
  private TypeHierarchy typeHierarchy;

  public SCCCollapser(PointerAssignmentGraph pag, boolean ignoreTypes) {
    this.pag = pag;
    this.ignoreTypes = ignoreTypes;
    typeHierarchy = pag.getTypeHierarchy();
    visited = new HashSet<>();
  }

  public void collapse() {
    new TopologicalSorter(pag, ignoreTypes).sort();
    TreeSet<VariableNode> set = new TreeSet<>();
    set.addAll(pag.getVariableNodes());
    for (VariableNode v : set) {
      dfsVisit(v, v);
    }
    visited = null;
  }

  protected final void dfsVisit(VariableNode v, VariableNode rootOfSCC) {
    if (visited.contains(v)) {
      return;
    }
    visited.add(v);
    Set<VariableNode> succs = pag.simpleInvLookup(v);
    if (succs != null && !succs.isEmpty()) {
      for (VariableNode element : succs) {
        if (ignoreTypes || typeHierarchy.canCast(element.getType(), v.getType())) {
          dfsVisit(element, rootOfSCC);
        }
      }
    }
    handleVariableNotRootOfSCC(v, rootOfSCC);
  }

  private void handleVariableNotRootOfSCC(VariableNode v, VariableNode rootOfSCC) {
    if (v != rootOfSCC) {
      if (!ignoreTypes) {
        if (typeHierarchy.canCast(v.getType(), rootOfSCC.getType())
            && typeHierarchy.canCast(rootOfSCC.getType(), v.getType())) {
          rootOfSCC.mergeWith(v);
          numCollapsed++;
        }
      } else {
        handleIgnoreTypes(v, rootOfSCC);
      }
    }
  }

  private void handleIgnoreTypes(VariableNode v, VariableNode rootOfSCC) {
    if (typeHierarchy.canCast(v.getType(), rootOfSCC.getType())) {
      rootOfSCC.mergeWith(v);
    } else if (typeHierarchy.canCast(rootOfSCC.getType(), v.getType())) {
      v.mergeWith(rootOfSCC);
    } else {
      rootOfSCC.getReplacement().setType(null);
      Set<Node> set = rootOfSCC.getPointsToSet();
      if (set != null) {
        // TODO: set.setType null
      }
      rootOfSCC.mergeWith(v);
    }
    numCollapsed++;
  }
}
