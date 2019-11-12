package de.upb.swt.soot.core.inputlocation;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.types.ClassType;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * Public interface to an input location. <code>AnalysisInputLocation</code>s are sources for {@link
 * SootClass}es, e.g. Java Classpath, Android APK, JAR file, etc. The strategy to traverse
 * something.
 *
 * <p>{@link #getClassSource(ClassType)} and {@link #getClassSources(IdentifierFactory)} should in
 * most cases simply call {@link #getClassSource(ClassType, ClassLoadingOptions)} or {@link
 * #getClassSources(IdentifierFactory, ClassLoadingOptions)} respectively with the default {@link
 * ClassLoadingOptions} of the frontend.
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
  Optional<? extends AbstractClassSource> getClassSource(@Nonnull ClassType type);

  /**
   * Scan the input location and create ClassSources for every compilation / interpretation unit.
   */
  @Nonnull
  Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory);

  /**
   * Create or find a class source for a given type.
   *
   * @param type The type of the class to be found.
   * @return The source entry for that class.
   */
  @Nonnull
  Optional<? extends AbstractClassSource> getClassSource(
      @Nonnull ClassType type, @Nonnull ClassLoadingOptions classLoadingOptions);

  /**
   * Scan the input location and create ClassSources for every compilation / interpretation unit.
   */
  @Nonnull
  Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory,
      @Nonnull ClassLoadingOptions classLoadingOptions);
}
