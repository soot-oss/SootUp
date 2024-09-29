package sootup.analysis.intraprocedural;

import sootup.core.graph.BasicBlock;
import sootup.core.graph.StmtGraph;

public abstract class BackwardFlowAnalysis<A> extends FlowAnalysis<A> {

  /** Construct the analysis from StmtGraph. */
  public <B extends BasicBlock<B>> BackwardFlowAnalysis(StmtGraph<B> graph) {
    super(graph);
  }

  @Override
  protected boolean isForward() {
    return false;
  }

  @Override
  protected void execute() {
    int i = execute(stmtToAfterFlow, stmtToBeforeFlow);
  }
}
