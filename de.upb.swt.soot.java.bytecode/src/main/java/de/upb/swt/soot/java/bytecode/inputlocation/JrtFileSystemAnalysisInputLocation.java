package de.upb.swt.soot.java.bytecode.inputlocation;

import com.google.common.base.Preconditions;
import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.PathUtils;
import de.upb.swt.soot.core.util.StreamUtils;
import de.upb.swt.soot.java.bytecode.frontend.AsmJavaClassProvider;
import de.upb.swt.soot.java.core.ModuleIdentifierFactory;
import de.upb.swt.soot.java.core.signatures.ModulePackageName;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Base class for {@link AnalysisInputLocation}s that can be located by a {@link Path} object.
 *
 * @author Andreas Dann created on 06.06.18
 */
public class JrtFileSystemAnalysisInputLocation implements BytecodeAnalysisInputLocation {

  private final FileSystem theFileSystem = FileSystems.getFileSystem(URI.create("jrt:/"));

  @Override
  public @Nonnull Optional<? extends AbstractClassSource> getClassSource(
      @Nonnull ClassType classType, @Nonnull ClassLoadingOptions classLoadingOptions) {
    JavaClassType klassType = (JavaClassType) classType;
    List<BodyInterceptor> bodyInterceptors = classLoadingOptions.getBodyInterceptors();
    if (klassType.getPackageName() instanceof ModulePackageName) {
      return this.getClassSourceInternalForModule(
          klassType, new AsmJavaClassProvider(bodyInterceptors));
    }
    return this.getClassSourceInternalForClassPath(
        klassType, new AsmJavaClassProvider(bodyInterceptors));
  }

  private @Nonnull Optional<AbstractClassSource> getClassSourceInternalForClassPath(
      @Nonnull JavaClassType classSignature, @Nonnull ClassProvider classProvider) {

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
      @Nonnull JavaClassType classSignature, @Nonnull ClassProvider classProvider) {
    Preconditions.checkArgument(classSignature.getPackageName() instanceof ModulePackageName);

    ModulePackageName modulePackageSignature = (ModulePackageName) classSignature.getPackageName();

    Path filepath = classSignature.toPath(classProvider.getHandledFileType(), theFileSystem);
    final Path module =
        theFileSystem.getPath(
            "modules", modulePackageSignature.getModuleSignature().getModuleName());
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
      @Nonnull IdentifierFactory identifierFactory,
      @Nonnull ClassLoadingOptions classLoadingOptions) {
    List<BodyInterceptor> bodyInterceptors = classLoadingOptions.getBodyInterceptors();

    final Path archiveRoot = theFileSystem.getPath("modules");
    return walkDirectory(
        archiveRoot, identifierFactory, new AsmJavaClassProvider(bodyInterceptors));
  }

  protected @Nonnull Collection<? extends AbstractClassSource> walkDirectory(
      @Nonnull Path dirPath,
      @Nonnull IdentifierFactory identifierFactory,
      ClassProvider classProvider) {

    final FileType handledFileType = classProvider.getHandledFileType();
    try {
      return Files.walk(dirPath)
          .filter(filePath -> PathUtils.hasExtension(filePath, handledFileType))
          .flatMap(
              p ->
                  StreamUtils.optionalToStream(
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

  // TODO: originally, I could create a ModuleSingatre in any case, however, then
  // every signature factory needs a methodRef create from path
  // however, I cannot think of a general way for java 9 modules anyway....
  // how to create the module name if we have a jar file..., or a multi jar, or the jrt file system
  // nevertheless, one general methodRef for all signatures seems reasonable
  private @Nonnull JavaClassType fromPath(
      final Path filename, final Path moduleDir, final IdentifierFactory identifierFactory) {

    // else use the module system and create fully class signature
    JavaClassType sig = (JavaClassType) identifierFactory.fromPath(filename);

    if (identifierFactory instanceof ModuleIdentifierFactory) {
      // FIXME: adann clean this up!
      // String filename = FilenameUtils.removeExtension(file.toString()).replace('/', '.');
      // int index = filename.lastIndexOf('.');
      // Path parentDir = filename.subpath(0, 2);
      // Path packageFileName = parentDir.relativize(filename);
      // // get the package
      // String packagename = packageFileName.toString().replace('/', '.');
      // String classname = FilenameUtils.removeExtension(packageFileName.getFileName().toString());
      //

      return ((ModuleIdentifierFactory) identifierFactory)
          .getClassType(
              sig.getClassName(), sig.getPackageName().getPackageName(), moduleDir.toString());
    }

    // if we are using the normal signature factory, than trim the module from the path
    return sig;
  }
}
