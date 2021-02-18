package de.upb.swt.soot.callgraph.spark.solver;

import de.upb.swt.soot.callgraph.spark.pag.PointerAssignmentGraph;

public class SCCCollapser {
    private PointerAssignmentGraph pag;

    public SCCCollapser(PointerAssignmentGraph pag){
        this.pag = pag;
    }

    public void collapse(){
    }

}
