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
import de.upb.swt.soot.callgraph.spark.pag.nodes.*;
import de.upb.swt.soot.core.model.Field;
import de.upb.swt.soot.core.types.Type;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Propagates points-to sets along pointer assignment graph using iteration.
 */
public class IterationPropagator implements Propagator {
    private PointerAssignmentGraph pag;

    public IterationPropagator(PointerAssignmentGraph pag){
        this.pag =pag;
    }

    @Override
    public void propagate() {
        new TopologicalSorter(pag, false).sort();

        handleAllocationNodeSources();

        boolean change;
        do{
            change = false;
            TreeSet<VariableNode> simpleSources = new TreeSet<>(pag.getSimpleEdges().keySet());
            for (VariableNode source : simpleSources) {
                change |= handleSimples(source);
            }

            Set<FieldReferenceNode> loadSources = pag.getLoadEdges().keySet();
            for (FieldReferenceNode source : loadSources) {
                change |= handleLoads(source);
            }
            Set<VariableNode> storeSources = pag.getStoreEdges().keySet();
            for(VariableNode source: storeSources){
                change |= handleStores(source);
            }
            Set<NewInstanceNode> assignInstanceSources = pag.getAssignInstanceEdges().keySet();
            for (NewInstanceNode source : assignInstanceSources) {
                handleNewInstances(source); // always returns false in old soot
            }
        } while(change);
    }



    private void handleAllocationNodeSources(){
        Map<AllocationNode, Set<VariableNode>> allocationEdges = pag.getAllocationEdges();
        for (Map.Entry<AllocationNode, Set<VariableNode>> entry : allocationEdges.entrySet()) {
            AllocationNode source = entry.getKey();
            Set<VariableNode> targets = entry.getValue();
            for (VariableNode target : targets) {
                target.getOrCreatePointsToSet().add(source);
            }
        }
    }


    private boolean handleSimples(VariableNode source){
        boolean ret = false;
        Set<Node> srcSet = source.getPointsToSet();
        if(srcSet.isEmpty()){
            return false;
        }
        Map<VariableNode, Set<VariableNode>> simpleEdges = pag.getSimpleEdges();
        Set<VariableNode> simpleTargets = simpleEdges.get(source);
        for (VariableNode element : simpleTargets) {
            ret |= element.getOrCreatePointsToSet().addAll(srcSet);
        }
        Map<VariableNode, Set<NewInstanceNode>> newInstanceEdges = pag.getNewInstanceEdges();
        Set<NewInstanceNode> newInstances = newInstanceEdges.get(source);
        for (NewInstanceNode element : newInstances) {
            ret |= element.getOrCreatePointsToSet().addAll(srcSet);
        }
        return ret;
    }

    private boolean handleLoads(FieldReferenceNode source){
        boolean ret = false;
        final Set<VariableNode> loadTargets = pag.loadLookup(source);
        if(loadTargets==null || loadTargets.isEmpty()){
            return false;
        }
        Field field = source.getField();
        Set<Node> basePointsToSet = source.getBase().getPointsToSet();
        for (Node node : basePointsToSet) {
            AllocationDotField allocDotField =
                    ((AllocationNode) node).dot(field);
            if(allocDotField==null){
                continue;
            }
            Set<Node> set = allocDotField.getPointsToSet();
            if(set.isEmpty()){
                continue;
            }
            for(VariableNode target: loadTargets){
                if(target.getOrCreatePointsToSet().addAll(set)){
                    ret|=true;
                }
            }
        }
        return ret;
    }

    private boolean handleStores(VariableNode source){
        boolean ret = false;
        Set<Node> srcSet = source.getPointsToSet();
        if(srcSet.isEmpty()){
            return false;
        }
        Set<FieldReferenceNode> storeTargets = pag.getStoreEdges().get(source);
        for(FieldReferenceNode fr: storeTargets){
            Field field = fr.getField();
            Set<Node> basePointsToSet = fr.getBase().getPointsToSet();
            for (Node node : basePointsToSet) {
                AllocationDotField allocationDotField = pag.getOrCreateAllocationDotField((AllocationNode) node, field);
                if(allocationDotField.getOrCreatePointsToSet().addAll(srcSet)){
                    ret|=true;
                }
            }
        }
        return ret;
    }

    private void handleNewInstances(NewInstanceNode source) {
        Set<VariableNode> newInstances = pag.getAssignInstanceEdges().get(source);
        for (VariableNode instance : newInstances) {
            Set<Node> srcSet = source.getPointsToSet();
            for (Node node : srcSet) {
                if(node instanceof ClassConstantNode){
                    ClassConstantNode ccn = (ClassConstantNode) node;
                    Type ccnType = ccn.getClassConstant().getType();
                    instance.getOrCreatePointsToSet().add(pag.getOrCreateAllocationNode(source.getValue(), ccnType, ccn.getMethod()));
                }
            }
        }

    }

}














