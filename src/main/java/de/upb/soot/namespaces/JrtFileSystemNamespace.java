package de.upb.soot.namespaces;

import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.ModulePackageSignature;
import de.upb.soot.signatures.ModuleSignatureFactory;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.util.Utils;

import com.google.common.base.Preconditions;

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
  public Optional<ClassSource> getClassSource(JavaClassSignature signature) {
    if (signature.packageSignature instanceof ModulePackageSignature) {
      return this.getClassSourceInternalForModule(signature);
    }
    return this.getClassSourceInternalForClassPath(signature);
  }

  private Optional<ClassSource> getClassSourceInternalForClassPath(JavaClassSignature classSignature) {

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

  private Optional<ClassSource> getClassSourceInternalForModule(JavaClassSignature classSignature) {
    Preconditions.checkArgument(classSignature.packageSignature instanceof ModulePackageSignature);

    ModulePackageSignature modulePackageSignature = (ModulePackageSignature) classSignature.packageSignature;

    Path filepath = classSignature.toPath(classProvider.getHandledFileType(), theFileSystem);
    final Path module = theFileSystem.getPath("modules", modulePackageSignature.moduleSignature.moduleName);
    Path foundClass = module.resolve(filepath);

    if (Files.isRegularFile(foundClass)) {
      return Optional.of(classProvider.createClassSource(this, foundClass, classSignature));

    } else {
      return Optional.empty();
    }

  }

  // get the factory, which I should use the create the correspond class signatures
  @Override
  public Collection<ClassSource> getClassSources(SignatureFactory factory) {

    final Path archiveRoot = theFileSystem.getPath("modules");
    return walkDirectory(archiveRoot, factory);

  }

  protected Collection<ClassSource> walkDirectory(Path dirPath, SignatureFactory factory) {

    final FileType handledFileType = classProvider.getHandledFileType();
    try {
      return Files.walk(dirPath).filter(filePath -> PathUtils.hasExtension(filePath, handledFileType))
          .flatMap(p -> Utils.optionalToStream(Optional.of(classProvider.createClassSource(this, p,
              this.fromPath(p.subpath(2, p.getNameCount()), p.subpath(1, 2), factory)))))
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
  public Collection<String> discoverModules() {
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
  private JavaClassSignature fromPath(final Path filename, final Path moduleDir, final SignatureFactory factory) {

    // else use the module system and create fully class signature
    if (factory instanceof ModuleSignatureFactory) {
      // FIXME: adann clean this up!
      // String filename = FilenameUtils.removeExtension(file.toString()).replace('/', '.');
      // int index = filename.lastIndexOf('.');
      // Path parentDir = filename.subpath(0, 2);
      // Path packageFileName = parentDir.relativize(filename);
      // // get the package
      // String packagename = packageFileName.toString().replace('/', '.');
      // String classname = FilenameUtils.removeExtension(packageFileName.getFileName().toString());
      //
      JavaClassSignature sig = factory.fromPath(filename);

      return ((ModuleSignatureFactory) factory).getClassSignature(sig.className, sig.packageSignature.packageName,
          moduleDir.toString());
    }

    // if we are using the normal signature factory, than trim the module from the path
    return factory.fromPath(filename);

  }

}
