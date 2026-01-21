package sootup.callgraph.mock;

import org.jspecify.annotations.NonNull;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.model.SootMethod;
import sootup.core.views.View;

/**
 * This class implements a modified Rapid Type Analysis call graph algorithm, in which the <code>
 * includeCall</code> method is overridden to test pruning. The corresponding tests can be found in
 * <code>CallGraphTestBase</code> and <code>RapidTypeAnalysisPruningAlgorithmTest</code>.
 */
public class RapidTypeAnalysisPruningAlgorithm extends RapidTypeAnalysisAlgorithm {

  /**
   * The constructor of the RTA algorithm.
   *
   * @param view it contains the data of the classes and methods
   */
  public RapidTypeAnalysisPruningAlgorithm(@NonNull View view) {
    super(view);
  }

  @Override
  protected boolean includeCall(@NonNull SootMethod method, @NonNull InvokableStmt statement) {
    String methodName = method.getName();
    return !methodName.equals("methodB");
  }
}
