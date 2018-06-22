package de.upb.soot.namespaces;

import de.upb.soot.Utils;
import de.upb.soot.core.SootClass;
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
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Basic implementation of {@link INamespace}, encapsulating common behavior. Also used to keep the {@link INamespace}
 * interface clean from internal methods like {@link AbstractNamespace#getClassSource(ClassSignature)}.
 *
 * @author Manuel Benz created on 22.05.18
 */
public abstract class AbstractNamespace implements INamespace {
  private static final String WILDCARD_CHAR = "*";
  protected final IClassProvider classProvider;

  public AbstractNamespace(IClassProvider classProvider) {
    this.classProvider = classProvider;
  }

  /**
   * Explode the class or modulepath entries, separated by {@link File#pathSeparator}.
   * 
   * @param paths
   *          entries as one string
   * @return path entries
   */
  public static Stream<Path> explode(String paths) {
    // the classpath is split at every path separator which is not escaped
    String regex = "(?<!\\\\)" + Pattern.quote(File.pathSeparator);
    final Stream<Path> exploded = Stream.of(paths.split(regex)).flatMap(AbstractNamespace::handleWildCards);
    // we need to filter out duplicates of the same files to not generate duplicate namespaces
    return exploded.map(cp -> cp.normalize()).distinct();
  }

  /**
   * The class path can have directories with wildcards as entries. All jar/JAR files inside those directories have to be
   * added to the class path.
   *
   * @param entry
   *          A class path entry
   * @return A stream of class path entries with wildcards exploded
   */
  private static Stream<Path> handleWildCards(String entry) {
    if (entry.endsWith(WILDCARD_CHAR)) {
      Path baseDir = Paths.get(entry.substring(0, entry.indexOf(WILDCARD_CHAR)));
      try {
        return Utils.iteratorToStream(Files.newDirectoryStream(baseDir, "*.{jar,JAR}").iterator());
      } catch (PatternSyntaxException | NotDirectoryException e) {
        throw new JavaClassPathNamespace.InvalidClassPathException("Malformed wildcard entry", e);
      } catch (IOException e) {
        throw new JavaClassPathNamespace.InvalidClassPathException("Couldn't access entries denoted by wildcard", e);
      }
    } else {
      return Stream.of(Paths.get(entry));
    }
  }

  @Override
  public Collection<SootClass> getClasses(SignatureFactory factory) {
    return getClassSources(factory).stream().map(cs -> new SootClass(cs)).collect(Collectors.toList());
  }

  @Override
  public Optional<SootClass> getClass(ClassSignature classSignature) {
    return getClassSource(classSignature).map(cs -> new SootClass(cs));
  }

  protected abstract Collection<ClassSource> getClassSources(SignatureFactory factory);

  protected abstract Optional<ClassSource> getClassSource(ClassSignature classSignature);
}
