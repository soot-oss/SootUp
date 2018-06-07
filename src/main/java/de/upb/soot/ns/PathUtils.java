package de.upb.soot.ns;

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

/** @author Manuel Benz created on 06.06.18 */
public class PathUtils {
  public static Path pathFromSignature(ClassSignature signature) {
    return pathFromSignature(signature, FileSystems.getDefault());
  }

  public static Path pathFromSignature(ClassSignature signature, FileSystem fs) {
    return fs.getPath(signature.getFullyQualifiedName().replace('.', '/'));
  }

  public static ClassSignature signatureFromPath(Path path, SignatureFactory fac) {
    return fac.getClassSignature(FilenameUtils.removeExtension(path.toString()).replace('/', '.'));
  }

  /**
   * Matches the given path with the given file extension (without a leading dot) and returns true if the path ends with this
   * extension, false otherwise.
   * 
   * @param path
   *          An arbitrary path
   * @param extensions
   *          One or more file extensions without a leading dot (e.g., java, class, jimple)
   * @return True if the path ends with one of the given extensions, false otherwise.
   */
  public static boolean hasExtension(Path path, FileType... extensions) {
    return hasExtension(path, Arrays.asList(extensions));
  }

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
