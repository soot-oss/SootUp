package de.upb.swt.soot.callgraph.spark.pag;

import de.upb.swt.soot.callgraph.spark.pag.nodes.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InternalEdges {

    /**
     * n2=n1: an edge from n1 to n2 indicates that n1 is added to the points-to set of n2
      */
    protected Map<VariableNode, Set<Node>> simpleEdges = new HashMap<>();
    protected Map<FieldReferenceNode, Set<Node>> loadEdges = new HashMap<>();
    protected Map<VariableNode, Set<Node>> storeEdges = new HashMap<>();
    protected Map<AllocationNode, Set<Node>> allocationEdges = new HashMap<>();
    protected Map<VariableNode, Set<Node>> newInstanceEdges = new HashMap<>();
    protected Map<NewInstanceNode, Set<Node>> assignInstanceEdges = new HashMap<>();

    // TODO: SPARK_OPT simple-edges-bidirectional
    // TODO: Inv edges

    public void addEdge(Node source, Node target){
        // TODO: getReplacements
        if (source == null || target == null) {
            throw new RuntimeException("Cannot get edge for null nodes");
        }
        if (source instanceof VariableNode) {
            if (target instanceof VariableNode) {
                addSimpleEdge((VariableNode) source, (VariableNode) target);
            } else if (target instanceof FieldReferenceNode) {
                addStoreEdge((VariableNode) source, (FieldReferenceNode) target);
            } else if (target instanceof NewInstanceNode) {
                addNewInstanceEdge((VariableNode) source, (NewInstanceNode) target);
            } else {
                throw new RuntimeException("Invalid node type:" + target);
            }
        } else if (source instanceof FieldReferenceNode) {
            addLoadEdge((FieldReferenceNode) source, (VariableNode) target);
        } else if (source instanceof NewInstanceNode) {
            addAssignInstanceEdge((NewInstanceNode) source, (VariableNode) target);
        } else {
            addAllocationEdge((AllocationNode) source, (VariableNode) target);
        }
    }

    public boolean addSimpleEdge(VariableNode source, VariableNode target){
        return simpleEdges.computeIfAbsent(source, v->new HashSet<>()).add(target);
    }

    public boolean addStoreEdge(VariableNode source, FieldReferenceNode target){
        return storeEdges.computeIfAbsent(source, v->new HashSet<>()).add(target);
    }

    public boolean addLoadEdge(FieldReferenceNode source, VariableNode target){
        return loadEdges.computeIfAbsent(source, v->new HashSet<>()).add(target);
    }

    public boolean addAllocationEdge(AllocationNode source, VariableNode target){
        return allocationEdges.computeIfAbsent(source, v->new HashSet<>()).add(target);
    }

    public boolean addNewInstanceEdge(VariableNode source, NewInstanceNode target){
        return newInstanceEdges.computeIfAbsent(source, v->new HashSet<>()).add(target);
    }

    public boolean addAssignInstanceEdge(NewInstanceNode source, VariableNode target){
        return assignInstanceEdges.computeIfAbsent(source, v->new HashSet<>()).add(target);
    }


}
