package de.upb.swt.soot.callgraph;

import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Represents a call graph algorithm
 *
 * @author Linghui Luo
 * @author Ben Hermann
 * @author Markus Schmidt
 */
public interface CallGraphAlgorithm {
  @Nonnull
  CallGraph initialize(@Nonnull List<MethodSignature> entryPoints);

  @Nonnull
  CallGraph addClass(@Nonnull CallGraph oldCallGraph, @Nonnull JavaClassType classType);
}
