package de.upb.soot.callgraph;

import de.upb.soot.signatures.MethodSignature;
import javax.annotation.Nonnull;

public interface MutableCallGraph extends CallGraph {
  // TODO Rename all methods called node/edges here and parent interface

  void addNode(@Nonnull MethodSignature calledMethod);

  void addEdge(@Nonnull MethodSignature method, @Nonnull MethodSignature calledMethod);

  void removeCallsFrom(MethodSignature method);
}
