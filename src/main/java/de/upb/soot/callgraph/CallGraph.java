package de.upb.soot.callgraph;

import de.upb.soot.signatures.MethodSignature;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Represents a call graph
 *
 * @author Linghui Luo
 * @author Ben Hermann
 * @author Markus Schmidt
 */
public interface CallGraph {

  void addNode(@Nonnull MethodSignature calledMethod);

  void addEdge(@Nonnull MethodSignature method, @Nonnull MethodSignature calledMethod);

  @Nonnull
  Set<MethodSignature> getMethodSignatures();

  @Nonnull
  Set<MethodSignature> callsFrom(@Nonnull MethodSignature sourceMethod);

  @Nonnull
  Set<MethodSignature> callsTo(@Nonnull MethodSignature targetMethod);

  boolean hasNode(@Nonnull MethodSignature method);

  boolean hasEdge(@Nonnull MethodSignature sourceMethod, @Nonnull MethodSignature targetMethod);
}
