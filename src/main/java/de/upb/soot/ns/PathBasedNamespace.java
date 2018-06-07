package de.upb.soot.ns;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import de.upb.soot.Utils;
import de.upb.soot.ns.classprovider.ClassSource;
import de.upb.soot.ns.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;

/** @author Manuel Benz created on 22.05.18 */
public abstract class PathBasedNamespace extends AbstractNamespace {
  protected final Path path;

  private PathBasedNamespace(IClassProvider classProvider, Path path) {
    super(classProvider);
    this.path = path;
  }

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

  protected Collection<ClassSource> walkDirectory(Path path) {
    try {
      return Files.walk(path).filter(p -> classProvider.handlesFile(p))
          .flatMap(p -> Utils.optionalToStream(classProvider.getClass(this, p))).collect(Collectors.toList());
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  protected Optional<ClassSource> getClassSourceInternal(ClassSignature signature, Path path) {
    Path pathToClass = path.resolve(PathUtils.pathFromSignature(signature, path.getFileSystem()));

    if (!Files.exists(pathToClass)) {
      return Optional.empty();
    }

    return classProvider.getClass(this, pathToClass);
  }

  private static final class DirectoryBasedNamespace extends PathBasedNamespace {

    private DirectoryBasedNamespace(IClassProvider classProvider, Path path) {
      super(classProvider, path);
    }

    @Override
    public Collection<ClassSource> getClassSources() {
      return walkDirectory(path);
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
        final Path pathInsideArchive = fs.getPath("/");
        return getClassSourceInternal(signature, pathInsideArchive);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    protected Collection<ClassSource> getClassSources() {
      try (FileSystem fs = FileSystems.newFileSystem(path, null)) {
        final Path archiveRoot = fs.getPath("/");
        return walkDirectory(archiveRoot);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
