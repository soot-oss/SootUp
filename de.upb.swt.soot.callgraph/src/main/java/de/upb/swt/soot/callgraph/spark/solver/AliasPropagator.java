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
import de.upb.swt.soot.callgraph.spark.pag.nodes.AllocationNode;
import de.upb.swt.soot.callgraph.spark.pag.nodes.FieldReferenceNode;
import de.upb.swt.soot.callgraph.spark.pag.nodes.Node;
import de.upb.swt.soot.callgraph.spark.pag.nodes.VariableNode;
import de.upb.swt.soot.core.model.Field;

import java.util.*;

/** Propagates points-to sets along pointer assignment graph using a relevant aliases. */
public class AliasPropagator implements Propagator {
  private PointerAssignmentGraph pag;
  private Map<FieldReferenceNode, Set<Node>> loadSets = new HashMap<>();
  private Map<Field, Set<VariableNode>> fieldToBase = new HashMap<>();

  private final Set<VariableNode> varNodeWorkList = new HashSet<>();
  private Set<VariableNode> aliasWorkList = new HashSet<>();
  private Set<FieldReferenceNode> fieldRefWorkList = new HashSet<>();

  //todo: a field OnFlyCallGraph ofcg


  public AliasPropagator(PointerAssignmentGraph pag) {
    this.pag = pag;
  }

  @Override
  public void propagate() {
    //todo: ofcg = pag.getOnFlyCallGraph;
    new TopologicalSorter(pag, false).sort();
    //collect all FieldReferenceNodes' (field, set of bases) pairs
    for(FieldReferenceNode frNode : pag.getLoadEdges().keySet()){
      if(!fieldToBase.containsKey(frNode)){
        fieldToBase.put(frNode.getField(), new HashSet<>());
      }
      fieldToBase.get(frNode.getField()).add(frNode.getBase());
    }

    for(FieldReferenceNode frNode : pag.getStoreEdgesInv().keySet()){
      if(!fieldToBase.containsKey(frNode)){
        fieldToBase.put(frNode.getField(), new HashSet<>());
      }
      fieldToBase.get(frNode.getField()).add(frNode.getBase());
    }

    //process all allocation nodes
    for(AllocationNode alNode: pag.getAllocationEdges().keySet()){
      handleAllocaionNode(alNode);
    }

    do{

      handleVarNodeWorkList();

      handleAliasWorkList();
      handleFieldRefWorkList();
    }while(!varNodeWorkList.isEmpty());


  }



  protected void handleVarNodeWorkList(){
    aliasWorkList = new HashSet<>();
    while(!varNodeWorkList.isEmpty()){
      VariableNode source = varNodeWorkList.iterator().next();
      varNodeWorkList.remove(source);
      aliasWorkList.add(source);
      handleVarNode(source);
    }
  }

  protected void handleAliasWorkList(){
    for(VariableNode source : aliasWorkList){
      for(FieldReferenceNode fr : source.getAllFieldReferences()){

      }
    }
  }

  protected void handleFieldRefWorkList(){

  }

  /**
   * Propagates new points-to information of AllocationNode source to all its successors.
   */
  protected void handleAllocaionNode(AllocationNode source){

    Set<VariableNode> targets =  pag.allocLookup(source);
    for(VariableNode target : targets){
      Set<Node> p2Set = target.getOrCreatePointsToSet();
      if(p2Set.add(source)){
        varNodeWorkList.add(target);
      }
    }
  }
  /**
   * Propagates new points-to information of VariableNode source to all its variable node successors
   */
  protected void handleVarNode(VariableNode source){
    if(source.getReplacement() != source){
      throw new RuntimeException("The variableNode " + source + " has been merged to another variable node " + source.getReplacement());
    }
    Set<Node> p2SetOfSource = source.getPointsToSet();

    //Todo: Lack of OnFlyCallGraph Part

    Set<VariableNode> varTargets = pag.simpleLookup(source);
    for(VariableNode varTarget : varTargets){
      Set<Node> p2SetOfTarget = varTarget.getOrCreatePointsToSet();
      if(p2SetOfTarget.addAll(p2SetOfSource)){
        varNodeWorkList.add(varTarget);
      }
    }

    //todo: how to distinguish q.f_in and q.f_out????
    Set<FieldReferenceNode> fieldTargets = pag.storeLookup(source);
    for(FieldReferenceNode fieldTarget: fieldTargets){
      Set<Node> p2SetOfTarget = fieldTarget.getOrCreatePointsToSet();
      if(p2SetOfTarget.addAll(p2SetOfSource)){
        fieldRefWorkList.add(fieldTarget);
      }
    }
  }
}
