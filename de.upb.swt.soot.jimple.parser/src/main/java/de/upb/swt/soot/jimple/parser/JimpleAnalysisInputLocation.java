package de.upb.swt.soot.jimple.parser;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.inputlocation.EmptyClassLoadingOptions;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.PathUtils;
import de.upb.swt.soot.core.util.StreamUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/** @author Markus Schmidt */
public class JimpleAnalysisInputLocation implements AnalysisInputLocation<SootClass<?>> {
  final Path path;

  public JimpleAnalysisInputLocation(@Nonnull Path path) {
    this.path = path;
  }

  @Nonnull
  List<AbstractClassSource<? extends SootClass<?>>> walkDirectory(
      @Nonnull Path dirPath,
      @Nonnull IdentifierFactory factory,
      @Nonnull ClassProvider<? extends SootClass<?>> classProvider) {
    try {
      final FileType handledFileType = classProvider.getHandledFileType();
      return Files.walk(dirPath)
          .filter(filePath -> PathUtils.hasExtension(filePath, handledFileType))
          .flatMap(
              p ->
                  StreamUtils.optionalToStream(
                      Optional.of(classProvider.createClassSource(this, p, factory.fromPath(p)))))
          .collect(Collectors.toList());

    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Nonnull
  @Override
  public Collection<? extends AbstractClassSource<? extends SootClass<?>>> getClassSources(
      @Nonnull IdentifierFactory identifierFactory) {
    return getClassSources(identifierFactory, EmptyClassLoadingOptions.Default);
  }

  @Override
  public @Nonnull Collection<? extends AbstractClassSource<? extends SootClass<?>>> getClassSources(
      @Nonnull IdentifierFactory identifierFactory,
      @Nonnull ClassLoadingOptions classLoadingOptions) {
    return walkDirectory(
        path,
        identifierFactory,
        new JimpleClassProvider(classLoadingOptions.getBodyInterceptors()));
  }

  @Nonnull
  @Override
  public Optional<? extends AbstractClassSource<SootClass<?>>> getClassSource(
      @Nonnull ClassType type) {
    return getClassSource(type, EmptyClassLoadingOptions.Default);
  }

  @Override
  public @Nonnull Optional<? extends AbstractClassSource<SootClass<?>>> getClassSource(
      @Nonnull ClassType type, @Nonnull ClassLoadingOptions classLoadingOptions) {
    final JimpleClassProvider classProvider =
        new JimpleClassProvider(classLoadingOptions.getBodyInterceptors());

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

    return Optional.of(classProvider.createClassSource(this, pathToClass, type));
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
