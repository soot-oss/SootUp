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

import com.google.common.collect.SetMultimap;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import qilin.core.pag.AllocNode;
import qilin.core.pag.LocalVarNode;
import qilin.core.pag.PAG;
import qilin.core.pag.PagNode;
import qilin.core.pag.SparkField;
import qilin.core.pag.VarNode;
import qilin.pta.toolkits.moon.graph.FieldEdge;
import qilin.pta.toolkits.moon.graph.FlowEdge;
import qilin.pta.toolkits.moon.graph.FlowKind;
import qilin.pta.toolkits.moon.graph.VFG;
import qilin.pta.toolkits.moon.support.FieldFlowRecorder;
import qilin.pta.toolkits.moon.support.KeyTypeCollector;
import qilin.pta.toolkits.moon.support.MoonDataConstructor;
import sootup.core.model.SootMethod;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

public class PartialChecker {
  private static final int fieldTypeThreshold = 3;
  private final VFG vfgForObj;
  private final PAG pag;
  private final KeyTypeCollector keyTypeCollector;
  private final FieldFlowRecorder fieldFlowRecorder;
  private final SetMultimap<SootMethod, AllocNode> methodToInvokeObjs;
  private final Set<AllocNode> objMetParamOfAllocMethod = new HashSet<>();

  public PartialChecker(MoonDataConstructor.MoonDataStructure graphBuilder) {
    this.keyTypeCollector = graphBuilder.keyTypeCollector();
    this.fieldFlowRecorder = graphBuilder.fieldFlowRecorder();
    this.vfgForObj = graphBuilder.vfgForObj();
    this.methodToInvokeObjs = graphBuilder.mthToRecvObj();
    this.pag = graphBuilder.pag();
  }

  public boolean addMetParamObj(AllocNode obj) {
    return objMetParamOfAllocMethod.add(obj);
  }

  public boolean check(AllocNode obj) {
    SootMethod inMethod = obj.getMethod();
    if (inMethod == null) return false;

    if (canReturnOut(obj)) {
      if (objMetParamOfAllocMethod.contains(obj) || inMethod.isStatic()) {
        return true;
      }
    }

    VarNode thisVar = pag.getMethodPAG(inMethod).nodeFactory().caseThis();
    Set<SootMethod> traversalMethods = new HashSet<>();
    Set<PagNode> thisAlias = getAliasOf(thisVar, traversalMethods);
    Deque<CheckTrace> stack = new ArrayDeque<>();
    Set<CheckTrace> visited = new HashSet<>();
    stack.push(new CheckTrace(obj, CheckStatus.Allocation));

    Set<Type> fieldTypes = new HashSet<>();
    while (!stack.isEmpty()) {
      CheckTrace crtTrace = stack.pop();
      if (visited.contains(crtTrace)) {
        continue;
      }
      visited.add(crtTrace);
      PagNode crtNode = crtTrace.getNode();
      CheckStatus crtState = crtTrace.getState();

      for (FlowEdge outEdge : vfgForObj.getSuccsOf(crtNode)) {
        FlowKind flowKind = outEdge.flowKind();
        CheckStatus nextState = moveToNode(crtState, flowKind, true);
        if (nextState == CheckStatus.UnDef) continue;
        if (flowKind == FlowKind.FIELD_STORE && thisAlias.contains(outEdge.target())) {
          FieldEdge fieldEdge = (FieldEdge) outEdge;
          fieldTypes.add(fieldEdge.field().getType());
          if (isContextAwareField(inMethod, fieldEdge.field())) {
            return fieldTypes.size() <= fieldTypeThreshold;
          }
        } else {
          if (flowKind != FlowKind.FIELD_STORE
              || keyTypeCollector.isConcernedType(((FieldEdge) outEdge).field().getType())) {
            if (outEdge.target() instanceof LocalVarNode localVarNode) {
              if (traversalMethods.contains(localVarNode.getMethod())) {
                if (outEdge instanceof FieldEdge && flowKind == FlowKind.FIELD_STORE) {
                  fieldTypes.add(((FieldEdge) outEdge).field().getType());
                }
                stack.push(new CheckTrace(localVarNode, nextState));
              }
            } else {
              stack.push(new CheckTrace(outEdge.target(), nextState));
            }
          }
        }
      }
      for (FlowEdge inEdge : vfgForObj.getPredsOf(crtNode)) {
        FlowKind flowKind = inEdge.flowKind();
        CheckStatus nextState = moveToNode(crtState, flowKind, false);
        if (nextState == CheckStatus.UnDef) continue;
        if (flowKind == FlowKind.FIELD_LOAD && thisAlias.contains(inEdge.source())) {
          FieldEdge fieldEdge = (FieldEdge) inEdge;
          fieldTypes.add(fieldEdge.field().getType());
          if (isContextAwareField(inMethod, fieldEdge.field())) {
            return fieldTypes.size() <= fieldTypeThreshold;
          }
        } else {
          if (flowKind != FlowKind.FIELD_LOAD
              || keyTypeCollector.isConcernedType(((FieldEdge) inEdge).field().getType())) {
            if (inEdge.source() instanceof LocalVarNode localVarNode) {
              if (traversalMethods.contains(localVarNode.getMethod())) {
                if (inEdge instanceof FieldEdge && flowKind == FlowKind.FIELD_LOAD) {
                  fieldTypes.add(((FieldEdge) inEdge).field().getType());
                }
                stack.push(new CheckTrace(localVarNode, nextState));
              }
            } else {
              stack.push(new CheckTrace(inEdge.source(), nextState));
            }
          }
        }
      }
    }
    return false;
  }

  private boolean isContextAwareField(SootMethod method, SparkField field) {
    if (!keyTypeCollector.isConcernedType(field.getType())) return false;
    Set<AllocNode> recvObjs = methodToInvokeObjs.get(method);
    for (AllocNode recvObj : recvObjs) {
      if (recvObj.getType() instanceof ClassType) {
        if (fieldFlowRecorder.isConnceredField(recvObj, field)) return true;
      }
    }
    return false;
  }

  private CheckStatus moveToNode(CheckStatus crtState, FlowKind flowKind, boolean forward) {
    switch (crtState) {
      case Allocation -> {
        if (forward && flowKind == FlowKind.NEW) {
          return CheckStatus.DirectVar;
        }
      }
      case DirectVar -> {
        if (forward && flowKind == FlowKind.LOCAL_ASSIGN) {
          return CheckStatus.DirectVar;
        } else if (forward && flowKind == FlowKind.FIELD_STORE) {
          return CheckStatus.StoredInVar;
        }
      }
      case StoredInVar -> {
        if (forward) {
          // Forward
          if (flowKind == FlowKind.FIELD_STORE) {
            return CheckStatus.StoredInVar;
          } else if (flowKind == FlowKind.LOCAL_ASSIGN) {
            return CheckStatus.StoredInVar;
          }
        } else {
          // backForward
          if (flowKind == FlowKind.LOCAL_ASSIGN) {
            return CheckStatus.StoredInVar;
          } else if (flowKind == FlowKind.FIELD_LOAD) {
            return CheckStatus.StoredInVar;
          }
        }
      }
      default -> {}
    }
    return CheckStatus.UnDef;
  }

  private boolean canReturnOut(AllocNode obj) {
    Stack<PagNode> stack = new Stack<>();
    for (FlowEdge edge : vfgForObj.getSuccsOf(obj)) {
      stack.push(edge.target());
    }
    for (FlowEdge edge : vfgForObj.getPredsOf(obj)) {
      stack.push(edge.source());
    }
    Set<PagNode> visited = new HashSet<>();
    while (!stack.isEmpty()) {
      PagNode crtNode = stack.pop();
      visited.add(crtNode);
      for (FlowEdge outEdge : vfgForObj.getSuccsOf(crtNode)) {
        if (outEdge.target() instanceof LocalVarNode localVarNode
            && localVarNode.isReturn()
            && localVarNode.getMethod().equals(obj.getMethod())) {
          return true;
        }
        if (outEdge.flowKind() == FlowKind.LOCAL_ASSIGN && !visited.contains(outEdge.target())) {
          stack.push(outEdge.target());
        }
      }
    }
    return false;
  }

  private Set<PagNode> getAliasOf(PagNode node, Set<SootMethod> traversalMethods) {
    Stack<PagNode> stack = new Stack<>();
    for (FlowEdge edge : vfgForObj.getSuccsOf(node)) {
      stack.push(edge.target());
    }
    for (FlowEdge inEdge : vfgForObj.getPredsOf(node)) {
      stack.push(inEdge.source());
    }
    Set<PagNode> visited = new HashSet<>();
    while (!stack.isEmpty()) {
      PagNode crtNode = stack.pop();
      visited.add(crtNode);
      if (crtNode instanceof LocalVarNode localVarNode) {
        SootMethod method = localVarNode.getMethod();
        if (method != null) {
          traversalMethods.add(method);
        }
      }
      for (FlowEdge outEdge : vfgForObj.getSuccsOf(crtNode)) {
        if (outEdge.flowKind() == FlowKind.LOCAL_ASSIGN && !visited.contains(outEdge.target())) {
          stack.push(outEdge.target());
        }
      }
    }
    return visited;
  }
}
