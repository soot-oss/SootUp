package qilin.pta.toolkits.moon.support;

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
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import qilin.core.PTA;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.AllocNode;
import qilin.core.pag.LocalVarNode;
import qilin.core.pag.PagNode;
import qilin.core.pag.SparkField;
import qilin.core.pag.VarNode;
import qilin.pta.toolkits.moon.graph.FieldEdge;
import qilin.pta.toolkits.moon.graph.FlowEdge;
import qilin.pta.toolkits.moon.graph.FlowKind;
import qilin.pta.toolkits.moon.graph.VFG;
import qilin.util.JavaTypes;
import qilin.util.Pair;
import qilin.util.collect.twokeymultimap.ConcurrentTwoKeyMultiMap;
import sootup.core.model.SootMethod;
import sootup.core.types.ReferenceType;

public class FieldFlowRecorder {

  protected final SetMultimap<SparkField, LocalVarNode> fieldToInParams =
      Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet);
  protected final SetMultimap<SparkField, LocalVarNode> fieldToOutParams =
      Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet);

  protected final ConcurrentTwoKeyMultiMap<AllocNode, SparkField, VarNode> objToNonThisFieldStore =
      new ConcurrentTwoKeyMultiMap<>();
  protected final ConcurrentTwoKeyMultiMap<AllocNode, SparkField, VarNode> objToNonThisFieldLoad =
      new ConcurrentTwoKeyMultiMap<>();
  protected final Set<SparkField> hasNonThisFieldLoad = ConcurrentHashMap.newKeySet();
  protected final SetMultimap<AllocNode, LocalVarNode> objToArgOfInvokeMethods =
      Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet);
  private final FieldRecorder fieldRecorder;
  private final VFG vfgForHeap;
  private final PTA pta;
  private final SetMultimap<AllocNode, SootMethod> objToInvokeMethods;

  public FieldFlowRecorder(
      PTA pta,
      FieldRecorder fieldRecorder,
      VFG vfgForHeap,
      SetMultimap<AllocNode, SootMethod> objToInvokeMethods) {
    this.pta = pta;
    this.fieldRecorder = fieldRecorder;
    this.vfgForHeap = vfgForHeap;
    this.objToInvokeMethods = objToInvokeMethods;
  }

  public void build(KeyTypeCollector openTypeCollector) {

    fieldRecorder.allFields(openTypeCollector).parallelStream()
        .forEach(
            field -> {
              boolean needInCheck = false;
              Set<LocalVarNode> retOrParams = traversal(field, false);
              if (!retOrParams.isEmpty()) {
                needInCheck = true;
                fieldToOutParams.putAll(field, retOrParams);
              }
              needInCheck |= hasNonThisFieldLoad.contains(field);
              if (needInCheck) {
                Set<LocalVarNode> paramsOrThis = traversal(field, true);
                if (!paramsOrThis.isEmpty()) {
                  fieldToInParams.putAll(field, paramsOrThis);
                }
              }
            });

    objToInvokeMethods.keySet().parallelStream()
        .forEach(
            obj -> {
              Set<SootMethod> invokeMethods = objToInvokeMethods.get(obj);
              invokeMethods.forEach(
                  m -> {
                    MethodNodeFactory factory = pta.getPag().getMethodPAG(m).nodeFactory();
                    for (int i = 0; i < m.getParameterCount(); ++i) {
                      if (m.getParameterType(i) instanceof ReferenceType
                          && !JavaTypes.isPrimitiveArrayType(m.getParameterType(i))) {
                        LocalVarNode param = (LocalVarNode) factory.caseParm(i);
                        objToArgOfInvokeMethods.put(obj, param);
                      }
                    }
                    objToArgOfInvokeMethods.put(obj, (LocalVarNode) factory.caseThis());
                  });
            });
  }

  private boolean hasInflow(AllocNode heap, SparkField field) {
    if (objToNonThisFieldStore.containsKey(heap, field)) return true;
    return Util.haveOverlap(fieldToInParams.get(field), objToArgOfInvokeMethods.get(heap));
  }

  private boolean hasOutflow(AllocNode heap, SparkField field) {
    if (objToNonThisFieldLoad.containsKey(heap, field)) return true;
    Set<SootMethod> invokedMethods = objToInvokeMethods.get(heap);
    return fieldToOutParams.get(field).stream()
        .anyMatch(v -> invokedMethods.contains(v.getMethod()));
  }

  public boolean isConnceredField(AllocNode heap, SparkField field) {
    return hasInflow(heap, field) && hasOutflow(heap, field);
  }

  private Set<LocalVarNode> traversal(SparkField field, boolean isInflow) {
    Set<LocalVarNode> ret = new HashSet<>();
    SetMultimap<Traversal, PagNode> state2nodes = HashMultimap.create();
    Stack<Pair<PagNode, Traversal>> stack = new Stack<>();

    Set<SootMethod> inMethods =
        fieldRecorder.fieldToBaseObjs.get(field).stream()
            .map(objToInvokeMethods::get)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

    vfgForHeap.getThisVars().stream()
        .filter(
            thisVar -> {
              SootMethod method = thisVar.getMethod();
              return inMethods.contains(method);
            })
        .forEach(thisVar -> stack.push(new Pair<>(thisVar, Traversal.ThisAlias)));

    while (!stack.isEmpty()) {
      Pair<PagNode, Traversal> front = stack.pop();
      if (front.second() == Traversal.Finish) {
        if (front.first() instanceof LocalVarNode lvn) {
          ret.add(lvn);
        }
      }
      state2nodes.put(front.second(), front.first());
      Set<Pair<PagNode, Traversal>> nexts = move(front, field, isInflow);
      for (Pair<PagNode, Traversal> nodeState : nexts) {
        if (!state2nodes.get(nodeState.second()).contains(nodeState.first())) {
          stack.add(nodeState);
        }
      }
    }
    return ret;
  }

  private Set<Pair<PagNode, Traversal>> move(
      Pair<PagNode, Traversal> nodeState, SparkField field, boolean in) {
    PagNode node = nodeState.first();
    Traversal state = nodeState.second();
    Set<Pair<PagNode, Traversal>> ret = new HashSet<>();

    for (FlowEdge outEdge : vfgForHeap.getSuccsOf(node)) {
      boolean metTargetField = false;
      if (outEdge instanceof FieldEdge fieldEdge) {
        metTargetField = fieldEdge.field().equals(field);
      }

      Traversal next;
      if (in) {
        next = moveForIn(state, outEdge.flowKind(), metTargetField, true);
      } else {
        next = moveForOut(state, outEdge.flowKind(), metTargetField, true);
      }

      if (next != Traversal.Undef) {
        ret.add(new Pair<>(outEdge.target(), next));
      }
    }

    for (FlowEdge inEdge : vfgForHeap.getPredsOf(node)) {

      boolean metTargetField = false;
      if (inEdge instanceof FieldEdge fieldEdge) {
        metTargetField = fieldEdge.field().equals(field);
      }
      Traversal nextState;
      if (in) {
        nextState = moveForIn(state, inEdge.flowKind(), metTargetField, false);
      } else {
        nextState = moveForOut(state, inEdge.flowKind(), metTargetField, false);
      }
      if (nextState != Traversal.Undef) {
        ret.add(new Pair<>(inEdge.source(), nextState));
      }
    }

    return ret;
  }

  private Traversal moveForOut(
      Traversal currState, FlowKind kind, boolean fieldMatch, boolean isForward) {
    switch (currState) {
      case THIS -> {
        if (!isForward && kind == FlowKind.THIS_CONNECT) {
          return Traversal.ThisAlias;
        }
      }
      case ThisAlias -> {
        if (isForward) {
          if (kind == FlowKind.LOCAL_ASSIGN) {
            return Traversal.ThisAlias;
          } else if (kind == FlowKind.FIELD_LOAD) {
            if (fieldMatch) {
              return Traversal.DirectVar;
            }
          }
        }
      }
      case DirectVar -> {
        if (isForward) {
          if (kind == FlowKind.LOCAL_ASSIGN
              || kind == FlowKind.FIELD_LOAD
              || kind == FlowKind.CALL_LOAD) {
            return Traversal.DirectVar;
          } else if (kind == FlowKind.RETURN) {
            return Traversal.Finish;
          } else if (kind == FlowKind.FIELD_STORE || kind == FlowKind.CALL_STORE) {
            return Traversal.StoredInVar;
          }
        } else {
          if (kind == FlowKind.FIELD_STORE || kind == FlowKind.CALL_STORE) {
            return Traversal.StoredInVar;
          }
        }
      }
      case StoredInVar -> {
        if (!isForward) {
          if (kind == FlowKind.LOCAL_ASSIGN
              || kind == FlowKind.FIELD_LOAD
              || kind == FlowKind.CALL_LOAD) {
            return Traversal.StoredInVar;
          } else if (kind == FlowKind.NEW) {
            return Traversal.Allocation;
          }
        } else {
          if (kind == FlowKind.PARAMETER_PASSING) {
            return Traversal.Finish;
          }
        }
      }
      case Allocation -> {
        if (isForward && kind == FlowKind.NEW) {
          return Traversal.DirectVar;
        }
      }
    }
    return Traversal.Undef;
  }

  private Traversal moveForIn(
      Traversal currState, FlowKind kind, boolean fieldMatch, boolean isForward) {
    switch (currState) {
      case Allocation -> {
        if (isForward && kind == FlowKind.NEW) {
          return Traversal.DirectVar;
        }
      }

      case THIS -> {
        if (!isForward && kind == FlowKind.THIS_CONNECT) {
          return Traversal.ThisAlias;
        }
      }
      case DirectVar -> {
        if (isForward) {
          if (kind == FlowKind.LOCAL_ASSIGN
              || kind == FlowKind.FIELD_LOAD
              || kind == FlowKind.CALL_LOAD) {
            return Traversal.DirectVar;
          } else if (kind == FlowKind.FIELD_STORE || kind == FlowKind.CALL_STORE) {
            return Traversal.StoredInVar;
          }
        } else {
          if (kind == FlowKind.FIELD_STORE || kind == FlowKind.CALL_STORE) {
            return Traversal.StoredInVar;
          }
        }
      }
      case StoredInVar -> {
        if (!isForward) {
          if (kind == FlowKind.LOCAL_ASSIGN
              || kind == FlowKind.FIELD_LOAD
              || kind == FlowKind.CALL_LOAD) {
            return Traversal.StoredInVar;
          } else if (kind == FlowKind.NEW) {
            return Traversal.Allocation;
          }
        } else {
          if (kind == FlowKind.PARAMETER_PASSING) {
            return Traversal.Finish;
          }
        }
      }
      case ThisAlias -> {
        if (isForward) {
          if (kind == FlowKind.LOCAL_ASSIGN) {
            return Traversal.ThisAlias;
          } else if (kind == FlowKind.FIELD_LOAD) {
            if (fieldMatch) {
              return Traversal.DirectVar;
            }
          }
        } else {
          if (kind == FlowKind.FIELD_STORE) {
            if (fieldMatch) return Traversal.StoredInVar;
          }
        }
      }
    }
    return Traversal.Undef;
  }

  private enum Traversal {
    DirectVar,
    StoredInVar,
    Allocation,
    ThisAlias,
    THIS,
    Finish,
    Undef
  }
}
