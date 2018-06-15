package de.upb.soot.namespaces;

import de.upb.soot.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Common functionality useful to cope with {@link Path}s.
 *
 * @author Manuel Benz created on 06.06.18
 */
public class PathUtils {
  private static final String WILDCARD_CHAR = "*";

  public static Stream<Path> explode(String classPath) {
    // the classpath is split at every path separator which is not escaped
    String regex = "(?<!\\\\)" + Pattern.quote(File.pathSeparator);
    return Stream.of(classPath.split(regex)).flatMap(PathUtils::handleWildCards);
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

  /**
   * Matches the given {@link Path} with the file extensions of the given {@link FileType}s.
   * 
   * @param path
   *          An arbitrary {@link Path}
   * @param extensions
   *          One or more {@link FileType}s to check against
   * @return True if the given {@link Path} has the given {@link FileType}, i.e., the path ends with a dot followed by either
   *         of the extensions defined by the given {@link FileType}s otherwise.
   */
  public static boolean hasExtension(Path path, FileType... extensions) {
    return hasExtension(path, Arrays.asList(extensions));
  }

  /**
   * Matches the given {@link Path} with the file extensions of the given {@link FileType}s.
   *
   * @see PathUtils#hasExtension(Path, FileType...)
   */
  public static boolean hasExtension(Path path, Collection<FileType> extensions) {
    if (Files.isDirectory(path)) {
      return false;
    }
    final String extensionList = extensions.stream().map(ft -> ft.getExtension()).collect(Collectors.joining(","));
    return path.getFileSystem().getPathMatcher("glob:*.{" + extensionList + "}").matches(path.getFileName());
  }

  public static boolean isArchive(Path path) {
    return hasExtension(path, FileType.ARCHIVE_TYPES);
  }
}
