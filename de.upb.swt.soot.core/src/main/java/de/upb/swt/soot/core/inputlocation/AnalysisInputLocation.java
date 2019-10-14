package de.upb.swt.soot.core.inputlocation;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SourceType;
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
public abstract class AnalysisInputLocation {

  @Nonnull protected final SourceTypeSpecifier sourceTypeSpecifier;

  protected AnalysisInputLocation(@Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    this.sourceTypeSpecifier = sourceTypeSpecifier;
  }

  /**
   * The SourceType attached to this input location.
   *
   * @return the type of Source
   */
  @Nonnull
  public SourceType getSourceType(JavaClassType jct) {
    return sourceTypeSpecifier.sourceTypeFor(jct);
  }

  public SourceTypeSpecifier getSourceTypeSpecifier() {
    return sourceTypeSpecifier;
  }

  /**
   * Create or find a class source for a given signature.
   *
   * @param signature The signature of the class to be found.
   * @return The source entry for that class.
   */
  @Nonnull
  public abstract Optional<? extends AbstractClassSource> getClassSource(
      @Nonnull JavaClassType signature);

  @Nonnull
  public abstract Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory);
  /**
   * The class provider attached to this input location.
   *
   * @return An instance of {@link ClassProvider} to be used.
   */
  @Nonnull
  public abstract ClassProvider getClassProvider();
}
