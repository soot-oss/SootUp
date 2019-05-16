package de.upb.soot.namespaces;

import de.upb.soot.util.Utils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

public class JavaNamespaceUtils {
  private static final @Nonnull String WILDCARD_CHAR = "*";

  /**
   * Explode the class or modulepath entries, separated by {@link File#pathSeparator}.
   *
   * @param paths entries as one string
   * @return path entries
   */
  public static @Nonnull Stream<Path> explode(@Nonnull String paths) {
    // the classpath is split at every path separator which is not escaped
    String regex = "(?<!\\\\)" + Pattern.quote(File.pathSeparator);
    final Stream<Path> exploded =
        Stream.of(paths.split(regex)).flatMap(JavaNamespaceUtils::handleWildCards);
    // we need to filter out duplicates of the same files to not generate duplicate namespaces
    return exploded.map(Path::normalize).distinct();
  }

  /**
   * The class path can have directories with wildcards as entries. All jar/JAR files inside those
   * directories have to be added to the class path.
   *
   * @param entry A class path entry
   * @return A stream of class path entries with wildcards exploded
   */
  private static @Nonnull Stream<Path> handleWildCards(@Nonnull String entry) {
    if (entry.endsWith(WILDCARD_CHAR)) {
      Path baseDir = Paths.get(entry.substring(0, entry.indexOf(WILDCARD_CHAR)));
      try {
        return Utils.iteratorToStream(Files.newDirectoryStream(baseDir, "*.{jar,JAR}").iterator());
      } catch (PatternSyntaxException | NotDirectoryException e) {
        throw new JavaClassPathNamespace.InvalidClassPathException("Malformed wildcard entry", e);
      } catch (IOException e) {
        throw new JavaClassPathNamespace.InvalidClassPathException(
            "Couldn't access entries denoted by wildcard", e);
      }
    } else {
      return Stream.of(Paths.get(entry));
    }
  }
}
