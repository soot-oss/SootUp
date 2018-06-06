package de.upb.soot.ns;

import com.google.common.io.Files;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.soot.ns.classprovider.ClassSource;
import de.upb.soot.ns.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;

/** @author Manuel Benz created on 22.05.18 */
public class JavaCPNamespace extends AbstractNamespace {
  private static final Logger logger = LoggerFactory.getLogger(JavaCPNamespace.class);

  private Collection<AbstractNamespace> cpEntries;

  public JavaCPNamespace(IClassProvider classProvider, String classPath) {
    super(classProvider);
    cpEntries = explode(classPath).map(cp -> nsForPath(cp)).collect(Collectors.toList());
  }

  private Stream<Path> explode(String classPath) {
    // the classpath is split at every path separator which is not escaped
    String regex = "(?<!\\\\)" + Pattern.quote(File.pathSeparator);
    // TODO implement support for class path wildcards, e.g., lib/*.
    // https://docs.oracle.com/javase/8/docs/technotes/tools/windows/classpath.html
    return Stream.of(classPath.split(regex)).map(s -> Paths.get(s));
  }

  @Override
  public Collection<ClassSource> getClassSources() {
    // By using a set here, already added classes won't be overwritten and the class which is found
    // first will be kept
    Set<ClassSource> found = new HashSet<>();
    for (AbstractNamespace ns : cpEntries) {
      found.addAll(ns.getClassSources());
    }
    return found;
  }

  @Override
  public ClassSource getClassSource(ClassSignature signature) throws SootClassNotFoundException {
    for (AbstractNamespace ns : cpEntries) {
      try {
        final ClassSource classSource = ns.getClassSource(signature);
        return classSource;
      } catch (SootClassNotFoundException e) {
        // the next namespace might still contain the class
      }
    }

    throw new SootClassNotFoundException(signature);
  }

  private AbstractNamespace nsForPath(Path path) {
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
