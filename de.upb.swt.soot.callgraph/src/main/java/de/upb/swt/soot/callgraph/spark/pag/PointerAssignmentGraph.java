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
import de.upb.swt.soot.callgraph.CallGraph;
import de.upb.swt.soot.callgraph.spark.Spark;
import de.upb.swt.soot.callgraph.spark.builder.GlobalNodeFactory;
import de.upb.swt.soot.callgraph.spark.pag.nodes.*;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.ClassConstant;
import de.upb.swt.soot.core.jimple.common.expr.JNewExpr;
import de.upb.swt.soot.core.model.Field;
import de.upb.swt.soot.core.model.Method;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.views.View;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private final DefaultDirectedGraph<SparkVertex, SparkEdge> graph;
  private CallGraph callGraph;
  private View<? extends SootClass> view;

  private final Map<Local, LocalVariableNode> localToNodeMap = new HashMap<>();
  private final Map<Object, LocalVariableNode> valToLocalVariableNode = new HashMap<>();
  private final Map<Object, AllocationNode> valToAllocationNode = new HashMap<>();
  private final Queue<AllocationNode> newAllocationNodes = new ArrayDeque<>();
  private final Table<Object, Type, AllocationNode> valToReflectiveAllocationNode = HashBasedTable.create();
  private final Map<Object, GlobalVariableNode> valToGlobalVariableNode = new HashMap<>();
  private final Map<Value, NewInstanceNode> valToNewInstanceNode = new HashMap<>();
  private final GlobalNodeFactory nodeFactory = new GlobalNodeFactory(this);
  private final SparkEdgeFactory edgeFactory = new SparkEdgeFactory();
  private final List<VariableNode> dereferences = new ArrayList<>();

  public PointerAssignmentGraph(View view, CallGraph callGraph) {
    this.view = view;
    this.callGraph = callGraph;
    this.graph = new DefaultDirectedGraph<>(null, null, false);
    build();
  }

  private void build() {
    for (SootClass clazz : view.getClasses()) {
      for (Method method : clazz.getMethods()) {
        SootMethod sootMethod = (SootMethod) method;
        if (!sootMethod.isAbstract() && callGraph.containsMethod(sootMethod.getSignature())) {
          IntraproceduralPointerAssignmentGraph intraPAG =
              new IntraproceduralPointerAssignmentGraph(this, sootMethod);
          addIntraproceduralPointerAssignmentGraph(intraPAG);
        }
      }
    }
  }

  public void addEdge(Node source, Node target) {
    SparkEdge edge = edgeFactory.getEdge(source, target);
    SparkVertex src = new SparkVertex(source);
    SparkVertex trg = new SparkVertex(target);
    graph.addVertex(src);
    graph.addVertex(trg);
    graph.addEdge(src, trg, edge);
    log.info("Added {} edge from:{} to:{}", edge.getEdgeType(), source, target);
  }

  private void addIntraproceduralPointerAssignmentGraph(
      IntraproceduralPointerAssignmentGraph intraPAG) {
    DefaultDirectedGraph<SparkVertex, SparkEdge> intraGraph = intraPAG.getGraph();
    System.out.println("method:" + intraPAG.getMethod());
    //printGraph(intraGraph);
    Iterator<SparkVertex> iter = new DepthFirstIterator<>(intraGraph);
    // TODO: intraPAG isn't actually a graph?
    while(iter.hasNext()){
      SparkVertex source = iter.next();
      SparkVertex target = iter.next();
      addEdge(source.node, target.node);
    }
  }

  private void printGraph(DefaultDirectedGraph<SparkVertex, SparkEdge> graph){
    Iterator<SparkVertex> iter = new DepthFirstIterator<>(graph);
    while(iter.hasNext()){
      SparkVertex vertex = iter.next();
      System.out.println(vertex.node);
    }
  }

  public View getView() {
    return view;
  }

  public LocalVariableNode getOrCreateLocalVariableNode(Object value, Type type, SootMethod method){
    //TODO: SPARK_OPTS RTA
    if(value instanceof Local){
      Local local = (Local) value;
      // TODO: numbering?
      LocalVariableNode localVariableNode = localToNodeMap.get(local);
      if(localVariableNode == null){
        localVariableNode = new LocalVariableNode(value, type, method);
        localToNodeMap.put(local, localVariableNode);
        //TODO: addNodeTag()
      } else if(!(localVariableNode.getType().equals(type))){
        throw new RuntimeException("Value " + value + " of type " + type + " previously had type " + localVariableNode.getType());
      }
      return localVariableNode;
    }
    LocalVariableNode localVariableNode = valToLocalVariableNode.get(value);
    if(localVariableNode == null){
      localVariableNode = new LocalVariableNode(value, type, method);
      valToLocalVariableNode.put(value, localVariableNode);
      //TODO: addNodeTag()
    } else if(!(localVariableNode.getType().equals(type))){
      throw new RuntimeException("Value " + value + " of type " + type + " previously had type " + localVariableNode.getType());
    }
    return localVariableNode;
  }


  /**
   * Finds or creates the FieldRefNode for base variable base and field field, of type type.
   */
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

  private void addNodeTag(Node node, SootMethod m) {
    // TODO: disabled by default
  }

  public AllocationNode getOrCreateAllocationNode(Object newExpr, Type type, SootMethod method){
    // TODO: SPARK_OPT types-for-sites
    AllocationNode node;
    if(newExpr instanceof JNewExpr){
      node =  valToAllocationNode.get(newExpr);
      if(node == null){
        node = new AllocationNode(type, (JNewExpr) newExpr, method);
        valToAllocationNode.put(newExpr, node);
        newAllocationNodes.add(node);
        addNodeTag(node, null);
      } else if(!node.getType().equals(type)){
        throw new RuntimeException("NewExpr " + newExpr + " of type " + type + " previously had type " + node.getType());
      }
    } else {
      node = valToReflectiveAllocationNode.get(newExpr, type);
      if(node == null){
        node = new AllocationNode(type, newExpr, method);
        valToReflectiveAllocationNode.put(newExpr, type, node);
        newAllocationNodes.add(node);
        addNodeTag(node, method);
      }
    }
    return node;
  }

  public GlobalVariableNode getOrCreateGlobalVariableNode(Object value, Type type){
    // TODO: SPARK_OPTS rta

    GlobalVariableNode node = valToGlobalVariableNode.get(value);
    if(node==null){
      node = new GlobalVariableNode(value,type);
      addNodeTag(node, null);
    } else if(!node.getType().equals(type)){
      throw new RuntimeException("Value " + value + " of type " + type + " previously had type " + node.getType());
    }
    return node;
  }

  public FieldReferenceNode getOrCreateLocalFieldReferenceNode(Object baseValue, Type baseType, Field field, SootMethod method){
    VariableNode base = getOrCreateLocalVariableNode(baseValue, baseType, method);
    FieldReferenceNode node = getOrCreateFieldReferenceNode(base, field);
    //TODO: SPARK_OPTS library mode
    return node;
  }

  public AllocationNode getOrCreateClassConstantNode(ClassConstant cc){
    // TODO: SPARK_OPT types-for-sites vta
    ClassConstantNode node = (ClassConstantNode) valToAllocationNode.get(cc);
    if(node == null){
      node = new ClassConstantNode(cc);
      valToAllocationNode.put(cc, node);
      newAllocationNodes.add(node);
      addNodeTag(node, null);
    }
    return node;
  }

  public NewInstanceNode getOrCreateNewInstanceNode(Value value, Type type, SootMethod method){
    NewInstanceNode node = valToNewInstanceNode.get(value);
    if(node == null){
      node = new NewInstanceNode(type, value);
      valToNewInstanceNode.put(value, node);
      addNodeTag(node, method);
    }
    return node;
  }

  /**
   * Adds the base of a dereference to the list of dereferenced variables.
   */
  public void addDereference(VariableNode base) {
    dereferences.add(base);
  }

  public GlobalNodeFactory getNodeFactory(){
    return nodeFactory;
  }


}
