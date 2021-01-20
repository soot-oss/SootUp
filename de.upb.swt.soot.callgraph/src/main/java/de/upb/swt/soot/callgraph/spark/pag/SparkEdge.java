package de.upb.swt.soot.callgraph.spark.pag;

import org.jgrapht.graph.DefaultEdge;

public class SparkEdge extends DefaultEdge {
    private EdgeType edgeType;

    public SparkEdge(EdgeType edgeType){
        this.edgeType = edgeType;
    }

    public EdgeType getEdgeType(){
        return edgeType;
    }

    @Override
    public String toString(){
        return "(" + getSource() + " : " + getTarget() + " : " + edgeType + ")";
    }

}