package sootup.java.core.interceptors.reachingdefs;

import sootup.core.graph.BasicBlock;
import sootup.core.graph.StmtGraph;

public abstract class ForwardFlowAnalysis<A> extends FlowAnalysis<A> {

  /** Construct the analysis from StmtGraph. */
  public <B extends BasicBlock<B>> ForwardFlowAnalysis(StmtGraph<B> graph) {
    super(graph);
  }

  @Override
  protected boolean isForward() {
    return true;
  }

  @Override
  protected void execute() {
    int i = execute(stmtToBeforeFlow, stmtToAfterFlow);
  }
}
