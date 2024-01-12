package sootup.jimple.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.io.FilenameUtils;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.ClassProvider;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.FileType;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.util.PathUtils;
import sootup.core.util.StreamUtils;
import sootup.core.views.View;

/** @author Markus Schmidt */
public class JimpleAnalysisInputLocation implements AnalysisInputLocation {
  final Path path;
  /** Variable to track if user has specified the SourceType. By default, it will be set to null. */
  private final SourceType srcType;

  @Nonnull private final List<BodyInterceptor> bodyInterceptors;

  public JimpleAnalysisInputLocation(@Nonnull Path path) {
    this(path, SourceType.Application, Collections.emptyList());
  }

  public JimpleAnalysisInputLocation(@Nonnull Path path, @Nullable SourceType srcType) {
    this(path, srcType, Collections.emptyList());
  }

  public JimpleAnalysisInputLocation(
      @Nonnull Path path,
      @Nullable SourceType srcType,
      @Nonnull List<BodyInterceptor> bodyInterceptors) {
    if (!Files.exists(path)) {
      throw new IllegalArgumentException(
          "The configured path '"
              + path
              + "' pointing to '"
              + path.toAbsolutePath()
              + "' does not exist.");
    }
    this.bodyInterceptors = bodyInterceptors;
    this.path = path;
    this.srcType = srcType;
  }

  @Nonnull
  @Override
  public SourceType getSourceType() {
    return srcType;
  }

  @Nonnull
  @Override
  public List<BodyInterceptor> getBodyInterceptors() {
    return bodyInterceptors;
  }

  @Nonnull
  List<SootClassSource> walkDirectory(
      @Nonnull Path dirPath,
      @Nonnull IdentifierFactory factory,
      @Nonnull ClassProvider classProvider) {

    try (final Stream<Path> walk = Files.walk(path)) {
      return walk.filter(filePath -> PathUtils.hasExtension(filePath, FileType.JIMPLE))
          .flatMap(
              p -> {
                String fullyQualifiedName =
                    FilenameUtils.removeExtension(
                        p.subpath(path.getNameCount(), p.getNameCount())
                            .toString()
                            .replace(p.getFileSystem().getSeparator(), "."));

                return StreamUtils.optionalToStream(
                    classProvider.createClassSource(
                        this, p, factory.getClassType(fullyQualifiedName)));
              })
          .collect(Collectors.toList());

    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  @Nonnull
  public Collection<SootClassSource> getClassSources(@Nonnull View view) {
    return walkDirectory(
        path, view.getIdentifierFactory(), new JimpleClassProvider(bodyInterceptors));
  }

  @Override
  @Nonnull
  public Optional<SootClassSource> getClassSource(@Nonnull ClassType type, @Nonnull View view) {
    final JimpleClassProvider classProvider = new JimpleClassProvider(bodyInterceptors);

    final String ext = classProvider.getHandledFileType().toString().toLowerCase();

    // is file under path:  with name package.subpackage.class.jimple
    Path pathToClass = path.resolve(type.getFullyQualifiedName() + "." + ext);
    if (!Files.exists(pathToClass)) {
      // is file under path with dir structure: package/subpackage/className.jimple
      pathToClass =
          path.resolve(
              type.getPackageName().toString().replace('.', File.separatorChar)
                  + File.separator
                  + type.getClassName()
                  + "."
                  + ext);
      if (!Files.exists(pathToClass)) {
        return Optional.empty();
      }
    }

    return classProvider.createClassSource(this, pathToClass, type);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof JimpleAnalysisInputLocation)) {
      return false;
    }
    return path.equals(((JimpleAnalysisInputLocation) o).path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path);
  }
}
