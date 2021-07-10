package de.upb.swt.soot.java.core.analysis;

import de.upb.swt.soot.core.graph.StmtGraph;

/**
 * Abstract class that provides the fixed point iteration functionality required by all ForwardFlowAnalyses.
 */
public abstract class ForwardFlowAnalysis<A> extends FlowAnalysis<A> {

    /**
     * Construct the analysis from a DirectedGraph representation of a Body.
     */
    public ForwardFlowAnalysis(StmtGraph graph) {
        super(graph);
    }

    @Override
    protected boolean isForward() {
        return true;
    }

    @Override
    protected void doAnalysis() {
        super.doAnalysis(GraphView.FORWARD, stmtToBeforeFlow, stmtToAfterFlow);
    }
}
