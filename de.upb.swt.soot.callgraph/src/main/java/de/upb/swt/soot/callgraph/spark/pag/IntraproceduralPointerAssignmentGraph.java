package de.upb.swt.soot.callgraph.spark.pag;

import com.sun.javafx.geom.Edge;
import de.upb.swt.soot.callgraph.spark.pag.nodes.Node;
import de.upb.swt.soot.core.model.SootMethod;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import sun.security.provider.certpath.Vertex;

import javax.annotation.Nonnull;

public class IntraproceduralPointerAssignmentGraph {

    private DefaultDirectedGraph<SparkVertex, SparkEdge> graph;

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

    public DefaultDirectedGraph<SparkVertex, SparkEdge> getGraph(){
        return graph;
    }

}
