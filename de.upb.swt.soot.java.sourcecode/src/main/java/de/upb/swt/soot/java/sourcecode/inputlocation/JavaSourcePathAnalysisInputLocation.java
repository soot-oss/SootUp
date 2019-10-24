package de.upb.swt.soot.java.sourcecode.inputlocation;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AbstractAnalysisInputLocation;
import de.upb.swt.soot.core.types.ReferenceType;
import de.upb.swt.soot.java.sourcecode.frontend.WalaClassLoader;
import de.upb.swt.soot.java.sourcecode.frontend.WalaJavaClassProvider;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the {@link AbstractAnalysisInputLocation} interface for the Java source code
 * path.
 *
 * @author Linghui Luo
 */
public class JavaSourcePathAnalysisInputLocation extends AbstractAnalysisInputLocation {

  private static final Logger log =
      LoggerFactory.getLogger(JavaSourcePathAnalysisInputLocation.class);

  @Nonnull private final Set<String> sourcePaths;
  private final String exclusionFilePath;

  /**
   * Create a {@link JavaSourcePathAnalysisInputLocation} which locates java source code in the
   * given source path.
   *
   * @param sourcePaths the source code path to search in
   */
  public JavaSourcePathAnalysisInputLocation(@Nonnull Set<String> sourcePaths) {
    this(sourcePaths, null);
  }

  /**
   * Create a {@link JavaSourcePathAnalysisInputLocation} which locates java source code in the
   * given source path.
   *
   * @param sourcePaths the source code path to search in
   */
  public JavaSourcePathAnalysisInputLocation(
      @Nonnull Set<String> sourcePaths, @Nullable String exclusionFilePath) {
    super(new WalaJavaClassProvider(exclusionFilePath));
    this.sourcePaths = sourcePaths;
    this.exclusionFilePath = exclusionFilePath;
  }

  @Override
  @Nonnull
  public Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory) {
    return new WalaClassLoader(sourcePaths, exclusionFilePath).getClassSources();
  }

  @Override
  @Nonnull
  public Optional<? extends AbstractClassSource> getClassSource(@Nonnull ReferenceType type) {
    for (String path : sourcePaths) {
      try {
        return Optional.of(getClassProvider().createClassSource(this, Paths.get(path), type));
      } catch (ResolveException e) {
        log.debug(type + " not found in sourcePath " + path, e);
      }
    }
    return Optional.empty();
  }
}
