package de.upb.soot.ns;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.upb.soot.ClassSource;
import de.upb.soot.IClassProvider;

/** @author Manuel Benz created on 22.05.18 */
public class PathBasedNamespace extends AbstractNamespace {
  private final Path path;

  public PathBasedNamespace(IClassProvider classProvider, Path path) {
    super(classProvider);
    this.path = path;
  }

  @Override
  public Collection<ClassSource> getClasses() {
    try {
      return walk().collect(Collectors.toList());
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public Optional<ClassSource> getClass(String className) {
    try {
      return walk().filter(cs -> cs.getName().equals(className)).findFirst();
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private Stream<ClassSource> walk() throws IOException {
    return Files.walk(path)
        .filter(p -> classProvider.handlesType(p.getFileName().toString()))
        .map(
            p -> {
              // FIXME this is not enough info to create a class source object!
              try (InputStream in = Files.newInputStream(p)) {
                return classProvider.find(in);
              } catch (IOException e) {
                throw new IllegalArgumentException(e);
              }
            });
  }
}
