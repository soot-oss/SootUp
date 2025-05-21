package sootup.spark;

import java.util.HashSet;
import java.util.Set;
import sootup.callgraph.CallGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.views.View;

public class Spark {
  public Spark(View view, CallGraph callGraph) {}

  public Set<Node> getPointsToSet(Local local) {
    return new HashSet<>();
  }

  public Type getType(Local local) {
    return PrimitiveType.IntType.getInstance();
  }

  public Set<Local> getAliases(Local local) {
    return new HashSet<>();
  }
}
