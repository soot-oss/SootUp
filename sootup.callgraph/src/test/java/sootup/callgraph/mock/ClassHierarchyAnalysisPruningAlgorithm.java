package sootup.callgraph.mock;

import org.jspecify.annotations.NonNull;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.model.SootMethod;
import sootup.core.views.View;

public class ClassHierarchyAnalysisPruningAlgorithm extends ClassHierarchyAnalysisAlgorithm {
  /**
   * The constructor of the CHA algorithm.
   *
   * @param view it contains the data of the classes and methods
   */
  public ClassHierarchyAnalysisPruningAlgorithm(@NonNull View view) {
    super(view);
  }

  @Override
  protected boolean includeCall(@NonNull SootMethod method, @NonNull InvokableStmt statement) {
    String methodName = method.getName();
    return !methodName.equals("methodB");
  }
}
