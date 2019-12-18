package de.upb.swt.soot;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.types.ClassType;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;

public class JimpleAnalysisInputLocation implements AnalysisInputLocation {
  @Nonnull
  @Override
  public Optional<? extends AbstractClassSource> getClassSource(@Nonnull ClassType type) {
    // TODO: implement
    return Optional.empty();
  }

  @Nonnull
  @Override
  public Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory) {
    // TODO: implement
    return null;
  }

  @Nonnull
  @Override
  public Optional<? extends AbstractClassSource> getClassSource(
      @Nonnull ClassType type, @Nonnull ClassLoadingOptions classLoadingOptions) {
    // TODO: implement
    return Optional.empty();
  }

  @Nonnull
  @Override
  public Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory,
      @Nonnull ClassLoadingOptions classLoadingOptions) {
    // TODO: implement
    return null;
  }
}
