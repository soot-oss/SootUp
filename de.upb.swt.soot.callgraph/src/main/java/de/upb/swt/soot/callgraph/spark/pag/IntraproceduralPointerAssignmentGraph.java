package de.upb.swt.soot.callgraph.spark.pag;

import de.upb.swt.soot.callgraph.spark.pag.nodes.Node;
import de.upb.swt.soot.core.model.SootMethod;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import javax.annotation.Nonnull;

public class IntraproceduralPointerAssignmentGraph {

    private static class Vertex {
        @Nonnull
        final Node node;

        private Vertex(@Nonnull Node node) {
            this.node = node;
        }
    }

    private static class Edge extends DefaultEdge {
        private EdgeType edgeType;

        public Edge(EdgeType edgeType){
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

    private DefaultDirectedGraph<Vertex, Edge> graph;

    private SootMethod method;

    public IntraproceduralPointerAssignmentGraph(SootMethod method){
        // TODO: need chaching?
        this.method = method;
    }

    public void build(){
        if(method.isConcrete()){

        } else {
            // TODO: build for native
        }
    }

    public DefaultDirectedGraph<Vertex, Edge> getGraph(){
        return graph;
    }

}
