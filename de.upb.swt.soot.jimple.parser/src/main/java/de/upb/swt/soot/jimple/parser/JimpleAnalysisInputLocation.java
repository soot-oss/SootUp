package de.upb.swt.soot.jimple.parser;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.PathUtils;
import de.upb.swt.soot.core.util.StreamUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class JimpleAnalysisInputLocation implements AnalysisInputLocation {
  final Path path;
  final IdentifierFactory factory;
  final ClassProvider classProvider;

  public JimpleAnalysisInputLocation(Path path, IdentifierFactory factory) {
    this.path = path;
    this.factory = factory;
    classProvider = new JimpleClassProvider();
  }

  @Nonnull
  @Override
  public Optional<? extends AbstractClassSource> getClassSource(@Nonnull ClassType type) {
    // FIXME classloadingopts
    return getClassSource(type, null);
  }

  @Nonnull
  @Override
  public Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory) {
    // FIXME classloadingopts
    return getClassSources(identifierFactory, null);
  }

  @Nonnull
  @Override
  public Optional<? extends AbstractClassSource> getClassSource(
      @Nonnull ClassType type, @Nonnull ClassLoadingOptions classLoadingOptions) {
    // TODO: implement classLoadingOpts
    return Optional.of(
        classProvider.createClassSource(this, Paths.get("SOME-JIMPLE-FILE.jimple"), type));
  }

  @Nonnull
  @Override
  public Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory,
      @Nonnull ClassLoadingOptions classLoadingOptions) {

    // TODO: implement classloadingopts

    try {

      return Files.walk(path)
          .filter(filePath -> PathUtils.hasExtension(filePath, FileType.JIMPLE))
          .flatMap(
              p ->
                  StreamUtils.optionalToStream(
                      Optional.of(classProvider.createClassSource(this, p, factory.fromPath(p)))))
          .collect(Collectors.toList());

    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
