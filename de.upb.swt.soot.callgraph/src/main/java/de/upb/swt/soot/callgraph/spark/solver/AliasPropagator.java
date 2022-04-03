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
import java.util.*;

/**
 * Propagates points-to sets along pointer assignment graph using a relevant aliases.
 *
 * @author Zun Wang
 */
public class AliasPropagator implements Propagator {
  private PointerAssignmentGraph pag;
  private Map<Field, Set<VariableNode>> fieldToBases = new HashMap<>();
  private Map<FieldReferenceNode, Set<FieldReferenceNode>> aliasEdges = new HashMap();

  private final Set<VariableNode> varNodeWorkList = new HashSet<>();
  private Set<VariableNode> aliasWorkList = new HashSet<>();
  private Set<FieldReferenceNode> inFieldRefWorkList = new HashSet<>();
  private Set<FieldReferenceNode> outFieldRefWorkList = new HashSet<>();

  // map contains the node which has new point-to info to propagate
  private HashMap<Node, Set<Node>> nodeToNewPoint2Set = new HashMap<>();

  private Map<FieldReferenceNode, Set<Node>> newloadSets = new HashMap<>();
  private Map<FieldReferenceNode, Set<Node>> oldLoadSets = new HashMap<>();

  // todo: a field OnFlyCallGraph ofcg
  public AliasPropagator(PointerAssignmentGraph pag) {
    this.pag = pag;
  }

  @Override
  public void propagate() {
    // todo: ofcg = pag.getOnFlyCallGraph;
    new TopologicalSorter(pag, false).sort();

    // collect all FieldReferenceNodes' (field, set of bases) pairs
    for (FieldReferenceNode frNode : pag.getLoadEdges().keySet()) {
      if (!fieldToBases.containsKey(frNode.getField())) {
        fieldToBases.put(frNode.getField(), new HashSet<>());
      }
      fieldToBases.get(frNode.getField()).add(frNode.getBase());
    }

    for (FieldReferenceNode frNode : pag.getStoreEdgesInv().keySet()) {
      if (!fieldToBases.containsKey(frNode.getField())) {
        fieldToBases.put(frNode.getField(), new HashSet<>());
      }
      fieldToBases.get(frNode.getField()).add(frNode.getBase());
    }

    // process all allocation nodes
    for (AllocationNode alNode : pag.getAllocationEdges().keySet()) {
      handleAllocationNode(alNode);
    }

    do {
      handleVarNodeWorkList();
      handleAliasWorkList();
      handleFieldRefWorkList();

    } while (!varNodeWorkList.isEmpty());
  }

  protected void handleVarNodeWorkList() {
    aliasWorkList = new HashSet<>();
    while (!varNodeWorkList.isEmpty()) {
      VariableNode source = varNodeWorkList.iterator().next();
      varNodeWorkList.remove(source);
      aliasWorkList.add(source);
      handleVarNode(source);
    }
  }

  /** add alias-edges between p.f and q.f, if p, q are alias */
  protected void handleAliasWorkList() {
    for (VariableNode source : aliasWorkList) {
      for (FieldReferenceNode srcFr : source.getAllFieldReferences()) {
        Field field = srcFr.getField();
        Set<VariableNode> targets = fieldToBases.get(field);
        if (targets != null && !targets.isEmpty()) {
          for (VariableNode target : targets) {
            if (intersect(getFullP2Set(source), getFullP2Set(target))) {
              FieldReferenceNode tgtFr = target.getField(field);
              if (!aliasEdges.containsKey(srcFr)) {
                aliasEdges.put(srcFr, new HashSet<>());
              }
              if (!aliasEdges.containsKey(tgtFr)) {
                aliasEdges.put(tgtFr, new HashSet<>());
              }
              aliasEdges.get(srcFr).add(tgtFr);
              aliasEdges.get(tgtFr).add(srcFr);
              inFieldRefWorkList.add(srcFr);
              inFieldRefWorkList.add(tgtFr);
              if (addNewP2Info(
                  getOrCreateFrNewLoadSet(tgtFr),
                  srcFr.getOrCreatePointsToSet(),
                  getOrCreateFrOldLoadSet(tgtFr))) {
                outFieldRefWorkList.add(tgtFr);
              }
              if (addNewP2Info(
                  getOrCreateFrNewLoadSet(srcFr),
                  tgtFr.getOrCreatePointsToSet(),
                  getOrCreateFrOldLoadSet(srcFr))) {
                outFieldRefWorkList.add(srcFr);
              }
            }
          }
        }
      }
    }
  }

  /** Propagate the load P2Set Info along to alias-edges */
  protected void handleFieldRefWorkList() {
    for (FieldReferenceNode source : inFieldRefWorkList) {
      Set<Node> newInfo = nodeToNewPoint2Set.get(source);
      if (newInfo == null) {
        newInfo = Collections.emptySet();
      }
      Set<FieldReferenceNode> targets = aliasEdges.get(source);
      if (targets != null && !targets.isEmpty()) {
        for (FieldReferenceNode target : aliasEdges.get(source)) {
          if (addNewP2Info(
              getOrCreateFrNewLoadSet(target), newInfo, getOrCreateFrOldLoadSet(target))) {
            outFieldRefWorkList.add(target);
          }
        }
      }
      flushNew(source, newInfo);
    }
    inFieldRefWorkList = new HashSet<>();

    for (FieldReferenceNode source : outFieldRefWorkList) {
      Set<Node> p2Set = getOrCreateFrNewLoadSet(source);
      if (p2Set.isEmpty()) {
        continue;
      }
      Set<VariableNode> targets = pag.loadLookup(source);
      if (targets != null && !targets.isEmpty()) {
        for (VariableNode target : targets) {
          Set<Node> newTargetP2Set = nodeToNewPoint2Set.get(target);
          if (newTargetP2Set == null) {
            newTargetP2Set = new HashSet<>();
            nodeToNewPoint2Set.put(target, newTargetP2Set);
          }
          if (addNewP2Info(newTargetP2Set, p2Set, target.getOrCreatePointsToSet())) {
            varNodeWorkList.add(target);
          }
        }
      }
      flushNewLoad(source);
    }
    outFieldRefWorkList = new HashSet<>();
  }

  /** Propagates new points-to information of AllocationNode source to all its successors. */
  protected void handleAllocationNode(AllocationNode source) {

    Set<VariableNode> targets = pag.allocLookup(source);
    if(targets != null && !targets.isEmpty()){
      for (VariableNode target : targets) {
        if (!nodeToNewPoint2Set.containsKey(target)) {
          nodeToNewPoint2Set.put(target, new HashSet<>());
        }
        Set<Node> oldP2Set = target.getOrCreatePointsToSet();
        Set<Node> newP2Set = nodeToNewPoint2Set.get(target);
        if ((!oldP2Set.contains(source)) && (!newP2Set.contains(source))) {
          newP2Set.add(source);
          varNodeWorkList.add(target);
        }
      }
    }
  }
  /**
   * Propagates new points-to information of VariableNode source to all its variable node successors
   */
  protected void handleVarNode(VariableNode source) {
    if (source.getReplacement() != source) {
      throw new RuntimeException(
          "The variableNode "
              + source
              + " has been merged to another variable node "
              + source.getReplacement());
    }
    Set<Node> newP2SetOfSource = nodeToNewPoint2Set.get(source);
    // There's no new points-to info waiting for propagation.
    if (newP2SetOfSource.isEmpty() || newP2SetOfSource == null) {
      return;
    }

    // Todo: Lack of OnFlyCallGraph Part

    Set<VariableNode> varTargets = pag.simpleLookup(source);
    if (varTargets != null && !varTargets.isEmpty()) {
      for (VariableNode varTarget : varTargets) {
        Set<Node> oldP2Set = varTarget.getOrCreatePointsToSet();
        Set<Node> newP2Set = nodeToNewPoint2Set.get(varTarget);
        if (newP2Set == null) {
          newP2Set = new HashSet<>();
          nodeToNewPoint2Set.put(varTarget, newP2Set);
        }
        if (addNewP2Info(newP2Set, newP2SetOfSource, oldP2Set)) {
          varNodeWorkList.add(varTarget);
        }
      }
    }

    Set<FieldReferenceNode> fieldTargets = pag.storeLookup(source);
    if (fieldTargets != null && !fieldTargets.isEmpty()) {
      for (FieldReferenceNode fieldTarget : fieldTargets) {
        Set<Node> oldP2Set = fieldTarget.getOrCreatePointsToSet();
        Set<Node> newP2Set = nodeToNewPoint2Set.get(fieldTarget);
        if (newP2Set == null) {
          newP2Set = new HashSet<>();
          nodeToNewPoint2Set.put(fieldTarget, newP2Set);
        }
        if (addNewP2Info(newP2Set, newP2SetOfSource, oldP2Set)) {
          inFieldRefWorkList.add(fieldTarget);
        }
      }
    }
    flushNew(source, newP2SetOfSource);
  }

  private boolean intersect(Set<Node> set1, Set<Node> set2) {
    Set<Node> intersection = new HashSet(set1);
    intersection.retainAll(set2);
    return intersection.size() > 0;
  }

  /** Add all new point to Info into its P2Set (after propagation), and flush new P2-info */
  private void flushNew(Node node, Set<Node> newP2Set) {
    if (newP2Set.isEmpty()) {
      return;
    }
    Set<Node> p2Set = node.getOrCreatePointsToSet();
    p2Set.addAll(newP2Set);
    nodeToNewPoint2Set.get(node).clear();
  }

  /**
   * newP2Set = newP2Set U (newP2Info\OldP2Set)
   *
   * @return true if newP2Set is changed, else false;
   */
  private boolean addNewP2Info(Set<Node> newP2Set, Set<Node> newP2Info, Set<Node> oldP2Set) {
    boolean ret = false;
    Set<Node> diffSet = new HashSet<>(newP2Info);
    diffSet.removeAll(oldP2Set);
    if (!diffSet.isEmpty()) {
      ret = true;
      newP2Set.addAll(diffSet);
    }
    return ret;
  }

  private Set<Node> getFullP2Set(Node node) {
    Set<Node> fullP2Set = new HashSet<>(node.getOrCreatePointsToSet());
    Set<Node> newP2Set = nodeToNewPoint2Set.get(node);
    if (newP2Set != null && !newP2Set.isEmpty()) {
      fullP2Set.addAll(newP2Set);
    }
    return fullP2Set;
  }

  private Set<Node> getOrCreateFrNewLoadSet(FieldReferenceNode frN) {
    Set<Node> p2S = newloadSets.get(frN);
    if (p2S == null) {
      p2S = new HashSet<>();
      newloadSets.put(frN, p2S);
    }
    return p2S;
  }

  private Set<Node> getOrCreateFrOldLoadSet(FieldReferenceNode frN) {
    Set<Node> p2S = oldLoadSets.get(frN);
    if (p2S == null) {
      p2S = new HashSet<>();
      oldLoadSets.put(frN, p2S);
    }
    return p2S;
  }

  /** Move new point to Info from newLoadSet to oldLoadSet(after propagation) */
  private void flushNewLoad(FieldReferenceNode node) {
    Set<Node> newP2Set = newloadSets.get(node);
    if (newP2Set == null && newP2Set.isEmpty()) {
      return;
    }
    Set<Node> oldP2Set = oldLoadSets.get(node);
    if (newP2Set == null) {
      oldP2Set = new HashSet<>();
      oldLoadSets.put(node, oldP2Set);
    }
    oldP2Set.addAll(newP2Set);
    newP2Set.clear();
  }

  /** This getter used for test*/
  public PointerAssignmentGraph getPag() {
    return this.pag;
  }
}
