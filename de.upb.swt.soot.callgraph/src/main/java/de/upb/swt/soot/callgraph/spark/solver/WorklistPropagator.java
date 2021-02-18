package de.upb.swt.soot.callgraph.spark.solver;

import de.upb.swt.soot.callgraph.spark.pag.PointerAssignmentGraph;
import de.upb.swt.soot.callgraph.spark.pag.SparkEdge;
import de.upb.swt.soot.callgraph.spark.pag.SparkVertex;
import de.upb.swt.soot.callgraph.spark.pag.nodes.*;
import de.upb.swt.soot.core.model.Field;
import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.*;

/**
 * Propagates points-to sets along pointer assignment graph using a worklist.
 */
public class WorklistPropagator implements Propagator {
    protected final Set<VariableNode> variableNodeWorkList = new TreeSet<VariableNode>();
    private PointerAssignmentGraph pag;

    public WorklistPropagator(PointerAssignmentGraph pag){
        this.pag = pag;
    }

    @Override
    public void propagate() {
        // handle Allocation Nodes
        handleAllocationNodeSources();



    }

    /**
     * adds AllocationNode sources to their targets' points-to sets
     * if a source is added for the first time adds it to worklist
     */
    private void handleAllocationNodeSources(){
        Map<AllocationNode, Set<VariableNode>> allocationEdges = pag.getAllocationEdges();
        for (Map.Entry<AllocationNode, Set<VariableNode>> entry : allocationEdges.entrySet()) {
            AllocationNode source = entry.getKey();
            Set<VariableNode> targets = entry.getValue();
            for(VariableNode target: targets){
                if(target.getOrCreatePointsToSet().add(source)){
                    variableNodeWorkList.add(target);
                }
            }
        }
    }

    private void handleVariableNodeSources(){
        Iterator<VariableNode> iter = variableNodeWorkList.iterator();
        while(iter.hasNext()){
            VariableNode source = iter.next();
            variableNodeWorkList.remove(source);
            handleVariableNodeSource(source);
        }
    }

    private void handleVariableNodeSource(final VariableNode source){
        if(source.getReplacement() != source){
            throw new RuntimeException("Got bad node " + source + " with rep " + source.getReplacement());
        }

        if(source.getPointsToSet().isEmpty()){
            return;
        }

        handleSimpleEdges(source);
        handleStoreEdges(source);
        final HashSet<Pair<Node, Node>> storesToPropagate = new HashSet<>();
        final HashSet<Pair<Node, Node>> loadsToPropagate = new HashSet<>();

        // TODO: SPARK_OPTS ofcg

    }

    private void handleSimpleEdges(final VariableNode source) {
        Set<VariableNode> targets = pag.getSimpleEdges().get(source);
        for(VariableNode target: targets){
            if(target.getOrCreatePointsToSet().addAll(source.getPointsToSet())){
                variableNodeWorkList.add(target);
            }
        }
    }

    private void handleStoreEdges(final VariableNode source) {
        Set<FieldReferenceNode> targets = pag.getStoreEdges().get(source);
        for(FieldReferenceNode target: targets){
            final Field field = target.getField();
            Set<Node> basePointsToSet = target.getBase().getPointsToSet();
            for (Node node : basePointsToSet) {
                AllocationDotField allocDotField = pag.getOrCreateAllocationDotField((AllocationNode) node, field);
                allocDotField.getPointsToSet().addAll(source.getPointsToSet());
            }
        }
    }

    private void handleStoresAndLoadsToPropagate(final VariableNode source,
                                                 final HashSet<Pair<Node, Node>> storesToPropagate,
                                                 final HashSet<Pair<Node, Node>> loadsToPropagate){
        for(final FieldReferenceNode fieldRef: source.getAllFieldReferences()){
            final Field field = fieldRef.getField();
        }
    }

}
