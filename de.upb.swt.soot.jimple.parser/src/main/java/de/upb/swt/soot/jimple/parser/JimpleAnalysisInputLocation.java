package de.upb.swt.soot.jimple.parser;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.inputlocation.EmptyClassLoadingOptions;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.PathUtils;
import de.upb.swt.soot.core.util.StreamUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class JimpleAnalysisInputLocation implements AnalysisInputLocation {
  final Path path;
  final IdentifierFactory factory;

  public JimpleAnalysisInputLocation(Path path, IdentifierFactory factory) {
    this.path = path;
    this.factory = factory;
  }

  @Nonnull
  Collection<? extends AbstractClassSource> walkDirectory(
      @Nonnull Path dirPath, @Nonnull IdentifierFactory factory, ClassProvider classProvider) {
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
  public Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory) {
    return getClassSources(identifierFactory, EmptyClassLoadingOptions.Default);
  }

  @Override
  public @Nonnull Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory,
      @Nonnull ClassLoadingOptions classLoadingOptions) {
    return walkDirectory(
        path,
        identifierFactory,
        new JimpleClassProvider(classLoadingOptions.getBodyInterceptors()));
  }

  @Nonnull
  @Override
  public Optional<? extends AbstractClassSource> getClassSource(@Nonnull ClassType type) {
    return getClassSource(type, EmptyClassLoadingOptions.Default);
  }

  @Override
  public @Nonnull Optional<? extends AbstractClassSource> getClassSource(
      @Nonnull ClassType type, @Nonnull ClassLoadingOptions classLoadingOptions) {
    final JimpleClassProvider classProvider =
        new JimpleClassProvider(classLoadingOptions.getBodyInterceptors());

    // is file under path:  with name package.subpackage.class.jimple
    Path pathToClass = path.resolve(type + "." + classProvider.getHandledFileType());
    if (!Files.exists(pathToClass)) {
      // is file under path with dir structure: package/subpackage/className.jimple
      pathToClass =
          path.resolve(
              type.getPackageName().toString().replace('.', '/')
                  + type.getClassName()
                  + "."
                  + classProvider.getHandledFileType());
      if (!Files.exists(pathToClass)) {
        // TODO: [ms] better throw an exception
        return Optional.empty();
      }
    }

    return Optional.of(classProvider.createClassSource(this, pathToClass, type));
  }
}
