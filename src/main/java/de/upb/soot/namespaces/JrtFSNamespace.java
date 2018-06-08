package de.upb.soot.namespaces;

import de.upb.soot.Utils;
import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.ModulePackageSignature;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Base class for {@link INamespace}s that can be located by a {@link Path} object.
 *
 * @author Andreas Dann created on 06.06.18
 */
public class JrtFSNamespace extends AbstractNamespace {

  protected JrtFSNamespace(IClassProvider classProvider) {
    super(classProvider);
  }

  @Override
  public Optional<ClassSource> getClassSource(ClassSignature signature) {
    if (signature.packageSignature instanceof ModulePackageSignature)
      return this.getClassSourceInternalForModule(signature);
    return this.getClassSourceInternalForClassPath(signature);
  }

  private Optional<ClassSource> getClassSourceInternalForClassPath(ClassSignature classSignature) {
    try (FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"))) {
      Path filepath = classSignature.toPath(classProvider.getHandledFileType(), fs);
      final Path moduleRoot = fs.getPath("modules");
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(moduleRoot)) {

        for (Path entry : stream) {
          // check each module folder for the class
          Path foundfile = entry.resolve(filepath);
          if (Files.isRegularFile(foundfile)) {
            return classProvider.getClass(this, foundfile);

          }
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    return Optional.empty();
  }

  private Optional<ClassSource> getClassSourceInternalForModule(ClassSignature classSignature) {
    Preconditions.checkArgument(classSignature.packageSignature instanceof ModulePackageSignature);

    ModulePackageSignature modulePackageSignature = (ModulePackageSignature) classSignature.packageSignature;
    try (FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"))) {
      Path filepath = classSignature.toPath(classProvider.getHandledFileType(), fs);
      final Path moduleRoot = fs.getPath("modules");
      Path modulePath = moduleRoot.resolve(modulePackageSignature.moduleSignature.moduleName);
      Path foundClass = modulePath.resolve(filepath);
      if (Files.isRegularFile(foundClass)) {
        return classProvider.getClass(this, foundClass);

      } else {
        return Optional.empty();
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  protected Collection<ClassSource> getClassSources() {
    try (FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"))) {
      final Path archiveRoot = fs.getPath("modules");
      return walkDirectory(archiveRoot);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected Collection<ClassSource> walkDirectory(Path dirPath) {
    try {
      final FileType handledFileType = classProvider.getHandledFileType();
      return Files.walk(dirPath).filter(filePath -> PathUtils.hasExtension(filePath, handledFileType))
          .flatMap(p -> Utils.optionalToStream(classProvider.getClass(this, p))).collect(Collectors.toList());
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

}
