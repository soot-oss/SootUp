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
 * @author Christian Brüggemann
 */
public interface CallGraph {

  @Nonnull
  Set<MethodSignature> getMethodSignatures();

  @Nonnull
  Set<MethodSignature> callsFrom(@Nonnull MethodSignature sourceMethod);

  @Nonnull
  Set<MethodSignature> callsTo(@Nonnull MethodSignature targetMethod);

  boolean containsMethod(@Nonnull MethodSignature method);

  boolean containsCall(
      @Nonnull MethodSignature sourceMethod, @Nonnull MethodSignature targetMethod);

  @Nonnull
  MutableCallGraph copy();
}
