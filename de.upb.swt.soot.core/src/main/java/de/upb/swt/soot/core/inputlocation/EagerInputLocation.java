package de.upb.swt.soot.core.inputlocation;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.types.ClassType;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** @author Markus Schmidt */

// TODO: implement sth useful - more than this dummy
public class EagerInputLocation implements AnalysisInputLocation {

  @Nonnull
  @Override
  public Optional<AbstractClassSource> getClassSource(@Nonnull ClassType type) {
    return Optional.empty();
  }

  @Nonnull
  @Override
  public Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory) {
    throw new ResolveException("getClassSources not implemented - No class sources found.");
  }

  @Override
  public @Nonnull Optional<AbstractClassSource> getClassSource(
      @Nonnull ClassType type, @Nullable ClassLoadingOptions classLoadingOptions) {
    return Optional.empty();
  }

  @Nonnull
  @Override
  public Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory,
      @Nullable ClassLoadingOptions classLoadingOptions) {
    throw new ResolveException("getClassSources not implemented - No class sources found.");
  }
}
