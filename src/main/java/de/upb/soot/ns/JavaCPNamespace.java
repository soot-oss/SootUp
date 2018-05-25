package de.upb.soot.ns;

import com.google.common.io.Files;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.upb.soot.signatures.ClassSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.soot.ClassSource;
import de.upb.soot.IClassProvider;

/** @author Manuel Benz created on 22.05.18 */
public class JavaCPNamespace extends AbstractNamespace {
  private static final Logger logger = LoggerFactory.getLogger(JavaCPNamespace.class);

  private final Map<Path, INamespace> cpEntries;
  private final Map<String, ClassSource> nameToSrc = new HashMap<>();

  public JavaCPNamespace(IClassProvider classProvider, String classPath) {
    super(classProvider);
    cpEntries = explode(classPath).collect(Collectors.toMap(Function.identity(), p -> null));
  }

  private Stream<Path> explode(String classPath) {
    // the classpath is split at every path separator which is not escaped
    String regex = "(?<!\\\\)" + Pattern.quote(File.pathSeparator);
    return Stream.of(classPath.split(regex)).map(s -> Paths.get(s));
  }

  @Override
  public Collection<ClassSource> getClasses() {
    if (nameToSrc.isEmpty()) {
      for (Path path : cpEntries.keySet()) {
        final INamespace ns = nsForPath(path);
        cpEntries.put(path, ns);
        for (ClassSource classSource : ns.getClasses()) {
          nameToSrc.put(classSource.getName(), classSource);
        }
      }
    }

    return nameToSrc.values();
  }

  @Override
  public Optional<ClassSource> getClass(ClassSignature className) {
    if (nameToSrc.isEmpty()) {
      getClasses();
    }
    return Optional.of(nameToSrc.get(className));
  }

  private INamespace nsForPath(Path path) {
    if (java.nio.file.Files.isDirectory(path)) {
      return new PathBasedNamespace(classProvider, path);
    } else {
      final String fileExtension = Files.getFileExtension(path.getFileName().toString());

      if (fileExtension.equals("zip") || fileExtension.equals("jar")) {
        try {
          return new PathBasedNamespace(
              classProvider, Paths.get(new URI("jar:" + path.toAbsolutePath().toString())));
        } catch (URISyntaxException e) {
          logger.warn("Invalid class path entry: " + path);
        }
      } else {
        logger.warn("Invalid/Unknown class path entry: " + path);
      }
    }

    throw new IllegalStateException("Empty class path");
  }
}
