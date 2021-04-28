package de.upb.swt.soot.callgraph.spark.solver;

import de.upb.swt.soot.callgraph.spark.pag.PointerAssignmentGraph;
import de.upb.swt.soot.callgraph.spark.pag.nodes.*;
import de.upb.swt.soot.core.model.Field;

import java.util.Set;
import java.util.TreeSet;

/**
 * Propagates points-to sets along pointer assignment graph using a merging of field reference (Red) nodes to improve scalability.
 */
public class MergePropagator implements Propagator{

    private PointerAssignmentGraph pag;
    private final Set<VariableNode> varNodeWorkList = new TreeSet<>();

    public MergePropagator(PointerAssignmentGraph pag){
        this.pag = pag;
    }

    @Override
    public void propagate() {
        new TopologicalSorter(pag, false).sort();
        for(AllocationNode source: pag.getAllocationEdges().keySet()){
            handleAllocNode((AllocationNode) source);
        }

        do{
            int iter = 0;
            while (!varNodeWorkList.isEmpty()){
                VariableNode src = varNodeWorkList.iterator().next();
                varNodeWorkList.remove(src);
                handleVariableNode(src);
            }

            for(VariableNode source :pag.getStoreEdges().keySet()){
                Set<FieldReferenceNode> storeTargets = pag.getStoreEdges().get(source);
                for (FieldReferenceNode fr : storeTargets) {
                    fr.getOrCreatePointsToSet().addAll(source.getPointsToSet());
                }
            }

            for(FieldReferenceNode source: pag.getLoadEdges().keySet()){
                if(source!=source.getReplacement()){
                    throw new RuntimeException("load source must be equal to its replacement");
                }
                Set<VariableNode> targets = pag.getLoadEdges().get(source);
                for (VariableNode target : targets) {
                    if(target.getOrCreatePointsToSet().addAll(source.getPointsToSet())){
                        varNodeWorkList.add(target);
                    }
                }
            }

        }while (!varNodeWorkList.isEmpty());

    }

    private void handleAllocNode(AllocationNode source) {
        Set<VariableNode> targets = pag.allocLookup(source);
        for (VariableNode target : targets) {
            if(target.getOrCreatePointsToSet().add(source)){
                varNodeWorkList.add(target);
            }
        }
    }

    private void handleVariableNode(VariableNode src) {
        if(src.getReplacement()!=src){
            return;
        }
        Set<Node> srcPointsToSet = src.getPointsToSet();
        if(srcPointsToSet.isEmpty()){
            return;
        }
        Set<VariableNode> simpleTargets = pag.getSimpleEdges().get(src);
        for (VariableNode element : simpleTargets) {
            if(element.getOrCreatePointsToSet().addAll(srcPointsToSet)){
                varNodeWorkList.add(element);
            }
        }

        Set<FieldReferenceNode> storeTargets = pag.getStoreEdges().get(src);
        for (FieldReferenceNode element : storeTargets) {
            element.getOrCreatePointsToSet().addAll(srcPointsToSet);
        }

        for(final FieldReferenceNode fr: src.getAllFieldReferences()){
            final Field field = fr.getField();
            for (Node node : srcPointsToSet) {
                AllocationDotField allocationDotField = pag.getOrCreateAllocationDotField((AllocationNode) node, field);
                Node replacement = allocationDotField.getReplacement();
                if(replacement != fr){
                    fr.mergeWith(replacement);
                }
            }
        }
    }
}
