package qilin.pta.toolkits.moon.objcollection;

/*-
 * #%L
 * SootUp - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt and others
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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import qilin.core.pag.AllocNode;
import qilin.core.pag.SparkField;
import qilin.pta.toolkits.moon.Moon;
import qilin.pta.toolkits.moon.graph.LabeledGraph;
import qilin.pta.toolkits.moon.support.MoonDataConstructor;
import qilin.pta.toolkits.moon.support.Util;
import qilin.pta.toolkits.moon.traversal.TraversalResult;
import qilin.util.collect.twokeymultimap.TwoKeyMultiHashMap;
import sootup.core.model.SootMethod;
import sootup.core.types.ArrayType;

public class ObjCollector {
  private final int maxCtxLayer;
  private final MoonDataConstructor.MoonDataStructure moonData;
  private final PartialChecker partialChecker;

  public ObjCollector(int maxCtxLayer, MoonDataConstructor.MoonDataStructure moonData) {
    this.maxCtxLayer = maxCtxLayer;
    this.moonData = moonData;
    this.partialChecker = new PartialChecker(moonData);
  }

  public Set<AllocNode> analyze(List<SetMultimap<AllocNode, TraversalResult>> traversalResults) {

    SetMultimap<AllocNode, SparkField> basePRObjToFields = HashMultimap.create();

    Set<AllocNode> potentialPRObjs = new HashSet<>();
    TwoKeyMultiHashMap<AllocNode, SparkField, SootMethod> storedFToReachMtds =
        new TwoKeyMultiHashMap<>();

    collectbasePRObjs(traversalResults, basePRObjToFields, potentialPRObjs, storedFToReachMtds);
    var prObjDepGraph = buildPRObjDepGraph(traversalResults, basePRObjToFields, potentialPRObjs);
    var recurPRObjToFields = collectRecurPRObjs(traversalResults, basePRObjToFields, prObjDepGraph);

    Set<AllocNode> prObjs = new HashSet<>();
    prObjs.addAll(basePRObjToFields.keySet());
    prObjs.addAll(recurPRObjToFields.keySet());
    int oldSize, newSize;
    while (true) {
      oldSize = prObjs.size();
      var redundantObjs = collectRedundantObjs(prObjs, prObjDepGraph);
      prObjs.removeAll(redundantObjs);
      redundantObjs.forEach(
          o -> {
            basePRObjToFields.removeAll(o);
            recurPRObjToFields.removeAll(o);
          });
      newSize = prObjs.size();
      if (oldSize == newSize) {
        break;
      }
    }

    System.out.println("[ObjCollector] Collected " + prObjs.size() + " PR objects.");
    System.out.println("[ObjCollector] Base PR objects: " + basePRObjToFields.keySet().size());
    System.out.println(
        "[ObjCollector] Recursive PR objects: " + recurPRObjToFields.keySet().size());
    return prObjs;
  }

  private Set<AllocNode> collectRedundantObjs(
      Set<AllocNode> prObjs, LabeledGraph<AllocNode, SparkField> containerGraph) {
    var fieldPointsToGraph = moonData.fieldPointsToGraph();
    Set<AllocNode> toRemoved = new HashSet<>();
    for (AllocNode matchedObj : prObjs) {
      if (matchedObj.getType() instanceof ArrayType) {
        // array obj.
        Set<AllocNode> pts = fieldPointsToGraph.getFieldPointsTo(matchedObj);
        if (pts.size() <= 1 && pts.stream().noneMatch(prObjs::contains)) {
          toRemoved.add(matchedObj);
        }
      } else {
        // class obj.
        boolean isRemoved =
            fieldPointsToGraph.getAllFieldsOf(matchedObj).stream()
                .allMatch(
                    field -> {
                      Set<AllocNode> pts = fieldPointsToGraph.getFieldPointsTo(matchedObj, field);
                      return pts.size() <= 1 && pts.stream().noneMatch(prObjs::contains);
                    });
        if (isRemoved) {
          toRemoved.add(matchedObj);
          containerGraph
              .getOutEdgesOf(matchedObj)
              .forEach(
                  edge -> {
                    AllocNode to = edge.target();
                    toRemoved.add(to);
                  });
        }
      }
    }
    return toRemoved;
  }

  private SetMultimap<AllocNode, SparkField> collectRecurPRObjs(
      List<SetMultimap<AllocNode, TraversalResult>> traversalResults,
      SetMultimap<AllocNode, SparkField> basePRObjToFields,
      LabeledGraph<AllocNode, SparkField> prObjDepGraph) {
    if (Moon.enableRecursivePRObjs) {
      SetMultimap<AllocNode, SparkField> recurPRObjToFields = HashMultimap.create();

      var wrapperObjToFields =
          collectWrapperContainerPRObjs(prObjDepGraph, basePRObjToFields.keySet());
      recurPRObjToFields.putAll(wrapperObjToFields);
      if (maxCtxLayer == 2) {
        var allocatorObjToFields =
            collectAllocatorContainersFor3obj(basePRObjToFields.keySet(), traversalResults);
        var wrapperOfAllocObjToFields =
            collectWrapperContainerPRObjs(prObjDepGraph, allocatorObjToFields.keySet());
        recurPRObjToFields.putAll(allocatorObjToFields);
        recurPRObjToFields.putAll(wrapperOfAllocObjToFields);
      }
      return recurPRObjToFields;
    } else {
      return HashMultimap.create();
    }
  }

  private SetMultimap<AllocNode, SparkField> collectAllocatorContainersFor3obj(
      Set<AllocNode> basePRObjs, List<SetMultimap<AllocNode, TraversalResult>> traversalResults) {
    SetMultimap<AllocNode, SparkField> allocPRObjToFields = HashMultimap.create();
    for (AllocNode basePRObj : basePRObjs) {
      for (SetMultimap<AllocNode, TraversalResult> traversalResult : traversalResults) {
        for (TraversalResult resultOfVarTrace : traversalResult.get(basePRObj)) {
          Set<AllocNode> firstLayerCtxObjs =
              new HashSet<>(resultOfVarTrace.getMatchedCtxObjsOfParam(1));
          Set<AllocNode> secondLayerCtxObjs =
              new HashSet<>(resultOfVarTrace.getMatchedCtxObjsOfParam(2));
          if (firstLayerCtxObjs.isEmpty() || secondLayerCtxObjs.isEmpty()) {
            continue;
          }
          for (AllocNode firstLayerCtxObj : firstLayerCtxObjs) {
            Set<AllocNode> allocatorsOfFirstLayerCtxObjs =
                moonData.oag().getPredsOf(firstLayerCtxObj);
            if ((firstLayerCtxObj.getMethod() != null && firstLayerCtxObj.getMethod().isStatic())
                || Util.haveOverlap(allocatorsOfFirstLayerCtxObjs, secondLayerCtxObjs)) {
              if (partialChecker.check(firstLayerCtxObj)) {
                allocPRObjToFields.put(firstLayerCtxObj, resultOfVarTrace.getField());
              }
            }
          }
        }
      }
    }
    return allocPRObjToFields;
  }

  private SetMultimap<AllocNode, SparkField> collectWrapperContainerPRObjs(
      LabeledGraph<AllocNode, SparkField> containerGraph, Set<AllocNode> basePRObjs) {
    SetMultimap<AllocNode, SparkField> wrapperObjToFields = HashMultimap.create();
    Set<AllocNode> nodes = new HashSet<>(containerGraph.getNodes());
    nodes.retainAll(basePRObjs);
    Queue<AllocNode> queue = new ArrayDeque<>(nodes);
    int size = queue.size();
    Queue<Integer> depthQueue = new ArrayDeque<>(Collections.nCopies(size, 0));
    Set<AllocNode> visited = new HashSet<>();
    while (!queue.isEmpty()) {
      AllocNode current = queue.remove();
      int depth = depthQueue.element();
      if (!visited.contains(current)) {
        visited.add(current);
        if (depth < maxCtxLayer) {
          // Push unvisited successors onto the queue
          for (LabeledGraph<AllocNode, SparkField>.LabelEdge outEdge :
              containerGraph.getOutEdgesOf(current)) {
            AllocNode neighbor = outEdge.target();
            if (!visited.contains(neighbor)
                && !basePRObjs.contains(neighbor)
                && !wrapperObjToFields.containsEntry(neighbor, outEdge.label())) {
              queue.add(neighbor);
              wrapperObjToFields.put(neighbor, outEdge.label());
              depthQueue.add(depth + 1);
            }
          }
        }
      }
    }
    return wrapperObjToFields;
  }

  private LabeledGraph<AllocNode, SparkField> buildPRObjDepGraph(
      List<SetMultimap<AllocNode, TraversalResult>> traversalResults,
      SetMultimap<AllocNode, SparkField> basePRObjToFields,
      Set<AllocNode> potentialPRObjs) {
    var oag = moonData.oag();
    LabeledGraph<AllocNode, SparkField> objDepGraph = new LabeledGraph<>();
    for (AllocNode nonInnerContainerObj : potentialPRObjs) {

      for (SetMultimap<AllocNode, TraversalResult> traversalResult : traversalResults) {
        for (TraversalResult resultOfVarTrace : traversalResult.get(nonInnerContainerObj)) {
          for (int layer = 0; layer < resultOfVarTrace.getRecordSize(); layer++) {
            if (layer > maxCtxLayer) break;
            Set<AllocNode> newlyAllocObjs = resultOfVarTrace.getNewlyAllocObjs(layer);
            for (AllocNode newlyAllocObj : newlyAllocObjs) {
              if (newlyAllocObj.equals(nonInnerContainerObj)) continue;
              Set<AllocNode> allocatorOfNewlyAlloc = oag.getPredsOf(newlyAllocObj);
              Set<AllocNode> allocatorOfNonInner = oag.getPredsOf(nonInnerContainerObj);
              if (Util.haveOverlap(allocatorOfNonInner, allocatorOfNewlyAlloc)
                  && !allocatorOfNewlyAlloc.contains(nonInnerContainerObj)) {
                objDepGraph.addEdge(
                    newlyAllocObj, nonInnerContainerObj, resultOfVarTrace.getField());
              }
            }
          }
        }
      }
    }
    return LabeledGraph.unmodifiableGraph(objDepGraph);
  }

  private void collectbasePRObjs(
      List<SetMultimap<AllocNode, TraversalResult>> traversalResults,
      SetMultimap<AllocNode, SparkField> basePRObjToFields,
      Set<AllocNode> potentialPRObjs,
      TwoKeyMultiHashMap<AllocNode, SparkField, SootMethod> storedFieldToExistingMethods) {
    for (int idx = 0; idx < traversalResults.size(); idx++) {
      int checkLayer = idx + 1;
      for (AllocNode obj : traversalResults.get(idx).keySet()) {
        for (TraversalResult traversalResult : traversalResults.get(idx).get(obj)) {
          if (!moonData.containers().contains(obj)) {
            continue;
          }
          if (traversalResult.isMetParamOfAllocatedMethod()) {
            partialChecker.addMetParamObj(obj);
          }
          var field = traversalResult.getField();
          if (traversalResult.hasCtxObjsOnLayerOf(checkLayer)) {
            basePRObjToFields.put(obj, field);
            traversalResult
                .getVisitedMethods()
                .forEach(m -> storedFieldToExistingMethods.put(obj, field, m));
          } else if (Moon.enableRecursivePRObjs && traversalResult.hasNewlyAllocObjs()) {
            potentialPRObjs.add(obj);
            traversalResult
                .getVisitedMethods()
                .forEach(m -> storedFieldToExistingMethods.put(obj, field, m));
          }
        }
      }
    }

    var removedObj =
        basePRObjToFields.keySet().stream().filter(obj -> !partialChecker.check(obj)).toList();
    removedObj.forEach(basePRObjToFields::removeAll);
    potentialPRObjs.removeIf(
        obj -> !partialChecker.check(obj) || basePRObjToFields.containsKey(obj));
  }
}
