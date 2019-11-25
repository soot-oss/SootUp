package de.upb.swt.soot.java.bytecode.inputlocation;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.types.ClassType;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * An {@link AnalysisInputLocation} containing Java bytecode. Supplies default {@link
 * de.upb.swt.soot.core.inputlocation.ClassLoadingOptions} from {@link BytecodeClassLoadingOptions}.
 */
public interface BytecodeAnalysisInputLocation extends AnalysisInputLocation {

  @Nonnull
  @Override
  default Optional<? extends AbstractClassSource> getClassSource(@Nonnull ClassType type) {
    return getClassSource(type, BytecodeClassLoadingOptions.Default);
  }

  @Nonnull
  @Override
  default Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory) {
    return getClassSources(identifierFactory, BytecodeClassLoadingOptions.Default);
  }
}
