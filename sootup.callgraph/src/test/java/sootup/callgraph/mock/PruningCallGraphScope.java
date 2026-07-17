package sootup.callgraph.mock;

import org.jspecify.annotations.NonNull;
import sootup.callgraph.scope.DefaultCallGraphScope;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.model.SootMethod;
import sootup.core.views.View;

/**
 * A {@link sootup.callgraph.scope.CallGraphScope} used to test edge pruning: in addition to the
 * default library-class exclusion, it excludes every call originating from a method named <code>
 * methodB</code>.
 */
public class PruningCallGraphScope extends DefaultCallGraphScope {
  public PruningCallGraphScope(@NonNull View view) {
    super(view);
  }

  @Override
  public boolean includeCall(@NonNull SootMethod method, @NonNull InvokableStmt statement) {
    return super.includeCall(method, statement) && !method.getName().equals("methodB");
  }
}
