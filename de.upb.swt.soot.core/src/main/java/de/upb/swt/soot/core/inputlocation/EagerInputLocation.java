package de.upb.swt.soot.core.inputlocation;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.types.JavaClassType;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;

/*
 * @author Markus Schmidt
 */

// TODO: implement sth useful - more than this dummy
public class EagerInputLocation implements AnalysisInputLocation {

  @Override
  public @Nonnull Optional<AbstractClassSource> getClassSource(@Nonnull JavaClassType signature) {
    return Optional.empty();
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
