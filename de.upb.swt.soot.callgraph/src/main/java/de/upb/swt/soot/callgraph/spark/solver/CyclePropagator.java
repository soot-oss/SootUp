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
import de.upb.swt.soot.callgraph.spark.pag.nodes.*;
import java.util.*;

public class CyclePropagator implements Propagator {

  private PointerAssignmentGraph pag;
  Integer currentIteration;
  Map<VariableNode, Integer> varNode2Iteraton = new HashMap<>();
  // todo: a field OnFlyCallGraph ofcg

  public CyclePropagator(PointerAssignmentGraph pag) {
    this.pag = pag;
  }

  @Override
  public void propagate() {
    // todo: OnFlyCallGraph ofcg, but it seems not support OnFlyCallGraph
    // get all bases of field-reference nodes
    Set<VariableNode> basesSet = new HashSet<>();
    for (FieldReferenceNode refNode : pag.getLoadEdges().keySet()) {
      basesSet.add(refNode.getBase());
    }
    for (FieldReferenceNode refNode : pag.getStoreEdgesInv().keySet()) {
      basesSet.add(refNode.getBase());
    }
    List<VariableNode> bases = new ArrayList<>(basesSet);

    int iteration = 0;
    boolean changed = true;
    boolean isFinalIter = false;

    while (changed) {
      changed = false;
      iteration++;
      currentIteration = iteration;
      for (VariableNode varNode : bases) {
        changed =
            computeP2Set((VariableNode) varNode.getReplacement(), new ArrayList<>()) | changed;
      }

      // if there's change, propagate along the store edges
      if (changed) {
        for (VariableNode storeSrc : pag.getStoreEdges().keySet()) {
          Set<FieldReferenceNode> storeTargets = pag.storeLookup(storeSrc);
          for (FieldReferenceNode storeTarget : storeTargets) {
            Set<Node> p2Nodes = storeTarget.getBase().getPointsToSet();
            for (Node p2Node : p2Nodes) {
              AllocationNode allocNode = (AllocationNode) p2Node;
              AllocationDotField anDotField =
                  pag.getOrCreateAllocationDotField(allocNode, storeTarget.getField());
              anDotField.getOrCreatePointsToSet().addAll(storeSrc.getPointsToSet());
            }
          }
        }
      }

      // if the bases are not changed anymore, then perform all local variable nodes
      if (!changed && !isFinalIter) {
        isFinalIter = true;
        bases = new ArrayList<>(pag.getVariableNodes());
        changed = true;
      }
    }
  }

  private boolean computeP2Set(VariableNode varNode, ArrayList<VariableNode> path) {
    boolean modified = false;
    if (path.contains(varNode)) {
      return false;
    }
    Integer varIteration = varNode2Iteraton.get(varNode);
    if (currentIteration != null && varIteration != null && currentIteration.equals(varIteration)) {
      return false;
    }
    varNode2Iteraton.put(varNode, currentIteration);
    path.add(varNode);

    // propagate along allocation edges
    if (varNode.getPointsToSet().isEmpty()) {
      Set<AllocationNode> allocSrcs = pag.allocInvLookup(varNode);
      if (allocSrcs != null && !allocSrcs.isEmpty()) {
        for (AllocationNode allocSrc : allocSrcs) {
          modified = varNode.getOrCreatePointsToSet().add(allocSrc) | modified;
        }
      }
    }

    // propagate along the path to the varNode recursively
    Set<VariableNode> simpleSrcs = pag.simpleInvLookup(varNode);
    if (simpleSrcs != null && !simpleSrcs.isEmpty()) {
      for (VariableNode simpleSrc : simpleSrcs) {
        modified = computeP2Set(simpleSrc, path) | modified;
        modified = varNode.getOrCreatePointsToSet().addAll(simpleSrc.getPointsToSet()) | modified;
      }
    }
    // propagate along the load edges
    Set<FieldReferenceNode> loadSrcs = pag.loadInvLookup(varNode);
    if (loadSrcs != null && !loadSrcs.isEmpty()) {
      for (FieldReferenceNode loadSrc : loadSrcs) {
        Set<Node> p2Nodes = loadSrc.getBase().getPointsToSet();
        for (Node p2Node : p2Nodes) {
          AllocationNode allocNode = (AllocationNode) p2Node;
          AllocationDotField anDotField =
              pag.getOrCreateAllocationDotField(allocNode, loadSrc.getField());
          modified =
              varNode.getOrCreatePointsToSet().addAll(anDotField.getPointsToSet()) | modified;
        }
      }
    }

    path.remove(path.size() - 1);
    return modified;
  }

  /** This getter used for test */
  public PointerAssignmentGraph getPag() {
    return this.pag;
  }
}
