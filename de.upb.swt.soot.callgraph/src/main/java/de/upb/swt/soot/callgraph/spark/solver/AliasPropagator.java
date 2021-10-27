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
  private final Set<VariableNode> workList = new TreeSet<>();

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
      handleAllocionNode(alNode);


    }
  }


  protected boolean handleAllocionNode(AllocationNode source){
    boolean ret = false;
    Set<VariableNode> targets =  pag.allocLookup(source);
    for(VariableNode target : targets){
      if(target.getReplacement() != target){

        throw new RuntimeException("The node " + target + " has been merged to " + target.getReplacement() + "! Can not be added into workList!");
      }
      workList.add(target);
    }
    return ret;
  }
}
