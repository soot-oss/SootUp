package sootup.jimple.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
  private final List<BodyInterceptor> bodyInterceptors;

  /** Variable to track if user has specified the SourceType. By default, it will be set to null. */
  private SourceType srcType = null;

  // TODO: allow pointing to a single file
  public JimpleAnalysisInputLocation(@Nonnull Path path) {
    this(path, null);
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
    this.path = path;
    this.bodyInterceptors = bodyInterceptors;
    setSpecifiedAsBuiltInByUser(srcType);
  }

  /**
   * The method sets the value of the variable srcType.
   *
   * @param srcType the source type for the path can be Library, Application, Phantom.
   */
  public void setSpecifiedAsBuiltInByUser(@Nullable SourceType srcType) {
    this.srcType = srcType;
  }

  @Override
  public SourceType getSourceType() {
    return srcType;
  }

  @Override
  @Nonnull
  public List<BodyInterceptor> getBodyInterceptors() {
    return bodyInterceptors;
  }

  @Nonnull
  List<SootClassSource> walkDirectory(
      @Nonnull Path dirPath,
      @Nonnull IdentifierFactory factory,
      @Nonnull ClassProvider classProvider) {
    try {
      final FileType handledFileType = classProvider.getHandledFileType();
      return Files.walk(dirPath)
          .filter(filePath -> PathUtils.hasExtension(filePath, handledFileType))
          .flatMap(
              p ->
                  StreamUtils.optionalToStream(
                      classProvider.createClassSource(this, p, factory.fromPath(dirPath, p))))
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
