package de.upb.soot.namespaces;

import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.frontends.java.WalaClassLoader;
import de.upb.soot.frontends.java.WalaJavaClassProvider;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.TypeFactory;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An implementation of the {@link INamespace} interface for the Java source code path.
 *
 * @author Linghui Luo
 */
public class JavaSourcePathNamespace extends AbstractNamespace {

  @Nonnull private final Set<String> sourcePaths;
  private final String exclusionFilePath;

  /**
   * Create a {@link JavaSourcePathNamespace} which locates java source code in the given source
   * path.
   *
   * @param sourcePaths the source code path to search in
   */
  public JavaSourcePathNamespace(@Nonnull Set<String> sourcePaths) {
    this(sourcePaths, null);
  }

  /**
   * Create a {@link JavaSourcePathNamespace} which locates java source code in the given source
   * path.
   *
   * @param sourcePaths the source code path to search in
   */
  public JavaSourcePathNamespace(
      @Nonnull Set<String> sourcePaths, @Nullable String exclusionFilePath) {
    super(new WalaJavaClassProvider(exclusionFilePath));

    this.sourcePaths = sourcePaths;
    this.exclusionFilePath = exclusionFilePath;
  }

  @Override
  @Nonnull
  public Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull SignatureFactory signatureFactory, TypeFactory typeFactory) {
    return new WalaClassLoader(sourcePaths, exclusionFilePath).getClassSources();
  }

  @Override
  @Nonnull
  public Optional<? extends AbstractClassSource> getClassSource(@Nonnull JavaClassType type) {
    for (String path : sourcePaths) {
      try {
        return Optional.of(getClassProvider().createClassSource(this, Paths.get(path), type));
      } catch (ResolveException ignored) {
        // TODO This is really ugly. Maybe we can make createClassSource return an optional /
        //   nullable?
      }
    }
    return Optional.empty();
  }
}
