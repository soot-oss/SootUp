package de.upb.soot.namespaces;

import de.upb.soot.Utils;
import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.SignatureFactory;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

/**
 * Base class for {@link INamespace}s that can be located by a {@link Path} object.
 *
 * @author Manuel Benz created on 22.05.18
 */
public abstract class PathBasedNamespace extends AbstractNamespace {
  protected final Path path;

  private PathBasedNamespace(IClassProvider classProvider, Path path) {
    super(classProvider);
    this.path = path;
  }

  /**
   * Creates a {@link PathBasedNamespace} depending on the given {@link Path}, e.g., differs between directories, archives
   * (and possibly network path's in the future).
   * 
   * @param classProvider
   *          The {@link IClassProvider} for generating {@link ClassSource}es out of the found files on the given path
   * @param path
   *          The path to search in
   * @return A {@link PathBasedNamespace} implementation dependent on the given {@link Path}'s {@link FileSystem}
   */
  public static PathBasedNamespace createForClassContainer(IClassProvider classProvider, Path path) {
    if (Files.isDirectory(path)) {
      return new DirectoryBasedNamespace(classProvider, path);
    } else if (PathUtils.isArchive(path)) {
      return new ArchiveBasedNamespace(classProvider, path);
    } else {
      throw new IllegalArgumentException(
          "Path has to be pointing to the root of a class container, e.g. directory, jar, zip, apk, etc.");
    }
  }

  protected Collection<ClassSource> walkDirectory(Path dirPath, SignatureFactory factory) {
    try {
      final FileType handledFileType = classProvider.getHandledFileType();
      return Files.walk(dirPath).filter(filePath -> PathUtils.hasExtension(filePath, handledFileType))
          .flatMap(p -> Utils.optionalToStream(classProvider.getClass(this, p, factory.fromPath(p, dirPath))))
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }



  protected Optional<ClassSource> getClassSourceInternal(ClassSignature signature, Path path) {
    Path pathToClass = path.resolve(signature.toPath(classProvider.getHandledFileType(), path.getFileSystem()));

    if (!Files.exists(pathToClass)) {
      return Optional.empty();
    }

    return classProvider.getClass(this, pathToClass, signature);
  }

  private static final class DirectoryBasedNamespace extends PathBasedNamespace {

    private DirectoryBasedNamespace(IClassProvider classProvider, Path path) {
      super(classProvider, path);
    }

    @Override
    public Collection<ClassSource> getClassSources(SignatureFactory factory) {
      return walkDirectory(path,factory);
    }

    @Override
    public Optional<ClassSource> getClassSource(ClassSignature signature) {
      return getClassSourceInternal(signature, path);
    }
  }

  private static final class ArchiveBasedNamespace extends PathBasedNamespace {

    private ArchiveBasedNamespace(IClassProvider classProvider, Path path) {
      super(classProvider, path);
    }

    @Override
    public Optional<ClassSource> getClassSource(ClassSignature signature) {
      try (FileSystem fs = FileSystems.newFileSystem(path, null)) {
        final Path archiveRoot = fs.getPath("/");
        return getClassSourceInternal(signature, archiveRoot);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    protected Collection<ClassSource> getClassSources(SignatureFactory factory) {
      try (FileSystem fs = FileSystems.newFileSystem(path, null)) {
        final Path archiveRoot = fs.getPath("/");
        return walkDirectory(archiveRoot, factory);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
