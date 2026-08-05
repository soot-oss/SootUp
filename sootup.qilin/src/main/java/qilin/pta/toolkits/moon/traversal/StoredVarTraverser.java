package qilin.pta.toolkits.moon.traversal;

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

import com.google.common.collect.SetMultimap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import qilin.core.pag.AllocNode;
import qilin.core.pag.ContextField;
import qilin.core.pag.LocalVarNode;
import qilin.core.pag.PAG;
import qilin.core.pag.PagNode;
import qilin.core.pag.SparkField;
import qilin.core.pag.VarNode;
import qilin.pta.toolkits.common.OAG;
import qilin.pta.toolkits.moon.graph.FieldEdge;
import qilin.pta.toolkits.moon.graph.FieldPointsToGraph;
import qilin.pta.toolkits.moon.graph.FlowEdge;
import qilin.pta.toolkits.moon.graph.FlowKind;
import qilin.pta.toolkits.moon.graph.VFG;
import qilin.pta.toolkits.moon.support.MoonDataConstructor;
import qilin.pta.toolkits.moon.support.PtrSetCache;
import qilin.pta.toolkits.moon.support.Util;
import qilin.util.CallDetails;
import qilin.util.Pair;
import sootup.core.jimple.common.Value;
import sootup.core.model.SootMethod;
import sootup.core.types.ArrayType;

public class StoredVarTraverser {

  private final VFG vfgForField;
  private final CallDetails methodCallDetail;
  private final SetMultimap<AllocNode, SootMethod> objToInvokedMethodsOn;
  private final OAG objectAllocationGraphWithArr;
  private final int maxCtxLayer;
  private final Map<AllocNode, Map<AllocNode, Integer>> ctxObjsCache = new ConcurrentHashMap<>();
  private final FieldPointsToGraph fieldPointsToGraph;
  private final Set<LocalVarNode> allocatedVars;

  private final PtrSetCache ptrSetCache;
  private final PAG pag;

  public StoredVarTraverser(MoonDataConstructor.MoonDataStructure moonData, int maxCtxLayer) {
    this.maxCtxLayer = maxCtxLayer;
    this.pag = moonData.pag();
    this.methodCallDetail = pag.getPta().getScene().getCallDetails();
    this.vfgForField = moonData.vfgForField();
    this.objectAllocationGraphWithArr = moonData.oag();
    this.objToInvokedMethodsOn = moonData.objToIvkMtds();
    this.fieldPointsToGraph = moonData.fieldPointsToGraph();
    this.allocatedVars = moonData.allocVars();
    this.ptrSetCache = moonData.ptrSetCache();
  }

  public TraversalResult findSourceOfVarStoredIn(
      AllocNode obj, LocalVarNode varStored, int maxAllocLayerToTrace, SparkField storedField) {
    SootMethod allocatedMethod = obj.getMethod();
    Deque<TraversalStatus> stack = new ArrayDeque<>(); // dfs
    Set<TraversalStatus> visited = new HashSet<>();
    TraversalResult traversalResult = new TraversalResult(maxAllocLayerToTrace, storedField);
    traversalResult.addVisitedMethod(varStored.getMethod());
    int maxAllocatorLayer = getMaxAllocLayer(varStored, obj) - 1;
    if (maxAllocatorLayer == -1 - 1)
      return traversalResult; // -1 means the variable not in methods called by the obj or its ctx
    // objs.
    stack.push(new TraversalStatus(varStored, maxAllocatorLayer, 0));

    if (obj.getType().toString().contains("javax.swing.event.EventListenerList")) {
      // This is a hack for special case of EventListenerList.
      // Specifically, it's a has a lazy-init field, which will be initialized when certain method
      // is called, and this behavior cannot be captured with context-insensitive analysis.
      traversalResult.addMatchedCtxObjsOfParam(objectAllocationGraphWithArr.getPredsOf(obj), 1);
    }

    while (!stack.isEmpty()) {
      TraversalStatus crtTrace = stack.pop();
      if (visited.contains(crtTrace)) continue;
      visited.add(crtTrace);
      PagNode crtPtr = crtTrace.pointer;
      int crtCallStackLevel = crtTrace.callStackLevel;
      if (crtPtr == null || crtCallStackLevel > 100) continue; // skip abnormal call stack.
      if (reachingPTS(crtPtr).isEmpty()) continue;
      int crtAllocatorLevel = crtTrace.allocatorLevel;
      if (crtPtr instanceof LocalVarNode localVarNode
          && localVarNode.getMethod().equals(allocatedMethod)) {
        traversalResult.hasMetParamOfAllocatedMethod();
      }
      if (crtPtr instanceof LocalVarNode && allocatedVars.contains(crtPtr)) {
        Set<AllocNode> pts = reachingPTS(crtPtr);

        for (AllocNode newObj : pts) {
          int allocatorLayer = -1;
          if (newObj.equals(obj) || objectAllocationGraphWithArr.getSuccsOf(obj).contains(newObj)) {
            allocatorLayer = 0;
          } else {
            Map<AllocNode, Integer> ctxObjs = getCtxObjsOf(obj);
            for (AllocNode allocator : ctxObjs.keySet()) {
              if (objectAllocationGraphWithArr.getSuccsOf(allocator).contains(newObj)) {
                allocatorLayer = ctxObjs.get(allocator);
                break;
              }
            }
          }
          if (allocatorLayer != -1) {
            traversalResult.addNewlyAllocObj(newObj, allocatorLayer);
          }
        }
        continue;
      }

      Set<PagNode> predOfParamPassing = new HashSet<>();
      for (FlowEdge inEdge : vfgForField.getPredsOf(crtPtr)) {
        FlowKind flowKind = inEdge.flowKind();
        PagNode predNode = inEdge.source();
        switch (flowKind) {
          case INSTANCE_LOAD, CALL_STORE, FIELD_STORE -> {}
          case STATIC_LOAD, STATIC_STORE -> {
            TraversalStatus newPtrTrace =
                new TraversalStatus(
                    predNode, crtAllocatorLevel, crtCallStackLevel, crtTrace, flowKind);
            if (!visited.contains(newPtrTrace)) {
              stack.push(newPtrTrace);
              if (predNode instanceof LocalVarNode localVarNode) {
                traversalResult.addVisitedMethod(localVarNode.getMethod());
              }
            }
          }
          case RETURN -> {
            if (predNode instanceof LocalVarNode localVarNode
                && localVarNode.getMethod().isStatic()) {
              TraversalStatus newPtrTrace =
                  new TraversalStatus(
                      predNode,
                      crtAllocatorLevel,
                      crtCallStackLevel,
                      crtPtr,
                      crtTrace,
                      FlowKind.STATIC_METHOD_RETURN);
              if (!visited.contains(newPtrTrace)) {
                stack.push(newPtrTrace);
                traversalResult.addVisitedMethod(localVarNode.getMethod());
              }
            }
          }
          case THIS_PASSING -> {
            List<Set<AllocNode>> ctxObjs = collectCtxObj(crtPtr, obj);
            for (int allocatorLayer = 0; allocatorLayer < ctxObjs.size(); allocatorLayer++) {
              Set<AllocNode> ctxObjsOfLayer = ctxObjs.get(allocatorLayer);
              if (ctxObjsOfLayer.isEmpty()) continue;
              if (allocatorLayer == maxAllocLayerToTrace) {
                // match finished.
                if (connectedUnderLimitNumOfField(varStored, ctxObjsOfLayer, obj)
                    != FieldRelation.NONE) {
                  traversalResult.addMatchedCtxObjsByThisAsParam(ctxObjsOfLayer, allocatorLayer);
                }

              } else {
                if (allocatorLayer > 0 && allocatorLayer >= crtAllocatorLevel) {
                  if (connectedUnderLimitNumOfField(varStored, ctxObjsOfLayer, obj)
                      != FieldRelation.NONE) {
                    traversalResult.addMatchedCtxObjsByThisAsParam(ctxObjsOfLayer, allocatorLayer);
                  }
                }
              }
            }
          }
          case CALL_LOAD -> {
            if (checkIfThis(predNode)) {
              Set<PagNode> returnVars = getReturnVarOfThisMethodCall(predNode, crtPtr);
              for (PagNode varReturnFrom : returnVars) {
                TraversalStatus newPtrTrace =
                    new TraversalStatus(
                        varReturnFrom,
                        crtAllocatorLevel,
                        crtCallStackLevel,
                        crtTrace,
                        FlowKind.THIS_METHOD_RETURN);
                if (!visited.contains(newPtrTrace)) {
                  stack.push(newPtrTrace);
                }
              }

            } else {
              if (containComplexFlowForSpecificFlow(crtTrace, FlowKind.CALL_LOAD)) continue;
              TraversalStatus newPtrTrace =
                  new TraversalStatus(
                      predNode, crtAllocatorLevel, crtCallStackLevel, crtTrace, FlowKind.CALL_LOAD);
              if (!visited.contains(newPtrTrace)) {
                stack.push(newPtrTrace);
              }
            }
          }
          case FIELD_LOAD -> {
            if (checkIfThis(predNode)) {
              if (maxCtxLayer == 1
                  && storedField.getType().toString().contains("java.awt.image.SampleModel")) {
                // handle another special case for SampleModel
                if (crtTrace.getVisitedFlowCounter(FlowKind.THIS_FIELD_STORE_AND_LOAD) > 0) {
                  List<Set<AllocNode>> ctxObjs = collectCtxObj(crtPtr, obj);
                  for (int allocatorLayer = 0; allocatorLayer < ctxObjs.size(); allocatorLayer++) {
                    Set<AllocNode> ctxObjsOfLayer = ctxObjs.get(allocatorLayer);
                    if (ctxObjsOfLayer.isEmpty()) continue;
                    if (allocatorLayer == maxAllocLayerToTrace
                        || (allocatorLayer > 0 && allocatorLayer >= crtAllocatorLevel)) {
                      // match finished.
                      if (connectedUnderLimitNumOfField(varStored, ctxObjsOfLayer, obj)
                          != FieldRelation.NONE) {
                        traversalResult.addMatchedCtxObjsOfParam(ctxObjsOfLayer, allocatorLayer);
                      }
                    }
                  }
                }
              } else {
                SparkField labeledField = ((FieldEdge) inEdge).field();
                if (labeledField instanceof qilin.core.pag.Field labeledQField
                    && storedField instanceof qilin.core.pag.Field storedQField) {
                  if (labeledQField
                      .getField()
                      .getDeclaringClassType()
                      .equals(storedQField.getField().getDeclaringClassType())) continue;
                }
              }
              if (containComplexFlowForSpecificFlow(crtTrace, FlowKind.THIS_FIELD_STORE_AND_LOAD))
                continue;
              Set<PagNode> fieldStoredVar = getFieldStoredVar(predNode, crtPtr);
              for (PagNode pointer : fieldStoredVar) {
                TraversalStatus newPtrTrace =
                    new TraversalStatus(
                        pointer,
                        crtAllocatorLevel,
                        crtCallStackLevel,
                        crtTrace,
                        FlowKind.THIS_FIELD_STORE_AND_LOAD);
                newPtrTrace.needCheckFieldRelationWhenMeetNew = false;
                if (!visited.contains(newPtrTrace)) {
                  stack.push(newPtrTrace);
                }
              }

            } else {
              if (containComplexFlowForSpecificFlow(crtTrace, FlowKind.FIELD_LOAD)) continue;
              TraversalStatus newPtrTrace =
                  new TraversalStatus(
                      predNode,
                      crtAllocatorLevel,
                      crtCallStackLevel,
                      crtTrace,
                      FlowKind.FIELD_LOAD);
              if (!visited.contains(newPtrTrace)) {
                stack.push(newPtrTrace);
              }
            }
          }
          case LOCAL_ASSIGN -> {
            TraversalStatus newPtrTrace =
                new TraversalStatus(
                    predNode,
                    crtAllocatorLevel,
                    crtCallStackLevel,
                    crtTrace,
                    FlowKind.LOCAL_ASSIGN);
            if (!visited.contains(newPtrTrace)) {
              stack.push(newPtrTrace);
            }
          }

          case NEW -> {}
          case PARAMETER_PASSING -> {
            // leave for below.
            predOfParamPassing.add(predNode);
          }
          default -> throw new RuntimeException("Unexpected flowKind: " + flowKind);
        }
      }

      if (!predOfParamPassing.isEmpty()) {
        if (getMethodOfPointer(crtPtr).isStatic()) {
          // in static method.
          Set<AllocNode> layerOneCtxObjs = objectAllocationGraphWithArr.getPredsOf(obj);
          if (getMethodOfPointer(crtPtr).equals(obj.getMethod())) {
            if (layerOneCtxObjs.size() > 1 && 1 >= crtAllocatorLevel) {
              FieldRelation fieldRelation =
                  connectedUnderLimitNumOfField(varStored, reachingPTS(crtPtr), obj);
              if (checkFieldRelation(fieldRelation, predOfParamPassing, varStored)) {
                traversalResult.addMatchedCtxObjsOfParam(layerOneCtxObjs, 1);
              }
            }
          } else if (maxAllocLayerToTrace == 2 && crtAllocatorLevel >= 1) {
            FieldRelation fieldRelation =
                connectedUnderLimitNumOfField(varStored, reachingPTS(crtPtr), obj);
            if (checkFieldRelation(fieldRelation, predOfParamPassing, varStored)) {
              traversalResult.addMatchedCtxObjsOfParam(layerOneCtxObjs, 2);
            }
          }

          SootMethod callerOfStatic = crtTrace.getStaticCallStackTop();
          for (PagNode pred : predOfParamPassing) { // static
            if (callerOfStatic != null && !callerOfStatic.equals(getMethodOfPointer(pred)))
              continue; // skip suspicious caller of static method.
            if (getMethodOfPointer(crtPtr).equals(getMethodOfPointer(pred)))
              continue; // skip static recursive call.
            TraversalStatus newPtrTrace =
                new TraversalStatus(
                    pred,
                    crtAllocatorLevel,
                    crtCallStackLevel,
                    null,
                    crtTrace,
                    FlowKind.STATIC_PARAMETER_PASSING);
            traversalResult.addVisitedMethod(getMethodOfPointer(pred));
            if (!visited.contains(newPtrTrace)) {
              stack.push(newPtrTrace);
            }
          }
        } else {
          // non-static
          if (predOfParamPassing.stream().allMatch(pred -> checkIfThisCallOnArg(pred, crtPtr))) {
            // all called by this variable.
            for (PagNode pred : predOfParamPassing) {
              if (falseParamPassingFlow(crtPtr, pred, obj)) continue;
              List<Set<AllocNode>> ctxObjs = collectCtxObj(pred, obj);
              if (ctxObjs.stream().anyMatch(s -> !s.isEmpty())) {
                TraversalStatus newPtrTrace =
                    new TraversalStatus(
                        pred,
                        crtAllocatorLevel,
                        crtCallStackLevel,
                        crtTrace,
                        FlowKind.THIS_PARAM_PASSING);
                if (!visited.contains(newPtrTrace)) {
                  stack.push(newPtrTrace);
                  traversalResult.addVisitedMethod(getMethodOfPointer(pred));
                }
              }
            }
          } else {

            List<Set<AllocNode>> ctxObjs = collectCtxObj(crtPtr, obj);
            for (int allocatorLayer = 0; allocatorLayer < ctxObjs.size(); allocatorLayer++) {
              Set<AllocNode> ctxObjsOfLayer = ctxObjs.get(allocatorLayer);
              if (ctxObjsOfLayer.isEmpty()) continue;
              if (allocatorLayer >= maxAllocLayerToTrace) {
                // match finished.
                FieldRelation fieldRelation =
                    connectedUnderLimitNumOfField(varStored, reachingPTS(crtPtr), obj);
                if (checkFieldRelation(fieldRelation, predOfParamPassing, varStored)) {
                  traversalResult.addMatchedCtxObjsOfParam(ctxObjsOfLayer, allocatorLayer);
                  break;
                }
              } else {
                boolean toBreak = false;
                if (allocatorLayer >= crtAllocatorLevel) {
                  FieldRelation fieldRelation =
                      connectedUnderLimitNumOfField(varStored, reachingPTS(crtPtr), obj);
                  if (checkFieldRelation(fieldRelation, predOfParamPassing, varStored)) {
                    traversalResult.addMatchedCtxObjsOfParam(ctxObjsOfLayer, allocatorLayer);
                    toBreak = true;
                  }
                }
                // continue match more layer.
                for (PagNode pred : predOfParamPassing) {
                  if (falseParamPassingFlow(crtPtr, pred, ctxObjsOfLayer)) continue;
                  TraversalStatus newPtrTrace =
                      new TraversalStatus(
                          pred,
                          allocatorLayer,
                          crtCallStackLevel + 1,
                          crtTrace,
                          FlowKind.PARAMETER_PASSING);
                  if (!visited.contains(newPtrTrace)) {
                    stack.push(newPtrTrace);
                    traversalResult.addVisitedMethod(getMethodOfPointer(pred));
                  }
                }
                if (toBreak) break;
              }
            }
          }
        }
      }
    }

    return traversalResult;
  }

  private boolean checkIfThisCallOnArg(PagNode arg, PagNode param) {
    Set<Value> recvVar =
        methodCallDetail.getRecvValueOfArgAndParam((LocalVarNode) arg, (LocalVarNode) param);
    return recvVar.stream()
        .allMatch(
            v -> {
              LocalVarNode recvVarNode =
                  pag.findLocalVarNode(((LocalVarNode) arg).getMethod(), v, v.getType());
              return checkIfThis(recvVarNode);
            });
  }

  private int getMaxAllocLayer(PagNode crtPtr, AllocNode obj) {
    Map<AllocNode, Integer> ctxs = getCtxObjsOf(obj);
    Set<AllocNode> ctxObjs = ctxs.keySet();
    Collection<Pair<Object, SootMethod>> usageCtxAndCallerPairs =
        methodCallDetail.usageCtxAndCallerOf(getMethodOfPointer(crtPtr));

    Set<AllocNode> matchCtxObjs =
        usageCtxAndCallerPairs.stream()
            .map(p -> (AllocNode) (p.first()))
            .filter(ctxObjs::contains)
            .collect(Collectors.toSet());
    return matchCtxObjs.stream().mapToInt(ctxs::get).min().orElse(-1);
  }

  private List<Set<AllocNode>> collectCtxObj(PagNode crtPtr, AllocNode obj) {
    Map<AllocNode, Integer> ctxs = getCtxObjsOf(obj);
    Set<AllocNode> ctxObjs = ctxs.keySet();
    SootMethod crtMethod = getMethodOfPointer(crtPtr);
    List<Set<AllocNode>> result = new ArrayList<>(this.maxCtxLayer + 1);
    for (int i = 0; i < this.maxCtxLayer + 1; i++) {
      result.add(Collections.emptySet());
    }
    int maxLayer = -1;
    for (AllocNode ctxObj : ctxObjs) {
      if (objToInvokedMethodsOn.containsKey(ctxObj)
          && objToInvokedMethodsOn.get(ctxObj).contains(crtMethod)) {
        int layer = ctxs.get(ctxObj);
        maxLayer = Math.max(layer, maxLayer);
        if (result.get(layer).isEmpty()) {
          result.set(layer, new HashSet<>());
        }
        result.get(layer).add(ctxObj);
      }
    }
    if (maxLayer == -1) return List.of();
    return result;
  }

  private Map<AllocNode, Integer> getCtxObjsOf(AllocNode obj) {
    return ctxObjsCache.computeIfAbsent(
        obj,
        k -> {
          Map<AllocNode, Integer> result = new HashMap<>();
          List<Set<AllocNode>> layerOfCtxObjs = new ArrayList<>();
          layerOfCtxObjs.add(new HashSet<>(Set.of(obj))); // index 0 for current obj.
          layerOfCtxObjs.add(
              new HashSet<>(objectAllocationGraphWithArr.getPredsOf(obj))); // index 1 for first
          // allocators of current obj.

          for (int i = 2; i <= maxCtxLayer; i++) {
            layerOfCtxObjs.add(
                new HashSet<>(
                    layerOfCtxObjs.get(i - 1).stream()
                        .map(objectAllocationGraphWithArr::getPredsOf)
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet())));
          }
          Set<AllocNode> alreadyIn = new HashSet<>();
          for (int i = 0; i < layerOfCtxObjs.size(); i++) {
            Set<AllocNode> layerCtx = layerOfCtxObjs.get(i);
            for (AllocNode ctx : layerCtx) {
              if (alreadyIn.contains(ctx)) continue;
              alreadyIn.add(ctx);
              result.put(ctx, i);
            }
          }
          return result;
        });
  }

  private boolean containComplexFlowForSpecificFlow(TraversalStatus crtTrace, FlowKind flowKind) {
    switch (flowKind) {
      case THIS_FIELD_STORE_AND_LOAD -> {
        int callLoadFreq = crtTrace.getVisitedFlowCounter(FlowKind.CALL_LOAD);
        int unwrappedFlowFreq = crtTrace.getVisitedFlowCounter(FlowKind.FIELD_LOAD);
        int wrappedFlowFreq = crtTrace.getVisitedFlowCounter(FlowKind.FIELD_STORE);
        return callLoadFreq + unwrappedFlowFreq + wrappedFlowFreq > 2;
      }
      case CALL_LOAD -> {
        int callLoadFreq = crtTrace.getVisitedFlowCounter(FlowKind.CALL_LOAD);
        int wrappedFlowFreq = crtTrace.getVisitedFlowCounter(FlowKind.FIELD_STORE);
        int unwrappedFlowFreq = crtTrace.getVisitedFlowCounter(FlowKind.FIELD_LOAD);
        return callLoadFreq + wrappedFlowFreq + unwrappedFlowFreq > 2;
      }
      default -> {}
    }
    return false;
  }

  private Set<PagNode> getReturnVarOfThisMethodCall(PagNode predAkaThis, PagNode crtPtr) {
    Set<AllocNode> thisPts = reachingPTS(predAkaThis);
    Set<PagNode> preds = new HashSet<>();
    for (FlowEdge inEdge : vfgForField.getPredsOf(crtPtr)) {
      if (inEdge.flowKind().equals(FlowKind.RETURN)) {
        SootMethod inMethod = getMethodOfPointer(inEdge.source());
        for (AllocNode thisObj : thisPts) {
          if (objToInvokedMethodsOn.get(thisObj).contains(inMethod)) {
            preds.add(inEdge.source());
            break;
          }
        }
      }
    }
    return preds;
  }

  private Set<PagNode> getFieldStoredVar(PagNode pred, PagNode crtPtr) {
    Set<PagNode> ptrStoredIn = new HashSet<>();
    Set<AllocNode> thisPts = reachingPTS(pred);
    AllocNode thisObj;

    for (FlowEdge edge : vfgForField.getPredsOf(crtPtr)) {
      if (edge.flowKind().equals(FlowKind.INSTANCE_LOAD)) {
        ContextField instanceField = (ContextField) edge.source();

        if (thisPts.contains(instanceField.getBase())
            && !vfgForField.getPredsOf(instanceField).isEmpty()) {
          thisObj = instanceField.getBase();

          for (FlowEdge inEdge : vfgForField.getPredsOf(instanceField)) {
            SootMethod inMethod = getMethodOfPointer(inEdge.source());
            if (objToInvokedMethodsOn.get(thisObj).contains(inMethod)) {
              ptrStoredIn.add(inEdge.source());
            }
          }
        }
      }
    }
    return ptrStoredIn;
  }

  private boolean checkIfThis(PagNode node) {
    LocalVarNode thisVar =
        (LocalVarNode) pag.getMethodPAG(getMethodOfPointer(node)).nodeFactory().caseThis();
    boolean isThis = node.equals(thisVar);
    boolean isLocalAssignedFromThis =
        vfgForField.getPredsOf(node).stream()
            .anyMatch(
                inEdge ->
                    inEdge.source().equals(thisVar)
                        && inEdge.flowKind().equals(FlowKind.LOCAL_ASSIGN));
    return isThis || isLocalAssignedFromThis;
  }

  private boolean falseParamPassingFlow(PagNode current, PagNode pred, Set<AllocNode> objs) {
    Set<Value> recvVars =
        methodCallDetail.getRecvValueOfArgAndParam((LocalVarNode) pred, (LocalVarNode) current);
    return recvVars.stream()
        .noneMatch(
            v -> {
              LocalVarNode recvVarNode =
                  pag.findLocalVarNode(((LocalVarNode) pred).getMethod(), v, v.getType());
              return Util.haveOverlap(objs, reachingPTS(recvVarNode));
            });
  }

  private boolean falseParamPassingFlow(PagNode current, PagNode pred, AllocNode obj) {

    if (objToInvokedMethodsOn.containsKey(obj)
        && objToInvokedMethodsOn.get(obj).contains(getMethodOfPointer(current))) {
      Set<Value> recvVars =
          methodCallDetail.getRecvValueOfArgAndParam((LocalVarNode) pred, (LocalVarNode) current);
      return recvVars.stream()
          .noneMatch(
              v -> {
                LocalVarNode recvVarNode =
                    pag.findLocalVarNode(((LocalVarNode) pred).getMethod(), v, v.getType());
                return reachingPTS(recvVarNode).contains(obj);
              });
    } else {
      SootMethod inMethod = getMethodOfPointer(current);
      if (inMethod.isStatic()) {
        return false;
      }
      VarNode calleeThis = pag.getMethodPAG(inMethod).nodeFactory().caseThis();
      Set<Value> recvVars =
          methodCallDetail.getRecvValueOfArgAndParam((LocalVarNode) pred, (LocalVarNode) current);
      return recvVars.stream()
          .noneMatch(
              v -> {
                LocalVarNode recvVar =
                    pag.findLocalVarNode(((LocalVarNode) pred).getMethod(), v, v.getType());
                return Util.haveOverlap(reachingPTS(calleeThis), reachingPTS(recvVar));
              });
    }
  }

  private boolean checkFieldRelation(
      FieldRelation fieldRelation, Set<PagNode> preds, LocalVarNode varStored) {
    if (fieldRelation == FieldRelation.NONE) return false;
    if (fieldRelation == FieldRelation.SAME) return true;
    if (fieldRelation == FieldRelation.POINT_TO) {

      Set<AllocNode> varStoredPTS = reachingPTS(varStored);
      List<Set<AllocNode>> predsFieldPTS = new ArrayList<>();
      for (PagNode pred : preds) {
        Set<AllocNode> predPts = reachingPTS(pred);
        Set<AllocNode> predFieldPTS =
            new HashSet<>(
                predPts.stream()
                    .map(fieldPointsToGraph::getFieldPointsTo)
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet()));
        predFieldPTS.retainAll(varStoredPTS);
        predsFieldPTS.add(predFieldPTS);
      }
      for (int i = 0; i < predsFieldPTS.size(); i++) {
        for (int j = i + 1; j < predsFieldPTS.size(); j++) {
          if (Collections.disjoint(predsFieldPTS.get(i), predsFieldPTS.get(j))) {
            return true; // Found two sets with different elements
          }
        }
      }
      return false;
    }
    if (fieldRelation == FieldRelation.POINT_FROM) {
      return preds.size() > 1;
    }
    return false;
  }

  private FieldRelation connectedUnderLimitNumOfField(
      PagNode varStoredIn, Set<AllocNode> allocatorDepPTS, AllocNode obj) {
    if (obj.getType() instanceof ArrayType) return FieldRelation.SAME;
    Set<AllocNode> inFlowPTS = reachingPTS(varStoredIn);
    if (Util.haveOverlap(inFlowPTS, allocatorDepPTS)) return FieldRelation.SAME;
    Set<AllocNode> allocFieldPTS =
        allocatorDepPTS.parallelStream()
            .map(fieldPointsToGraph::getFieldPointsTo)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    if (Util.haveOverlap(inFlowPTS, allocFieldPTS)) return FieldRelation.POINT_TO;
    Set<AllocNode> allcFieldFrom =
        allocatorDepPTS.parallelStream()
            .map(fieldPointsToGraph::getFieldPointsFrom)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    if (Util.haveOverlap(inFlowPTS, allcFieldFrom)) return FieldRelation.POINT_FROM;
    return FieldRelation.NONE;
  }

  private Set<AllocNode> reachingPTS(PagNode node) {
    return ptrSetCache.ptsOf(node);
  }

  private SootMethod getMethodOfPointer(PagNode n) {
    if (n instanceof LocalVarNode localVarNode) {
      return localVarNode.getMethod();
    }
    throw new RuntimeException("Unexpected node type: " + n.getClass().getName());
  }

  private enum FieldRelation {
    NONE,
    POINT_TO,
    POINT_FROM,
    SAME
  }
}
