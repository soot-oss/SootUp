package de.upb.swt.soot.java.core.analysis;

import de.upb.swt.soot.core.graph.StmtGraph;

/**
 * Abstract class that provides the fixed point iteration functionality required by all BackwardFlowAnalyses.
 */
public abstract class BackwardFlowAnalysis<A> extends FlowAnalysis<A> {
    /**
     * Construct the analysis from a DirectedGraph representation of a Body.
     */
    protected BackwardFlowAnalysis(StmtGraph graph) {
        super(graph);
    }

    /**
     * Returns <code>false</code>
     *
     * @return false
     **/
    @Override
    protected boolean isForward() {
        return false;
    }

    @Override
    protected void doAnalysis() {
        super.doAnalysis(GraphView.BACKWARD, stmtToAfterFlow, stmtToBeforeFlow);
    }
}
