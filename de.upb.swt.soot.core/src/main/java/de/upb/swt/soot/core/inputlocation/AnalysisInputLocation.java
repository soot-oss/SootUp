package de.upb.swt.soot.core.inputlocation;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.types.JavaClassType;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * Public interface to an input location. {@AnalysisInputLocation}s are sources for {@link
 * SootClass}es, e.g. Java Classpath, Android APK, JAR file, etc. The strategy to traverse
 * something.
 *
 * @author Manuel Benz created on 22.05.18
 * @author Ben Hermann
 * @author Linghui Luo
 */
public interface AnalysisInputLocation {

  /**
   * Create or find a class source for a given signature.
   *
   * @param signature The signature of the class to be found.
   * @return The source entry for that class.
   */
  @Nonnull
  Optional<? extends AbstractClassSource> getClassSource(@Nonnull JavaClassType signature);

  /**
   * The class provider attached to this input location.
   *
   * @return An instance of {@link ClassProvider} to be used.
   */
  @Nonnull
  ClassProvider getClassProvider();

  @Nonnull
  Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory);
}
