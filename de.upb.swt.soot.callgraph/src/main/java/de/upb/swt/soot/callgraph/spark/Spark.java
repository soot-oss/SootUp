package de.upb.swt.soot.callgraph.spark;

import de.upb.swt.soot.callgraph.CallGraph;
import de.upb.swt.soot.callgraph.spark.pag.PointerAssignmentGraph;
import de.upb.swt.soot.callgraph.spark.pointsto.PointsToAnalysis;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.views.View;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class Spark {
    private View view;
    private CallGraph callGraph;

    private PointerAssignmentGraph pag;

    private PointsToAnalysis analysis;

    public Spark(View view, CallGraph callGraph){
        this.view = view;
        this.callGraph = callGraph;
    }

    public void analyze() {
        // Build PAG
        buildPointerAssignmentGraph();
        // Simplify

        // Propagate
    }

    private void buildPointerAssignmentGraph(){
        pag = new PointerAssignmentGraph(view, callGraph);
    }

}
