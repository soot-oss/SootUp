package de.upb.swt.soot.callgraph.spark.pag;

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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.upb.swt.soot.callgraph.MethodUtil;
import de.upb.swt.soot.callgraph.model.CallGraph;
import de.upb.swt.soot.callgraph.model.CalleeMethodSignature;
import de.upb.swt.soot.callgraph.spark.builder.GlobalNodeFactory;
import de.upb.swt.soot.callgraph.spark.pag.nodes.*;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.ClassConstant;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.JNewExpr;
import de.upb.swt.soot.core.model.Field;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.core.JavaSootClass;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;

public class PointerAssignmentGraph {

  // VariableNodes for local variables and static fields
  // Single VariableNode for all thrown exceptions
  // Depending on the options:
  // instance fields, method params and return values are represented with:
  // - variable, or
  // - field reference nodes
  // Array elements are always represented with field reference node
  // allocnodes created for alloc sites and string constants, including args to main method

  // Edges are created for all pointer-valued assignments including:
  // - casts
  // - throw catch
  // - pointers passed to and returned from methods (unless cfg is otf)
  // special edges for implicit flows:
  // - finalize methods
  // - java.lang.Thread start method to run method
  private static final Logger log = LoggerFactory.getLogger(PointerAssignmentGraph.class);

  // private final Graph<SparkVertex, SparkEdge> graph;
  private CallGraph callGraph;
  private View<? extends SootClass> view;

  private final Map<Local, LocalVariableNode> localToNodeMap = new HashMap<>();
  private final Map<Object, LocalVariableNode> valToLocalVariableNode = new HashMap<>();
  private final Map<Object, AllocationNode> valToAllocationNode = new HashMap<>();
  private final Queue<AllocationNode> newAllocationNodes = new ArrayDeque<>();
  private final Table<Object, Type, AllocationNode> valToReflectiveAllocationNode =
      HashBasedTable.create();
  private final Map<Object, GlobalVariableNode> valToGlobalVariableNode = new HashMap<>();
  private final Map<Value, NewInstanceNode> valToNewInstanceNode = new HashMap<>();
  private final GlobalNodeFactory nodeFactory = new GlobalNodeFactory(this);
  private final SparkEdgeFactory edgeFactory = new SparkEdgeFactory();
  private final List<VariableNode> dereferences = new ArrayList<>();
  private InternalEdges internalEdges = new InternalEdges();
  private final List<VariableNode> variableNodes = new ArrayList<>();
  private int maxFinishingNumber = 0;
  private Map<AbstractInvokeExpr, Pair<Node, Node>> callAssigns = new HashMap<>();
  private Map<AbstractInvokeExpr, SootMethod> callToMethod = new HashMap<>();
  private Map<AbstractInvokeExpr, Node> virtualCallsToReceivers = new HashMap<>();
  private Map<SootMethod, IntraproceduralPointerAssignmentGraph> methodToIntraPag = new HashMap<>();
  private Set<IntraproceduralPointerAssignmentGraph> addedIntraPags = new HashSet<>();

  public PointerAssignmentGraph(View<? extends SootClass> view, CallGraph callGraph) {
    this.view = view;
    this.callGraph = callGraph;
    // this.graph = new DirectedAcyclicGraph<>(null, null, false);
    build();
  }

  private void build() {
    for (SootClass clazz : view.getClasses()) {
      for (SootMethod method : clazz.getMethods()) {
        if (!method.isAbstract() && callGraph.containsMethod(method.getSignature())) {
          IntraproceduralPointerAssignmentGraph intraPAG =
              IntraproceduralPointerAssignmentGraph.getInstance(this, method);
          intraPAG.addToPAG();
        }
      }
    }
    handleCallEdges();
  }

  private void handleCallEdges() {
    Iterator<Pair<MethodSignature, CalleeMethodSignature>> iter = callGraph.getEdges().iterator();
    while (iter.hasNext()) {
      Pair<MethodSignature, CalleeMethodSignature> edge = iter.next();
      SootMethod tgt = MethodUtil.methodSignatureToMethod(view, edge.getValue().getMethodSignature());
        if (tgt.isConcrete() || tgt.isNative()) {
          IntraproceduralPointerAssignmentGraph intraPAG =
              IntraproceduralPointerAssignmentGraph.getInstance(this, tgt);
          intraPAG.addToPAG();
        }
        CallTargetHandler callTargetHandler = new CallTargetHandler(this);
        callTargetHandler.addCallTarget(edge);
    }
  }


  public void addEdge(Node source, Node target) {
    internalEdges.addEdge(source, target);
  }

  public View<JavaSootClass> getView() {
    return (View<JavaSootClass>) view;
  }

  public LocalVariableNode getOrCreateLocalVariableNode(
      Object value, Type type, SootMethod method) {
    // TODO: SPARK_OPTS RTA
    if (value instanceof Local) {
      Local local = (Local) value;
      // TODO: numbering?
      LocalVariableNode localVariableNode = localToNodeMap.get(local);
      if (localVariableNode == null) {
        localVariableNode = new LocalVariableNode(this, value, type, method);
        localToNodeMap.put(local, localVariableNode);
        // TODO: addNodeTag()
      } else if (!(localVariableNode.getType().equals(type))) {
        throw new RuntimeException(
            MessageFormat.format(
                "Value {0} of type {1} previously had type {2}",
                value, type, localVariableNode.getType()));
      }
      return localVariableNode;
    }
    LocalVariableNode localVariableNode = valToLocalVariableNode.get(value);
    if (localVariableNode == null) {
      localVariableNode = new LocalVariableNode(this, value, type, method);
      valToLocalVariableNode.put(value, localVariableNode);
      // TODO: addNodeTag()
    } else if (!(localVariableNode.getType().equals(type))) {
      throw new RuntimeException(
          MessageFormat.format(
              "Value {0} of type {1} previously had type {2}",
              value, type, localVariableNode.getType()));
    }
    return localVariableNode;
  }

  /** Finds or creates the FieldRefNode for base variable base and field field, of type type. */
  public FieldReferenceNode getOrCreateFieldReferenceNode(VariableNode base, Field field) {
    FieldReferenceNode fieldRef = base.getField(field);
    if (fieldRef == null) {
      fieldRef = new FieldReferenceNode(base, field);
      if (base instanceof LocalVariableNode) {
        addNodeTag(fieldRef, ((LocalVariableNode) base).getMethod());
      } else {
        addNodeTag(fieldRef, null);
      }
    }
    return fieldRef;
  }

  public AllocationDotField getOrCreateAllocationDotField(
      AllocationNode allocationNode, Field field) {
    AllocationDotField node = allocationNode.dot(field);
    if (node == null) {
      node = new AllocationDotField(this, allocationNode, field);
    }
    return node;
  }

  private void addNodeTag(Node node, SootMethod m) {
    // TODO: disabled by default
  }

  public AllocationNode getOrCreateAllocationNode(Object newExpr, Type type, SootMethod method) {
    // TODO: SPARK_OPT types-for-sites
    AllocationNode node;
    if (newExpr instanceof JNewExpr) {
      node = valToAllocationNode.get(newExpr);
      if (node == null) {
        node = new AllocationNode(type, newExpr, method);
        valToAllocationNode.put(newExpr, node);
        newAllocationNodes.add(node);
        addNodeTag(node, null);
      } else if (!node.getType().equals(type)) {
        throw new RuntimeException(
            MessageFormat.format(
                "NewExpr {0} of type {1} previously had type {2}", newExpr, type, node.getType()));
      }
    } else {
      node = valToReflectiveAllocationNode.get(newExpr, type);
      if (node == null) {
        node = new AllocationNode(type, newExpr, method);
        valToReflectiveAllocationNode.put(newExpr, type, node);
        newAllocationNodes.add(node);
        addNodeTag(node, method);
      }
    }
    return node;
  }

  public GlobalVariableNode getOrCreateGlobalVariableNode(Object value, Type type) {
    // TODO: SPARK_OPTS rta

    GlobalVariableNode node = valToGlobalVariableNode.get(value);
    if (node == null) {
      node = new GlobalVariableNode(this, value, type);
      addNodeTag(node, null);
    } else if (!node.getType().equals(type)) {
      throw new RuntimeException(
          MessageFormat.format(
              "Value {0} of type {1} previously had type {2}", value, type, node.getType()));
    }
    return node;
  }

  public FieldReferenceNode getOrCreateLocalFieldReferenceNode(
      Object baseValue, Type baseType, Field field, SootMethod method) {
    VariableNode base = getOrCreateLocalVariableNode(baseValue, baseType, method);
    FieldReferenceNode node = getOrCreateFieldReferenceNode(base, field);
    // TODO: SPARK_OPTS library mode
    return node;
  }

  public AllocationNode getOrCreateClassConstantNode(ClassConstant cc) {
    // TODO: SPARK_OPT types-for-sites vta
    return valToAllocationNode.computeIfAbsent(cc, k -> createNodeForClassConstant(cc));
  }

  private ClassConstantNode createNodeForClassConstant(ClassConstant cc) {
    ClassConstantNode node = new ClassConstantNode(cc);
    newAllocationNodes.add(node);
    addNodeTag(node, null);
    return node;
  }

  public NewInstanceNode getOrCreateNewInstanceNode(Value value, Type type, SootMethod method) {
    return valToNewInstanceNode.computeIfAbsent(
        value, k -> createNodeForNewInstance(value, type, method));
  }

  private NewInstanceNode createNodeForNewInstance(Value value, Type type, SootMethod method) {
    NewInstanceNode node = new NewInstanceNode(this, type, value);
    addNodeTag(node, method);
    return node;
  }

  /** Adds the base of a dereference to the list of dereferenced variables. */
  public void addDereference(VariableNode base) {
    dereferences.add(base);
  }

  public GlobalNodeFactory getNodeFactory() {
    return nodeFactory;
  }

  public LocalVariableNode getLocalVariableNode(Object value) {
    // TODO: SPARK_OPTS rta
    if (value instanceof Local) {
      return localToNodeMap.get(value);
    }
    return valToLocalVariableNode.get(value);
  }

  public Map<VariableNode, Set<VariableNode>> getSimpleEdges() {
    return internalEdges.simpleEdges;
  }

  public Map<AllocationNode, Set<VariableNode>> getAllocationEdges() {
    return internalEdges.allocationEdges;
  }

  public Map<VariableNode, Set<FieldReferenceNode>> getStoreEdges() {
    return internalEdges.storeEdges;
  }

  public Map<FieldReferenceNode, Set<VariableNode>> getLoadEdges() {
    return internalEdges.loadEdges;
  }

  public Set<VariableNode> storeInvLookup(FieldReferenceNode key) {
    // TODO: somethingMerged?
    return internalEdges.storeEdgesInv.get(key);
  }

  public Set<VariableNode> loadLookup(FieldReferenceNode key) {
    return internalEdges.loadEdges.get(key);
  }

  public List<VariableNode> getVariableNodes() {
    return variableNodes;
  }

  public int getMaxFinishingNumber() {
    return maxFinishingNumber;
  }

  public void setMaxFinishingNumber(int maxFinishingNumber) {
    this.maxFinishingNumber = maxFinishingNumber;
  }

  public Map<AbstractInvokeExpr, Pair<Node, Node>> getCallAssigns() {
    return callAssigns;
  }

  public Map<AbstractInvokeExpr, SootMethod> getCallToMethod() {
    return callToMethod;
  }

  public Map<AbstractInvokeExpr, Node> getVirtualCallsToReceivers() {
    return virtualCallsToReceivers;
  }

  public Set<IntraproceduralPointerAssignmentGraph> getAddedIntraPags() {
    return addedIntraPags;
  }

  public Map<SootMethod, IntraproceduralPointerAssignmentGraph> getMethodToIntraPag() {
    return methodToIntraPag;
  }
}
