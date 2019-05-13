package de.upb.soot.namespaces;

import de.upb.soot.IdentifierFactory;
import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.frontends.IClassProvider;
import de.upb.soot.signatures.ModuleSignature;
import de.upb.soot.types.GlobalScope;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.util.Utils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Base class for {@link INamespace}s that can be located by a {@link Path} object.
 *
 * @author Andreas Dann created on 06.06.18
 */
public class JrtFileSystemNamespace extends AbstractNamespace {

  private FileSystem theFileSystem = FileSystems.getFileSystem(URI.create("jrt:/"));

  protected JrtFileSystemNamespace(IClassProvider classProvider) {
    super(classProvider);
  }

  @Override
  public @Nonnull Optional<? extends AbstractClassSource> getClassSource(
      @Nonnull JavaClassType signature) {

    if (signature.getScope() instanceof ModuleSignature) {
      return this.getClassSourceInternalForModule(
          signature, (ModuleSignature) signature.getScope());
    } else if (signature.getScope() instanceof GlobalScope) {
      // FIXME: return any matching class name, by checking the complete path...
      this.getClassSourceInternalForClassPath(signature);
    }
    return Optional.empty();
  }

  private @Nonnull Optional<AbstractClassSource> getClassSourceInternalForClassPath(
      @Nonnull JavaClassType classSignature) {

    Path filepath = classSignature.toPath(classProvider.getHandledFileType(), theFileSystem);
    final Path moduleRoot = theFileSystem.getPath("modules");
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(moduleRoot)) {
      {
        for (Path entry : stream) {
          // check each module folder for the class
          Path foundfile = entry.resolve(filepath);
          if (Files.isRegularFile(foundfile)) {
            return Optional.of(classProvider.createClassSource(this, foundfile, classSignature));
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return Optional.empty();
  }

  private @Nonnull Optional<? extends AbstractClassSource> getClassSourceInternalForModule(
      @Nonnull JavaClassType classSignature, ModuleSignature moduleSignature) {

    Path filepath = classSignature.toPath(classProvider.getHandledFileType(), theFileSystem);
    final Path module = theFileSystem.getPath("modules", moduleSignature.getModuleName());
    Path foundClass = module.resolve(filepath);

    if (Files.isRegularFile(foundClass)) {
      return Optional.of(classProvider.createClassSource(this, foundClass, classSignature));

    } else {
      return Optional.empty();
    }
  }

  // get the factory, which I should use the create the correspond class signatures
  @Override
  public @Nonnull Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory) {

    final Path archiveRoot = theFileSystem.getPath("modules");
    return walkDirectory(archiveRoot, identifierFactory);
  }

  protected @Nonnull Collection<? extends AbstractClassSource> walkDirectory(
      @Nonnull Path dirPath, @Nonnull IdentifierFactory identifierFactory) {

    final FileType handledFileType = classProvider.getHandledFileType();
    try {
      return Files.walk(dirPath)
          .filter(filePath -> PathUtils.hasExtension(filePath, handledFileType))
          .flatMap(
              p ->
                  Utils.optionalToStream(
                      Optional.of(
                          classProvider.createClassSource(
                              this,
                              p,
                              this.fromPath(
                                  p.subpath(2, p.getNameCount()),
                                  p.subpath(1, 2),
                                  identifierFactory)))))
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Discover and return all modules contained in the jrt filesystem.
   *
   * @return Collection of found module names.
   */
  public @Nonnull Collection<String> discoverModules() {
    final Path moduleRoot = theFileSystem.getPath("modules");
    List<String> foundModules = new ArrayList<>();

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(moduleRoot)) {
      {
        for (Path entry : stream) {
          if (Files.isDirectory(entry)) {
            foundModules.add(entry.subpath(1, 2).toString());
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return foundModules;
  }

  private @Nonnull JavaClassType fromPath(
      final Path filename, final Path moduleDir, final IdentifierFactory identifierFactory) {

    JavaClassType sig = identifierFactory.fromPath(filename);
    sig.setScope(identifierFactory.getModuleSignature(moduleDir.toString()));
    return sig;
  }
}
