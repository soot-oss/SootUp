package de.upb.swt.soot.core.inputlocation;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.types.JavaClassType;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Public interface to an input location. <code>AnalysisInputLocation</code>s are sources for {@link
 * SootClass}es, e.g. Java Classpath, Android APK, JAR file, etc. The strategy to traverse
 * something.
 *
 * @author Manuel Benz created on 22.05.18
 * @author Ben Hermann
 * @author Linghui Luo
 */
public interface AnalysisInputLocation {

  /**
   * Create or find a class source for a given type.
   *
   * @param type The type of the class to be found.
   * @return The source entry for that class.
   */
  @Nonnull
  default Optional<? extends AbstractClassSource> getClassSource(@Nonnull JavaClassType type) {
    return getClassSource(type, null);
  }

  @Nonnull
  default Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory) {
    return getClassSources(identifierFactory, null);
  }

  /**
   * Create or find a class source for a given type.
   *
   * @param type The type of the class to be found.
   * @return The source entry for that class.
   */
  @Nonnull
  Optional<? extends AbstractClassSource> getClassSource(
      @Nonnull JavaClassType type, @Nullable ClassLoadingOptions classLoadingOptions);

  @Nonnull
  Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory,
      @Nullable ClassLoadingOptions classLoadingOptions);
}
