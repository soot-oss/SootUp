package de.upb.soot.inputlocation;

import de.upb.soot.IdentifierFactory;
import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.frontends.ClassProvider;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.EagerJavaClassSource;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.util.NotYetImplementedException;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/*
 * @author Markus Schmidt
 */
public class EagerInputLocation implements AnalysisInputLocation {
  final String srcNamespace;
  /**
   * Creates a new instance of the {@link EagerInputLocation} class.
   */
  public EagerInputLocation( String srcNamespace ) {
    this.srcNamespace = srcNamespace;
  }

  /**
   * @param signature The class to be searched.
   * @return The {@link ClassSource} instance found or created... Or an empty Optional.
   */
  @Override
  public @Nonnull Optional<AbstractClassSource> getClassSource(@Nonnull JavaClassType signature) {
    // TODO
    throw new NotYetImplementedException("Not implemented - No class Source found.");
  }

  /**
   *
   * @return An instance of {@link ClassProvider} to be used.
   */
  @Override
  public @Nonnull ClassProvider getClassProvider() {
    // TODO
    throw new NotYetImplementedException("Not implemented - No class Provider found.");
  }

  @Override
  public @Nonnull Collection<AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory) {
    // TODO Auto-generated methodRef stub
    throw new NotYetImplementedException("Getting class sources is not implemented, yet.");
  }
}
