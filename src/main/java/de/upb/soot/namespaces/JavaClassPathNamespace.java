package de.upb.soot.namespaces;

import static com.google.common.base.Strings.isNullOrEmpty;

import de.upb.soot.Utils;
import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.SignatureFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
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

/**
 * An implementation of the {@link INamespace} interface for the Java class path. Handles directories, archives (including
 * wildcard denoted archives) as stated in the official documentation:
 * https://docs.oracle.com/javase/8/docs/technotes/tools/windows/classpath.html
 * 
 * @author Manuel Benz created on 22.05.18
 */
public class JavaClassPathNamespace extends AbstractNamespace {
  private static final Logger logger = LoggerFactory.getLogger(JavaClassPathNamespace.class);

  protected Collection<AbstractNamespace> cpEntries;

  /**
   * Creates a {@link JavaClassPathNamespace} which locates classes based on the provided {@link IClassProvider}.
   * 
   * @param classProvider
   *          The {@link IClassProvider} for generating {@link ClassSource}es for the files found on the class path
   * @param classPath
   *          The class path to search in
   */
  public JavaClassPathNamespace(IClassProvider classProvider, String classPath) {
    super(classProvider);

    if (isNullOrEmpty(classPath)) {
      throw new InvalidClassPathException("Empty class path given");
    }

    try {
      cpEntries = explode(classPath).flatMap(cp -> Utils.optionalToStream(nsForPath(cp))).collect(Collectors.toList());
          = PathUtils.explode(classPath).flatMap(cp -> Utils.optionalToStream(nsForPath(cp))).distinct().collect(Collectors.toList());
    } catch (IllegalArgumentException e) {
      throw new InvalidClassPathException("Malformed class path given: " + classPath, e);
    }

    if (cpEntries.isEmpty()) {
      throw new InvalidClassPathException("Empty class path given");
    }

    logger.trace("{} class path entries registered", cpEntries.size());
  }

    // we need to filter out duplicates of the same files to not generate duplicate namespaces
    return exploded.map(cp -> cp.normalize()).distinct();


  @Override
  public Collection<ClassSource> getClassSources(SignatureFactory factory) {
    // By using a set here, already added classes won't be overwritten and the class which is found
    // first will be kept
    Set<ClassSource> found = new HashSet<>();
    for (AbstractNamespace ns : cpEntries) {
      found.addAll(ns.getClassSources(factory));
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
    if (Files.exists(path)
        && (java.nio.file.Files.isDirectory(path) || PathUtils.hasExtension(path, FileType.JAR, FileType.ZIP))) {
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
