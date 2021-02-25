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
import de.upb.swt.soot.core.model.Field;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/** Propagates points-to sets along pointer assignment graph using a worklist. */
public class WorklistPropagator implements Propagator {
  protected final Set<VariableNode> variableNodeWorkList = new HashSet<>();
  private PointerAssignmentGraph pag;

  public WorklistPropagator(PointerAssignmentGraph pag) {
    this.pag = pag;
  }

  @Override
  public void propagate() {
    // handle Allocation Nodes
    handleAllocationNodeSources();

    do {
      handleVariableNodeSources();

      handleStoreSources();

      handleLoadSources();
    } while (!variableNodeWorkList.isEmpty());
  }

  /**
   * Propagates new points-to information of node src to all its successors.
   *
   * <p>adds AllocationNode sources to their targets' points-to sets if a source is added for the
   * first time adds it to worklist
   */
  private void handleAllocationNodeSources() {
    Map<AllocationNode, Set<VariableNode>> allocationEdges = pag.getAllocationEdges();
    for (Map.Entry<AllocationNode, Set<VariableNode>> entry : allocationEdges.entrySet()) {
      AllocationNode source = entry.getKey();
      Set<VariableNode> targets = entry.getValue();
      for (VariableNode target : targets) {
        if (target.getOrCreatePointsToSet().add(source)) {
          variableNodeWorkList.add(target);
        }
      }
    }
  }

  private void handleVariableNodeSources() {
    while (!variableNodeWorkList.isEmpty()) {
      VariableNode source = variableNodeWorkList.iterator().next();
      variableNodeWorkList.remove(source);
      handleVariableNodeSource(source);
    }
  }

  /**
   * Propagates new points-to information of node src to all its successors.
   *
   * @param source
   */
  private void handleVariableNodeSource(final VariableNode source) {
    if (source.getReplacement() != source) {
      throw new RuntimeException("Got bad node " + source + " with rep " + source.getReplacement());
    }

    if (source.getPointsToSet().isEmpty()) {
      return;
    }

    handleSimpleEdges(source);
    handleStoreEdges(source);
    final HashSet<Pair<Node, Node>> storesToPropagate = new HashSet<>();
    final HashSet<Pair<Node, Node>> loadsToPropagate = new HashSet<>();

    // TODO: SPARK_OPTS ofcg

    handleStoresAndLoadsToPropagate(source, storesToPropagate, loadsToPropagate);

    propagateStoresAndLoads(storesToPropagate, loadsToPropagate);
  }

  private void handleSimpleEdges(final VariableNode source) {
    Set<VariableNode> targets = pag.getSimpleEdges().get(source);
    if(targets==null || targets.isEmpty()){
      return;
    }
    for (VariableNode target : targets) {
      if (target.getOrCreatePointsToSet().addAll(source.getPointsToSet())) {
        variableNodeWorkList.add(target);
      }
    }
  }

  private void handleStoreEdges(final VariableNode source) {
    Set<FieldReferenceNode> targets = pag.getStoreEdges().get(source);
    if(targets==null || targets.isEmpty()){
      return;
    }
    for (FieldReferenceNode target : targets) {
      final Field field = target.getField();
      Set<Node> basePointsToSet = target.getBase().getPointsToSet();
      for (Node node : basePointsToSet) {
        AllocationDotField allocDotField =
            pag.getOrCreateAllocationDotField((AllocationNode) node, field);
        allocDotField.getPointsToSet().addAll(source.getPointsToSet());
      }
    }
  }

  private void handleStoresAndLoadsToPropagate(
      final VariableNode source,
      final HashSet<Pair<Node, Node>> storesToPropagate,
      final HashSet<Pair<Node, Node>> loadsToPropagate) {
    for (final FieldReferenceNode fieldRef : source.getAllFieldReferences()) {
      final Field field = fieldRef.getField();

      Set<VariableNode> storeSources = pag.storeInvLookup(fieldRef);
      if (!storeSources.isEmpty()) {
        Set<Node> sourcePointsToSet = source.getPointsToSet();
        for (Node node : sourcePointsToSet) {
          AllocationDotField allocationDotField =
              pag.getOrCreateAllocationDotField((AllocationNode) node, field);
          for (VariableNode element : storeSources) {
            Pair<Node, Node> pair =
                new ImmutablePair<>(element, allocationDotField.getReplacement());
            storesToPropagate.add(pair);
          }
        }
      }

      final Set<VariableNode> loadTargets = pag.loadLookup(fieldRef);
      if (!loadTargets.isEmpty()) {
        Set<Node> sourcePointsToSet = source.getPointsToSet();
        for (Node node : sourcePointsToSet) {
          AllocationDotField allocationDotField =
              pag.getOrCreateAllocationDotField((AllocationNode) node, field);
          if (allocationDotField != null) {
            for (Node element : loadTargets) {
              Pair<Node, Node> pair =
                  new ImmutablePair<>(allocationDotField.getReplacement(), element);
              loadsToPropagate.add(pair);
            }
          }
        }
      }
    }
  }

  private void propagateStoresAndLoads(
      HashSet<Pair<Node, Node>> storesToPropagate, HashSet<Pair<Node, Node>> loadsToPropagate) {
    for (Pair<Node, Node> pair : storesToPropagate) {
      VariableNode storeSource = (VariableNode) pair.getKey();
      AllocationDotField allocationDotField = (AllocationDotField) pair.getValue();
      allocationDotField.getPointsToSet().addAll(storeSource.getPointsToSet());
    }
    for (Pair<Node, Node> pair : loadsToPropagate) {
      AllocationDotField allocationDotField = (AllocationDotField) pair.getKey();
      VariableNode loadTarget = (VariableNode) pair.getValue();
      if (loadTarget.getPointsToSet().addAll(allocationDotField.getPointsToSet())) {
        variableNodeWorkList.add(loadTarget);
      }
    }
  }

  private void handleStoreSources() {
    Map<VariableNode, Set<FieldReferenceNode>> storeEdges = pag.getStoreEdges();
    for (Map.Entry<VariableNode, Set<FieldReferenceNode>> entry : storeEdges.entrySet()) {
      final VariableNode source = entry.getKey();
      Set<FieldReferenceNode> targets = entry.getValue();
      for (FieldReferenceNode target : targets) {
        Set<Node> targetPointsToSet = target.getBase().getPointsToSet();
        for (Node node : targetPointsToSet) {
          AllocationDotField allocationDotField =
              pag.getOrCreateAllocationDotField((AllocationNode) node, target.getField());
          // TODO: SPARK_OPTS ofcg
          allocationDotField.getPointsToSet().addAll(source.getPointsToSet());
        }
      }
    }
  }

  private void handleLoadSources() {
    Set<Pair<Set<Node>, Node>> edgesToPropagate = new HashSet<>();
    Map<FieldReferenceNode, Set<VariableNode>> loadEdges = pag.getLoadEdges();
    for (Map.Entry<FieldReferenceNode, Set<VariableNode>> entry : loadEdges.entrySet()) {
      handleFieldReferenceNode(entry.getKey(), edgesToPropagate);
    }
    handleEdgesToPropagate(edgesToPropagate);
  }

  /** Propagates new points-to information of node src to all its successors. */
  private void handleFieldReferenceNode(
      FieldReferenceNode source, final Set<Pair<Set<Node>, Node>> edgesToPropagate) {
    final Set<VariableNode> loadTargets = pag.getLoadEdges().get(source);
    if (loadTargets == null || loadTargets.isEmpty()) {
      return;
    }
    final Field field = source.getField();

    Set<Node> basePointsToSet = source.getBase().getPointsToSet();
    for (Node node : basePointsToSet) {
      AllocationDotField allocationDotField =
          pag.getOrCreateAllocationDotField((AllocationNode) node, field);
      if (allocationDotField != null) {
        Set<Node> pointsToSet = allocationDotField.getPointsToSet();
        if (!pointsToSet.isEmpty()) {
          for (Node element : loadTargets) {
            Pair<Set<Node>, Node> pair = new ImmutablePair<>(pointsToSet, element);
            edgesToPropagate.add(pair);
          }
        }
      }
    }
  }

  private void handleEdgesToPropagate(Set<Pair<Set<Node>, Node>> edgesToPropagate) {
    Set<Set> nodesToFlush = Collections.newSetFromMap(new IdentityHashMap<>());
    for (Pair<Set<Node>, Node> pair : edgesToPropagate) {
      Set<Node> pointsToSet = pair.getKey();
      VariableNode loadTarget = (VariableNode) pair.getValue();
      if (loadTarget.getPointsToSet().addAll(pointsToSet)) {
        variableNodeWorkList.add(loadTarget);
      }
      nodesToFlush.add(pointsToSet);
    }
    for (Set toFlush : nodesToFlush) {
      // TODO flushNew only in double set
    }
  }
}
