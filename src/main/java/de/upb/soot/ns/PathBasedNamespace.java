package de.upb.soot.ns;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

import de.upb.soot.ns.classprovider.ClassSource;
import de.upb.soot.ns.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;

/** @author Manuel Benz created on 22.05.18 */
public class PathBasedNamespace extends AbstractNamespace {
  private final Path path;

  public PathBasedNamespace(IClassProvider classProvider, Path path) {
    super(classProvider);

    if (!Files.isDirectory(path) && !PathUtils.isArchive(path)) {
      throw new IllegalArgumentException(
          "Path has to be pointing to the root of a class container, e.g. directory, jar, zip, apk, etc.");
    }

    this.path = path;
  }

  @Override
  public Collection<ClassSource> getClassSources() {
    if (Files.isDirectory(path)) {
      return walk(path);
    } else {
      try (FileSystem fs = FileSystems.newFileSystem(path, null)) {
        final Path archiveRoot = fs.getPath("/");
        return walk(archiveRoot);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private Collection<ClassSource> walk(Path path) {
    try {
      return Files.walk(path).filter(p -> classProvider.handlesFile(p)).map(p -> {
        try {
          return classProvider.getClass(this, p);
        } catch (SootClassNotFoundException e) {
          // this should not happen since we are currently enumerating all classes on the
          // path
          throw new IllegalStateException(e);
        }
      }).collect(Collectors.toList());
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public ClassSource getClassSource(ClassSignature signature) throws SootClassNotFoundException {
    final Path subPath = PathUtils.pathFromSignature(signature);
    if (Files.isDirectory(path)) {
      return getClassSourceInternal(signature, path.resolve(subPath));
    } else {
      try (FileSystem fs = FileSystems.newFileSystem(path, null)) {
        final Path pathInsideArchive = fs.getPath("/" + subPath.toString());
        return getClassSourceInternal(signature, pathInsideArchive);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private ClassSource getClassSourceInternal(ClassSignature signature, Path pathToClass) throws SootClassNotFoundException {
    if (!Files.exists(pathToClass)) {
      throw new SootClassNotFoundException(signature);
    }

    return classProvider.getClass(this, pathToClass);
  }
}
