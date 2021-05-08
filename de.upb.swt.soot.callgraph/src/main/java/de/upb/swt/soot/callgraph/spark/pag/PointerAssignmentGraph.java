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
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import de.upb.swt.soot.callgraph.MethodUtil;
import de.upb.swt.soot.callgraph.model.CallGraph;
import de.upb.swt.soot.callgraph.spark.builder.GlobalNodeFactory;
import de.upb.swt.soot.callgraph.spark.builder.SparkOptions;
import de.upb.swt.soot.callgraph.spark.pag.nodes.*;
import de.upb.swt.soot.callgraph.typehierarchy.TypeHierarchy;
import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.ClassConstant;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.JNewExpr;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Field;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.core.JavaSootClass;

import java.io.ObjectInputStream;
import java.text.MessageFormat;
import java.util.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private TypeHierarchy typeHierarchy;
  private boolean somethingMerged = false;
  private SparkOptions sparkOptions;

  public PointerAssignmentGraph(View<? extends SootClass> view, CallGraph callGraph, SparkOptions sparkOptions) {
    this.view = view;
    this.callGraph = callGraph;
    this.sparkOptions = sparkOptions;
    if(sparkOptions.isIgnoreTypes()){
      this.typeHierarchy = new ViewTypeHierarchy(view);
    }
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
    Iterator<Pair<MethodSignature, CalleeMethodSignature>> iter = getCallEdges().iterator();
    while (iter.hasNext()) {
      Pair<MethodSignature, CalleeMethodSignature> edge = iter.next();
      SootMethod tgt =
          MethodUtil.methodSignatureToMethod(view, edge.getValue().getMethodSignature());
      if (tgt.isConcrete() || tgt.isNative()) {
        IntraproceduralPointerAssignmentGraph intraPAG =
            IntraproceduralPointerAssignmentGraph.getInstance(this, tgt);
        intraPAG.addToPAG();
      }
      CallTargetHandler callTargetHandler = new CallTargetHandler(this);
      callTargetHandler.addCallTarget(edge);
    }
  }

  private Set<Pair<MethodSignature, CalleeMethodSignature>> getCallEdges() {
    Set<MethodSignature> methodSigs = callGraph.getMethodSignatures();
    Set<Pair<MethodSignature, CalleeMethodSignature>> callEdges = new HashSet<>();
    for (MethodSignature caller : methodSigs) {
      SootMethod method = MethodUtil.methodSignatureToMethod(view, caller);
      for (Stmt s : method.getBody().getStmts()) {
        if (s.containsInvokeExpr()) {
          CalleeMethodSignature callee =
                  new CalleeMethodSignature(
                          s.getInvokeExpr().getMethodSignature(),
                          MethodUtil.findCallGraphEdgeType(s.getInvokeExpr()),
                          s);
          callEdges.add(new ImmutablePair<>(caller, callee));
        }
      }
    }
    return callEdges;
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

  public GlobalVariableNode getGlobalVariableNode(Object value){
    if(sparkOptions.isRta()){
      value = null;
    }
    return valToGlobalVariableNode.get(value);
  }

  public LocalVariableNode getLocalVariableNode(Object value) {
    if(sparkOptions.isRta()){
      value = null;
    } else if (value instanceof Local) {
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

  public Map<VariableNode, Set<NewInstanceNode>> getNewInstanceEdges(){
    return internalEdges.newInstanceEdges;
  }

  public Map<NewInstanceNode, Set<VariableNode>> getAssignInstanceEdges(){
    return internalEdges.assignInstanceEdges;
  }

  public Set<VariableNode> storeInvLookup(FieldReferenceNode key) {
    // TODO: somethingMerged?
    return internalEdges.storeEdgesInv.get(key);
  }

  public Set<VariableNode> simpleInvLookup(VariableNode key){
    return internalEdges.simpleEdgesInv.get(key);
  }

  public Set<VariableNode> loadLookup(FieldReferenceNode key) {
    return internalEdges.loadEdges.get(key);
  }

  public Set<FieldReferenceNode> loadInvLookup(VariableNode key) {
    return internalEdges.loadEdgesInv.get(key);
  }

  public Set<VariableNode> allocLookup(AllocationNode key){
    return internalEdges.allocationEdges.get(key);
  }

  public Set<AllocationNode> allocInvLookup(VariableNode key){
    return internalEdges.allocationEdgesInv.get(key);
  }


  /**
   * to notify PAG that n2 has been merged into n1
   *
   */
  public void mergedWith(Node n1, Node n2){
    if(n1.equals(n2)){
      throw new RuntimeException("merged nodes cannot be the same");
    }
    somethingMerged = true;
    //TODO: notify ofcg

    Map[] edgeMaps = {internalEdges.simpleEdges, internalEdges.allocationEdges, internalEdges.storeEdges, internalEdges.loadEdges,
    internalEdges.simpleEdgesInv, internalEdges.allocationEdgesInv, internalEdges.storeEdgesInv, internalEdges.loadEdgesInv};
    for (Map<Node, Set<Node>> m : edgeMaps) {
      if(!m.keySet().contains(n2)){
        continue;
      }
      //Pair<Set<Node>, Set<Node>> setPair = new ImmutablePair<>(m.get(n1), m.get(n2));
      Set<Node> set1 = m.get(n1);
      Set<Node> set2 = m.get(n2);

      if(set1.isEmpty()){
        if(set2!=null){
          m.put(n1, set2);
        }
      } else if(set2.isEmpty()){
        // nothing needed
      } else if(set1 instanceof HashSet){
        if(set2 instanceof HashSet){
          set1.addAll(set2);
        } else{
          for(Node node: set2){
            set1.add(node);
          }
        }
      } else if(set2 instanceof HashSet){
        for(Node node: set1){
          set2.add(node);
        }
        m.put(n1, set2);
      } else if(set1.size()*set2.size()<1000){
        Node[] ret = new Node[set1.size()+set2.size()];
        System.arraycopy(set1, 0, ret, 0, set1.size());
        int j = set1.size();
        outer: for(Node rep: set2){
          for(int k=0; k<j; k++){
            if(rep == ret[k]){
              continue outer;
            }
          }
          ret[j++] = rep;
        }
        Node[] newArray = new Node[j];
        System.arraycopy(ret, 0, newArray, 0, j);
        m.put(n1, Sets.newHashSet(ret = newArray));
      } else{
        HashSet<Node> s = new HashSet<>(set1.size()+set2.size());
        s.addAll(set1);
        s.addAll(set2);
        m.put(n1, s);
      }
      m.remove(n2);
    }
  }

  public void cleanUpMerges(){
    lookupInMap(internalEdges.simpleEdges);
    lookupInMap(internalEdges.allocationEdges);
    lookupInMap(internalEdges.storeEdges);
    lookupInMap(internalEdges.loadEdges);
    lookupInMap(internalEdges.simpleEdgesInv);
    lookupInMap(internalEdges.allocationEdgesInv);
    lookupInMap(internalEdges.storeEdgesInv);
    lookupInMap(internalEdges.loadEdgesInv);
    somethingMerged=false;
  }

  private <K, N extends Node> void lookupInMap(Map<K, Set<N>> map) {
    for (K key : map.keySet()) {
      lookup(map, key);
    }
  }

  protected final static Node[] EMPTY_NODE_ARRAY = new Node[0];

  protected <K,N extends Node> Node[] lookup(Map<K, Set<N>> m, K key) {
    Set<N> valueList = m.get(key);
    if (valueList == null) {
      return EMPTY_NODE_ARRAY;
    }

    m.put(key, valueList);

    Node[] ret = (Node[]) valueList.toArray();
    if (somethingMerged) {
      for (int i = 0; i < ret.length; i++) {
        Node reti = ret[i];
        Node rep = reti.getReplacement();
        if (rep != reti || rep == key) {
          Set<Node> s;
          if (ret.length <= 75) {
            int j = i;
            outer: for (; i < ret.length; i++) {
              reti = ret[i];
              rep = reti.getReplacement();
              if (rep == key) {
                continue;
              }
              for (int k = 0; k < j; k++) {
                if (rep == ret[k]) {
                  continue outer;
                }
              }
              ret[j++] = rep;
            }
            Node[] newArray = new Node[j];
            System.arraycopy(ret, 0, newArray, 0, j);
            ret = newArray;
            m.put(key, Sets.newHashSet((N[]) newArray));
          } else {
            s = new HashSet<>(ret.length * 2);
            for (int j = 0; j < i; j++) {
              s.add(ret[j]);
            }
            for (int j = i; j < ret.length; j++) {
              rep = ret[j].getReplacement();
              if (rep != key) {
                s.add(rep);
              }
            }
            ret = s.toArray(EMPTY_NODE_ARRAY);
            m.put(key, (Set<N>) s);
          }
          break;
        }
      }
    }
    return ret;
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

  public TypeHierarchy getTypeHierarchy(){
    if(typeHierarchy==null){
      throw new RuntimeException("Can't use TypeHierarchy when \"ignore-types\" is set to true");
    }
    return typeHierarchy;
  }

  public SparkOptions getSparkOptions(){
    return sparkOptions;
  }

}
