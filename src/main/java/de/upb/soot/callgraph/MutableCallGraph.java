package de.upb.soot.callgraph;

import de.upb.soot.signatures.MethodSignature;
import javax.annotation.Nonnull;

public interface MutableCallGraph extends CallGraph {
  void addNode(@Nonnull MethodSignature calledMethod);

  void addEdge(@Nonnull MethodSignature method, @Nonnull MethodSignature calledMethod);
}
