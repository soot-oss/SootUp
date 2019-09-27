package de.upb.soot.inputlocation;

import de.upb.soot.IdentifierFactory;
import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.frontends.ClassProvider;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.types.JavaClassType;
import java.util.*;
import javax.annotation.Nonnull;

/*
 * @author Markus Schmidt
 */

// TODO: implement sth useful - more than this dummy
public class EagerInputLocation implements AnalysisInputLocation {
  final String srcNamespace;
  /** Creates a new instance of the {@link EagerInputLocation} class. */
  public EagerInputLocation(String srcNamespace) {
    this.srcNamespace = srcNamespace;
  }

  @Override
  public @Nonnull Optional<AbstractClassSource> getClassSource(@Nonnull JavaClassType signature) {
    throw new ResolveException("getClassSourve is not implemented - No class Source found.");
  }

  @Override
  public @Nonnull ClassProvider getClassProvider() {
    throw new ResolveException("Not implemented - No class Provider found.");
  }

  @Nonnull
  @Override
  public Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory) {
    throw new ResolveException("getClassSources not implemented - No class Sources found.");
  }
}
