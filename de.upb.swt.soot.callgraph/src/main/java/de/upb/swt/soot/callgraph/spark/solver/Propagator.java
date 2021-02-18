package de.upb.swt.soot.callgraph.spark.solver;

/**
 * Propagator that propagates points-to sets along pointer assignment graph.
 */
public interface Propagator {
    void propagate();
}
