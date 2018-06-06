package de.upb.soot.ns;

import java.io.IOException;
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

    if (!Files.isDirectory(path)) {
      throw new IllegalArgumentException(
          "Path has to be pointing to the root of a class container, e.g. directory, jar, zip, etc.");
    }

    this.path = path;
  }

  @Override
  public Collection<ClassSource> getClassSources() {
    try {
      return Files.walk(path).filter(p -> classProvider.handlesType(p.getFileName())).map(p -> {
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
    final Path pathToClass = path.resolve(PathUtils.signatureToPath(signature));

    if (!Files.exists(pathToClass)) {
      throw new SootClassNotFoundException(signature);
    }

    return classProvider.getClass(this, pathToClass);
  }
}
