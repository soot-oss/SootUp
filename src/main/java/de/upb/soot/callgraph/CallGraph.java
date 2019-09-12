package de.upb.soot.callgraph;

import de.upb.soot.core.Method;
import de.upb.soot.core.SootMethod;
import de.upb.soot.signatures.MethodSignature;

import java.util.Set;

/**
 * Represents a call graph
 *
 * @author Linghui Luo
 * @author Ben Hermann
 * @author Markus Schmidt
 */
public interface CallGraph {

  void addNode(MethodSignature calledMethod);

  void addEdge(MethodSignature method, MethodSignature calledMethod);

  Set<MethodSignature> getNodes();

  Set<MethodSignature> getTransitions(MethodSignature method);

  boolean hasNode(MethodSignature method);

  boolean hasEdge(MethodSignature sourceMethod, MethodSignature targetMethod);
}
