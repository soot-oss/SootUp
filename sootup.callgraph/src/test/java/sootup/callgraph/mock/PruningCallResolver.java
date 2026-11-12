package sootup.callgraph.mock;

import org.jspecify.annotations.NonNull;
import sootup.callgraph.scope.CallFilter;
import sootup.callgraph.scope.DefaultCallResolver;
import sootup.core.model.SootMethod;
import sootup.core.views.View;

/**
 * A {@link sootup.callgraph.scope.CallResolver} used to test edge pruning: in addition to the
 * default library-class exclusion, it excludes every call originating from a method named <code>
 * methodB</code>.
 */
public class PruningCallResolver extends DefaultCallResolver {
  public PruningCallResolver(@NonNull View view) {
    super(view);
  }

  @Override
  @NonNull
  public CallFilter tryAdvanceMethod(@NonNull SootMethod method) {
    if (method.getName().equals("methodB")) {
      return CallFilter.none();
    }
    return super.tryAdvanceMethod(method);
  }
}
