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

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;
import qilin.core.PTA;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.builder.callgraph.Edge;
import qilin.core.builder.callgraph.OnFlyCallGraph;
import qilin.core.pag.AllocNode;
import qilin.core.pag.ContextField;
import qilin.core.pag.ContextVarNode;
import qilin.core.pag.FieldRefNode;
import qilin.core.pag.GlobalVarNode;
import qilin.core.pag.LocalVarNode;
import qilin.core.pag.MethodPAG;
import qilin.core.pag.PAG;
import qilin.core.pag.PagNode;
import qilin.core.pag.SparkField;
import qilin.core.pag.ValNode;
import qilin.core.pag.VarNode;
import qilin.core.pag.VirtualCallSite;
import qilin.pta.toolkits.common.OAG;
import qilin.pta.toolkits.moon.graph.FieldPointsToGraph;
import qilin.pta.toolkits.moon.graph.FlowKind;
import qilin.pta.toolkits.moon.graph.VFG;
import qilin.util.PTAUtils;
import qilin.util.queue.QueueReader;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.ClassConstant;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ReferenceType;

public class MoonDataConstructor {

  private final PTA pta;
  private final PAG pag;
  private final VFG vfgForField;
  private final VFG vfgForObj;
  private final OAG oag;
  private final FieldRecorder fieldRecorder;
  private final PtrSetCache ptrSetCache;
  private final SetMultimap<AllocNode, SootMethod> objToInvokedMethods =
      Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet);
  private final SetMultimap<SootMethod, AllocNode> methodToRecvObjs =
      Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet);
  private final Set<LocalVarNode> allocatedVars = ConcurrentHashMap.newKeySet();
  private final FieldFlowRecorder fieldFlowRecorder;
  private final KeyTypeCollector keyTypeCollector;
  private final ContainerCollector containerCollector;
  private final FieldPointsToGraph fieldPointsToGraph;

  public MoonDataConstructor(PTA pta) {
    this.pta = pta;
    this.pag = pta.getPag();
    this.ptrSetCache = new PtrSetCache(pta);
    this.vfgForField = new VFG();
    this.vfgForObj = new VFG();
    this.fieldRecorder = new FieldRecorder(pta);
    this.fieldFlowRecorder =
        new FieldFlowRecorder(pta, fieldRecorder, vfgForObj, objToInvokedMethods);
    this.oag = new OAG(pta);
    this.keyTypeCollector = new KeyTypeCollector(pta, fieldRecorder);
    this.containerCollector =
        new ContainerCollector(pta, fieldRecorder, fieldFlowRecorder, keyTypeCollector);
    fieldPointsToGraph = new FieldPointsToGraph(pta, ptrSetCache);
  }

  public record MoonDataStructure(
      OAG oag,
      VFG vfgForField,
      VFG vfgForObj,
      FieldRecorder fieldRecorder,
      FieldFlowRecorder fieldFlowRecorder,
      SetMultimap<SootMethod, AllocNode> mthToRecvObj,
      KeyTypeCollector keyTypeCollector,
      SetMultimap<AllocNode, SootMethod> objToIvkMtds,
      Set<LocalVarNode> allocVars,
      Set<AllocNode> containers,
      PtrSetCache ptrSetCache,
      FieldPointsToGraph fieldPointsToGraph,
      PAG pag) {}

  public MoonDataStructure analyze() {
    List<Runnable> runnableItems =
        List.of(
            this.oag::build,
            this::buildOag,
            this::addExtraFlow,
            this::buildObjAndInvokeToCallee,
            this::initOpenTypes,
            this::initFieldReachability,
            this::initContainerFilter);

    for (Runnable runnableItem : runnableItems) {
      runnableItem.run();
    }
    return new MoonDataStructure(
        oag,
        vfgForField,
        vfgForObj,
        fieldRecorder,
        fieldFlowRecorder,
        methodToRecvObjs,
        keyTypeCollector,
        objToInvokedMethods,
        allocatedVars,
        containerCollector.containerToFields.keySet(),
        ptrSetCache,
        fieldPointsToGraph,
        pag);
  }

  private void initOpenTypes() {
    keyTypeCollector.run(pag.getAllocNodes());
  }

  private void initFieldReachability() {
    this.fieldFlowRecorder.build(this.keyTypeCollector);
  }

  private void initContainerFilter() {
    containerCollector.collect();
  }

  private void buildOag() {
    Map<ValNode, Set<ValNode>> simples = pta.getPag().getSimple();

    simples.keySet().parallelStream()
        .forEach(
            source -> {
              Set<ValNode> targets = simples.get(source);
              if (localVarBase(source)) {
                targets.forEach(
                    target -> {
                      if (localVarBase(target)) {
                        LocalVarNode toNode = fetchVar(target);
                        LocalVarNode fromNode = fetchVar(source);
                        LocalVarNode srcTmp = fetchLocalVar(source);
                        if (srcTmp.isInterProcSource()
                            && srcTmp.isReturn()) { // source of THROW and RETURN
                          vfgForField.addSimpleFlowEdge(FlowKind.RETURN, fromNode, toNode);
                        } else {
                          if (fetchLocalVar(target)
                              .isInterProcTarget()) { // target of THIS_PASSING and PARAM_PASSING
                            if (!fetchLocalVar(target).isThis()) {
                              vfgForField.addSimpleFlowEdge(
                                  FlowKind.PARAMETER_PASSING, fromNode, toNode);
                            }
                          } else {
                            vfgForField.addSimpleFlowEdge(FlowKind.LOCAL_ASSIGN, fromNode, toNode);
                          }
                        }
                      } else if (target instanceof ContextField ctxField) {
                        LocalVarNode varNode = fetchVar(source);
                        vfgForField.addSimpleFlowEdge(FlowKind.INSTANCE_STORE, varNode, ctxField);
                      }
                    });
              } else if (source instanceof ContextField ctxField) {
                targets.forEach(
                    t -> {
                      assert localVarBase(t);
                      LocalVarNode varNode = fetchVar(t);
                      vfgForField.addSimpleFlowEdge(FlowKind.INSTANCE_LOAD, ctxField, varNode);
                    });
              }
            });

    StreamSupport.stream(pta.getCallGraph().spliterator(), true) // true for parallel
        .forEach(
            callEdge -> {
              Stmt callsite = callEdge.srcStmt();
              SootMethod caller = callEdge.src();
              if (caller != null) {
                SootMethod callee = callEdge.tgt();
                if (!callee.isStatic()) {
                  MethodNodeFactory calleeNodeFactory =
                      pta.getPag().getMethodPAG(callee).nodeFactory();
                  LocalVarNode thisVar = (LocalVarNode) calleeNodeFactory.caseThis();
                  AbstractInvokeExpr invokeExpr = callsite.asInvokableStmt().getInvokeExpr().get();
                  Value base = null;
                  if (invokeExpr instanceof AbstractInstanceInvokeExpr instanceInvokeExpr) {
                    base = instanceInvokeExpr.getBase();
                  }
                  if (base != null) {
                    LocalVarNode fromNode = (LocalVarNode) pta.getPag().findValNode(base, caller);
                    vfgForField.addSimpleFlowEdge(FlowKind.THIS_PASSING, fromNode, thisVar);
                  }
                }
              }
            });
  }

  private void addExtraFlow() {
    pta.getNakedReachableMethods().parallelStream().forEach(this::buildInternalWithInline);
  }

  protected void buildInternalWithInline(SootMethod method) {
    MethodPAG methodPAG = pag.getMethodPAG(method);
    MethodNodeFactory factory = methodPAG.nodeFactory();
    Set<FieldRefNode> stores = new HashSet<>();
    Set<FieldRefNode> loads = new HashSet<>();
    Set<PagNode> thisAlias = new HashSet<>();
    thisAlias.add(factory.caseThis());
    vfgForObj.recordThisVar(factory.caseThis());
    QueueReader<PagNode> reader = methodPAG.getInternalReader().clone();
    while (reader.hasNext()) {
      PagNode from = reader.next(), to = reader.next();
      if (from instanceof LocalVarNode) {
        if (to instanceof LocalVarNode) {
          if (thisAlias.contains(from)) {
            thisAlias.add(to);
          }
          vfgForObj.addSimpleFlowEdge(FlowKind.LOCAL_ASSIGN, from, to);
        } else if (to instanceof FieldRefNode fr) {
          stores.add(fr);
          vfgForObj.addFieldFlowEdge(FlowKind.FIELD_STORE, from, fr.getBase(), fr.getField());
          vfgForField.addFieldFlowEdge(FlowKind.FIELD_STORE, from, fr.getBase(), fr.getField());
          fieldRecorder.putStore((LocalVarNode) fr.getBase(), fr.getField(), (LocalVarNode) from);
        } else if (to instanceof GlobalVarNode globalVarTo) {
          // local-global
          Object variable = globalVarTo.getVariable();
          if (variable instanceof FieldSignature) {
            vfgForField.addSimpleFlowEdge(FlowKind.STATIC_STORE, from, globalVarTo);
          } else if (!(variable instanceof ClassConstant)
              && !(variable instanceof StringConstant)) {
            throw new RuntimeException("Unknown GlobalVarNode");
          }
        } else {
          throw new RuntimeException("Unknown Node Type");
        }

      } else if (from instanceof AllocNode) {
        if (to instanceof LocalVarNode localVarTo) {
          vfgForObj.addSimpleFlowEdge(FlowKind.NEW, from, to);
          allocatedVars.add(localVarTo);
          vfgForField.addSimpleFlowEdge(FlowKind.NEW, from, to);

        } else if (to instanceof GlobalVarNode globalVarTo) {
          vfgForField.addSimpleFlowEdge(FlowKind.LOCAL_ASSIGN, from, globalVarTo);
        } else {
          throw new RuntimeException("Unknown to Node Type");
        }
      } else if (from instanceof FieldRefNode fr) {
        loads.add(fr);
        vfgForObj.addFieldFlowEdge(FlowKind.FIELD_LOAD, fr.getBase(), to, fr.getField());
        vfgForField.addFieldFlowEdge(FlowKind.FIELD_LOAD, fr.getBase(), to, fr.getField());
        fieldRecorder.putLoad((LocalVarNode) fr.getBase(), fr.getField(), (LocalVarNode) to);
      } else if (from instanceof GlobalVarNode globalVarFrom) {
        // global-local
        Object variable = globalVarFrom.getVariable();
        if (variable instanceof FieldSignature) {
          vfgForField.addSimpleFlowEdge(FlowKind.STATIC_LOAD, globalVarFrom, to);
        } else if (!(variable instanceof ClassConstant) && !(variable instanceof StringConstant)) {
          throw new RuntimeException("Unknown GlobalVarNode");
        }
      }
    }

    doHandleField(thisAlias, stores, true);
    doHandleField(thisAlias, loads, false);

    // handle call statements.
    for (final InvokableStmt s : methodPAG.getInvokeStmts()) {
      AbstractInvokeExpr invokeExpr = s.getInvokeExpr().get();
      int numArgs = invokeExpr.getArgCount();
      Value[] args = new Value[numArgs];
      for (int i = 0; i < numArgs; i++) {
        Value arg = invokeExpr.getArg(i);
        if (!(arg.getType() instanceof ReferenceType) || arg instanceof NullConstant) continue;
        args[i] = arg;
      }
      LocalVarNode retDest = null;
      if (s instanceof JAssignStmt) {
        Value dest = ((JAssignStmt) s).getLeftOp();
        if (dest.getType() instanceof ReferenceType) {
          retDest = (LocalVarNode) pag.findValNode(dest, method);
        }
      }
      if (invokeExpr instanceof AbstractInstanceInvokeExpr instanceInvokeExpr) {
        LocalVarNode receiver =
            (LocalVarNode) pag.findValNode(instanceInvokeExpr.getBase(), method);
        if (instanceInvokeExpr instanceof JSpecialInvokeExpr specialInvokeExpr) {
          SootMethod inlinedMethod =
              pta.getView().getMethod(specialInvokeExpr.getMethodSignature()).get();
          inlineForMatchObjTraversal(s, method, inlinedMethod, false);
          if (retDest != null) {
            vfgForField.addSimpleFlowEdge(FlowKind.CALL_LOAD, receiver, retDest);
          }
          pta.getScene().getCallDetails().addInvokeExpr(receiver, instanceInvokeExpr);
        } else {
          if (retDest != null) {
            vfgForObj.addSimpleFlowEdge(FlowKind.CALL_LOAD, receiver, retDest);
            vfgForField.addSimpleFlowEdge(FlowKind.CALL_LOAD, receiver, retDest);
            pta.getScene().getCallDetails().addInvokeExpr(receiver, instanceInvokeExpr);
          }

          for (int i = 0; i < numArgs; i++) {
            if (args[i] == null) {
              continue;
            }
            ValNode argNode = pag.findValNode(args[i], method);
            if (argNode instanceof LocalVarNode) {
              vfgForObj.addSimpleFlowEdge(FlowKind.CALL_STORE, argNode, receiver);
            }
          }
        }

        for (int i = 0; i < numArgs; i++) {
          if (args[i] == null) {
            continue;
          }
          ValNode argNode = pag.findValNode(args[i], method);
          if (argNode instanceof LocalVarNode) {
            vfgForField.addSimpleFlowEdge(FlowKind.CALL_STORE, argNode, receiver);
            pta.getScene().getCallDetails().addInvokeExpr(receiver, instanceInvokeExpr);
          }
        }

      } else {
        if (invokeExpr instanceof JStaticInvokeExpr sie) {
          SootMethod inlinedMethod = pta.getView().getMethod(sie.getMethodSignature()).get();
          inlineForMatchObjTraversal(s, method, inlinedMethod, true);
        }
      }
    }

    // handle parameters.
    for (int i = 0; i < method.getParameterCount(); ++i) {
      if (method.getParameterType(i) instanceof ReferenceType
          && !PTAUtils.isPrimitiveArrayType(method.getParameterType(i))) {
        LocalVarNode param = (LocalVarNode) factory.caseParm(i);
        vfgForObj.addSimpleFlowEdge(FlowKind.PARAMETER_PASSING, param, param);
      }
    }

    if (!method.isStatic()) {
      VarNode thisVar = factory.caseThis();
      vfgForObj.addSimpleFlowEdge(FlowKind.PARAMETER_PASSING, thisVar, thisVar);
    }

    // handle returns
    if (method.getReturnType() instanceof ReferenceType
        && !PTAUtils.isPrimitiveArrayType(method.getReturnType())) {
      vfgForObj.addSimpleFlowEdge(FlowKind.RETURN, factory.caseRet(), factory.caseRet());
    }
  }

  private void doHandleField(
      Set<PagNode> thisAliases, Set<FieldRefNode> operations, boolean isStore) {
    for (FieldRefNode operation : operations) {
      LocalVarNode baseVar = (LocalVarNode) operation.getBase();
      SparkField field = operation.getField();
      boolean isNonThisBase = !thisAliases.contains(baseVar);
      for (AllocNode heap : ptrSetCache.ptsOf(baseVar)) {
        fieldRecorder.recordObjToField(heap, field);
        if (isNonThisBase) {
          if (isStore) {
            fieldFlowRecorder.objToNonThisFieldStore.put(heap, field, baseVar);
          } else {
            fieldFlowRecorder.objToNonThisFieldLoad.put(heap, field, baseVar);
            fieldFlowRecorder.hasNonThisFieldLoad.add(field);
          }
        }
      }
    }
  }

  private void inlineForMatchObjTraversal(
      Stmt invokeStmt, SootMethod caller, SootMethod inlinedMethod, boolean isStaticCall) {
    vfgForObj.recordInlineMethod(inlinedMethod, caller);
    AbstractInvokeExpr ie = invokeStmt.asInvokableStmt().getInvokeExpr().get();
    int numArgs = ie.getArgCount();
    Value[] args = new Value[numArgs];
    for (int i = 0; i < numArgs; i++) {
      Value arg = ie.getArg(i);
      if (!(arg.getType() instanceof ReferenceType) || arg instanceof NullConstant) continue;
      args[i] = arg;
    }
    LocalVarNode retDest = null;
    if (invokeStmt instanceof JAssignStmt) {
      Value dest = ((JAssignStmt) invokeStmt).getLeftOp();
      if (dest.getType() instanceof ReferenceType) {
        retDest = (LocalVarNode) pag.findValNode(dest, caller);
      }
    }
    LocalVarNode receiver = null;
    if (ie instanceof AbstractInstanceInvokeExpr iie) {
      receiver = (LocalVarNode) pag.findValNode(iie.getBase(), caller);
    }
    MethodPAG mpag = pag.getMethodPAG(inlinedMethod);
    MethodNodeFactory nodeFactory = mpag.nodeFactory();
    if (numArgs != inlinedMethod.getParameterCount()) {
      return;
    }
    // handle parameters
    for (int i = 0; i < inlinedMethod.getParameterCount(); ++i) {
      if (args[i] != null
          && inlinedMethod.getParameterType(i) instanceof ReferenceType
          && !PTAUtils.isPrimitiveArrayType(inlinedMethod.getParameterType(i))) {
        LocalVarNode param = (LocalVarNode) nodeFactory.caseParm(i);
        ValNode argVal = pag.findValNode(args[i], caller);
        if (argVal instanceof LocalVarNode argNode) {
          vfgForObj.addSimpleFlowEdge(FlowKind.LOCAL_ASSIGN, argNode, param);
        }
      }
    }
    // handle return node
    if (retDest != null
        && inlinedMethod.getReturnType() instanceof ReferenceType
        && !PTAUtils.isPrimitiveArrayType(inlinedMethod.getReturnType())) {
      vfgForObj.addSimpleFlowEdge(FlowKind.LOCAL_ASSIGN, nodeFactory.caseRet(), retDest);
    }
    // handle this node
    if (receiver != null) {
      vfgForObj.addSimpleFlowEdge(FlowKind.LOCAL_ASSIGN, receiver, nodeFactory.caseThis());
    }
  }

  private boolean localVarBase(ValNode valNode) {
    if (valNode instanceof ContextVarNode cvn) {
      return cvn.base() instanceof LocalVarNode;
    } else {
      return valNode instanceof LocalVarNode;
    }
  }

  private LocalVarNode fetchLocalVar(ValNode valNode) {
    if (valNode instanceof ContextVarNode cvn) {
      if (cvn.base() instanceof LocalVarNode) {
        return (LocalVarNode) cvn.base();
      }
    } else if (valNode instanceof LocalVarNode) {
      return (LocalVarNode) valNode;
    }
    throw new RuntimeException("Not a local var: " + valNode);
  }

  private LocalVarNode fetchVar(ValNode valNode) {
    if (valNode instanceof ContextVarNode cvn) {
      VarNode base = cvn.base();
      if (base instanceof LocalVarNode lvn) {
        return lvn;
      }
    } else if (valNode instanceof LocalVarNode lvn) {
      return lvn;
    }
    throw new RuntimeException("Not a local var: " + valNode);
  }

  private void buildObjAndInvokeToCallee() {
    OnFlyCallGraph callgraph = pta.getCallGraph();
    // collect virtual callsites.
    Set<VirtualCallSite> vcallsites = new HashSet<>();
    for (Edge edge : callgraph) {
      SootMethod tgtM = edge.tgt();
      // this fork has no phantom-class tracking (PTAScene#getPhantomClasses() is always empty)
      if (tgtM.isStatic()) {
        continue;
      }
      final InvokableStmt s = edge.srcStmt();
      AbstractInvokeExpr ie = s.getInvokeExpr().get();
      if (ie instanceof AbstractInstanceInvokeExpr iie) {
        LocalVarNode receiver = (LocalVarNode) pag.findValNode(iie.getBase(), edge.src());
        MethodSubSignature subSig = iie.getMethodSignature().getSubSignature();
        VirtualCallSite virtualCallSite =
            new VirtualCallSite(receiver, s, edge.getSrc(), iie, subSig, Edge.ieToKind(iie));
        vcallsites.add(virtualCallSite);
      } else {
        throw new RuntimeException("ie could not be of " + ie.getClass());
      }
    }
    vcallsites.parallelStream()
        .forEach(
            vcallsite -> {
              // foreach virtualcallsite, we build mapping from their receiver objects.
              AbstractInstanceInvokeExpr iie = vcallsite.iie();
              LocalVarNode receiver =
                  (LocalVarNode) pag.findValNode(iie.getBase(), vcallsite.container().method());
              for (AllocNode heap :
                  pta.reachingObjects(receiver).toCIPointsToSet().toCollection()) {
                QueueReader<SootMethod> reader = pta.getCgb().dispatch(heap.getType(), vcallsite);
                while (reader.hasNext()) {
                  SootMethod tgtM = reader.next();
                  objToInvokedMethods.put(heap, tgtM);
                  methodToRecvObjs.put(tgtM, heap);
                }
              }
            });
  }
}
