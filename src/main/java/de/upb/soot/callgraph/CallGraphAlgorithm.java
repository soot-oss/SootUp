package de.upb.soot.callgraph;

import de.upb.soot.signatures.MethodSignature;
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
}
