package de.upb.swt.soot.callgraph.spark.pag;

import de.upb.swt.soot.callgraph.spark.pag.nodes.LocalVariableNode;
import de.upb.swt.soot.callgraph.spark.pag.nodes.Node;

public class SparkEdgeFactory {

    public SparkEdge getEdge(Node source, Node target){
        if(source == null || target==null){
            throw new RuntimeException("Cannot get edge for null nodes");
        }

        if(source instanceof LocalVariableNode && target instanceof LocalVariableNode){
            return new SparkEdge(EdgeType.ASSIGNMENT_EDGE);
        }

        throw new RuntimeException("Unknown edge");
    }


}
