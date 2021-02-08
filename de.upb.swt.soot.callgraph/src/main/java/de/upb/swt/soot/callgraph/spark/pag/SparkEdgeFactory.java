package de.upb.swt.soot.callgraph.spark.pag;

import de.upb.swt.soot.callgraph.spark.pag.nodes.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class SparkEdgeFactory {

    public SparkEdge getEdge(Node source, Node target){
        if(source == null || target==null){
            throw new RuntimeException("Cannot get edge for null nodes");
        }

        if(source instanceof VariableNode){
            if(target instanceof VariableNode){
                return new SparkEdge(new SparkVertex(source), new SparkVertex(target), EdgeType.ASSIGNMENT_EDGE);
            } else if(target instanceof FieldReferenceNode){
                return new SparkEdge(new SparkVertex(source), new SparkVertex(target), EdgeType.STORE_EDGE);
            } else if(target instanceof NewInstanceNode){
                //TODO: NewInstanceEdge
                throw new NotImplementedException();
            } else {
                throw new RuntimeException("Invalid node type:" + target);
            }
        } else if(source instanceof FieldReferenceNode){
            return new SparkEdge(new SparkVertex(source), new SparkVertex(target), EdgeType.LOAD_EDGE);
        } else if(source instanceof NewInstanceNode){
            // TODO: assignInstanceEdge
            throw new NotImplementedException();
        } else {
            return new SparkEdge(new SparkVertex(source), new SparkVertex(target), EdgeType.ALLOCATION_EDGE);
        }
    }


}
