package de.upb.swt.soot.callgraph.spark.solver;

import de.upb.swt.soot.callgraph.spark.pag.PointerAssignmentGraph;

public class CyclePropagator implements Propagator {
    private PointerAssignmentGraph pag;

    public CyclePropagator(PointerAssignmentGraph pag){
        this.pag = pag;
    }

    @Override
    public void propagate() {

    }
}
