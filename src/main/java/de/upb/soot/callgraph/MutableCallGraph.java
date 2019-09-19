package de.upb.soot.callgraph;

import de.upb.soot.signatures.MethodSignature;
import javax.annotation.Nonnull;

/**
 * Represents a mutable call graph
 *
 * @author Linghui Luo
 * @author Ben Hermann
 * @author Markus Schmidt
 * @author Christian Brüggemann
 */
public interface MutableCallGraph extends CallGraph {
  void addMethod(@Nonnull MethodSignature calledMethod);

  void addCall(@Nonnull MethodSignature sourceMethod, @Nonnull MethodSignature targetMethod);
}
