package de.upb.soot.callgraph;

import de.upb.soot.signatures.MethodSignature;

import java.util.List;

/**
 * Represents a call graph algorithm
 *
 * @author Linghui Luo
 * @author Ben Hermann
 * @author Markus Schmidt
 */
public interface CallGraphAlgorithm {
  CallGraph initialize(List<MethodSignature> entryPoints);
}
