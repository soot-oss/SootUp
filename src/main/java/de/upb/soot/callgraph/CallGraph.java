package de.upb.soot.callgraph;

import de.upb.soot.core.SootMethod;
import java.util.Set;

/**
 * Represents a call graph
 *
 * @author Linghui Luo
 * @author Ben Hermann
 * @author Markus Schmidt
 */
public interface CallGraph {

  void addNode(SootMethod calledMethod);

  void addEdge(SootMethod method, SootMethod calledMethod);

  Set<SootMethod> getNodes();

  Set<SootMethod> getTransitions(SootMethod method);

  boolean hasNode(SootMethod sootMethod);

  boolean hasEdge(SootMethod sootMethod, SootMethod calledMethod);
}
