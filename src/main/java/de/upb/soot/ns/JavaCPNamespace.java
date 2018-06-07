package de.upb.soot.ns;

import com.google.common.base.Strings;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.soot.Utils;
import de.upb.soot.ns.classprovider.ClassSource;
import de.upb.soot.ns.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;

/** @author Manuel Benz created on 22.05.18 */
public class JavaCPNamespace extends AbstractNamespace {
  private static final Logger logger = LoggerFactory.getLogger(JavaCPNamespace.class);

  private Collection<AbstractNamespace> cpEntries;

  public JavaCPNamespace(IClassProvider classProvider, String classPath) {
    super(classProvider);

    if (Strings.isNullOrEmpty(classPath)) {
      throw new InvalidClassPathException("Empty class path given");
    }

    try {
      cpEntries = explode(classPath).flatMap(cp -> Utils.optionalToStream(nsForPath(cp))).collect(Collectors.toList());
    } catch (IllegalArgumentException e) {
      throw new InvalidClassPathException("Malformed class path given: " + classPath, e);
    }

    if (cpEntries.isEmpty()) {
      throw new InvalidClassPathException("Empty class path given");
    }
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
  public Optional<ClassSource> getClassSource(ClassSignature signature) {
    for (AbstractNamespace ns : cpEntries) {
      final Optional<ClassSource> classSource = ns.getClassSource(signature);
      if (classSource.isPresent()) {
        return classSource;
      }
    }
    return Optional.empty();
  }

  private Optional<AbstractNamespace> nsForPath(Path path) {
    if (Files.exists(path) && (java.nio.file.Files.isDirectory(path) || PathUtils.hasExtension(path, "jar", "zip"))) {
      return Optional.of(PathBasedNamespace.createForClassContainer(classProvider, path));
    } else {
      logger.warn("Invalid/Unknown class path entry: " + path);
      return Optional.empty();
    }
  }

  static final class InvalidClassPathException extends IllegalArgumentException {
    public InvalidClassPathException(String s) {
      super(s);
    }

    public InvalidClassPathException(String message, Throwable cause) {
      super(message, cause);
    }

    public InvalidClassPathException(Throwable cause) {
      super(cause);
    }
  }
}
