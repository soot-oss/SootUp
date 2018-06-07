package de.upb.soot.namespaces;

import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.SignatureFactory;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

/**
 * Common functionality useful to cope with {@link Path}s.
 *
 * @author Manuel Benz created on 06.06.18
 */
public class PathUtils {


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
