package de.upb.soot.ns;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;

import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.SignatureFactory;

/** @author Manuel Benz created on 06.06.18 */
public class PathUtils {
  public static Path pathFromSignature(ClassSignature signature) {
    return pathFromSignature(signature, FileSystems.getDefault());
  }

  public static Path pathFromSignature(ClassSignature signature, FileSystem fs) {
    return fs.getPath(signature.getFullyQualifiedName().replace('.', '/') + ".class");
  }

  public static ClassSignature signatureFromPath(Path path, SignatureFactory fac) {
    if (!hasExtension(path.getFileName(), "class")) {
      throw new IllegalArgumentException("Given path is not a class file " + path);
    }
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
  public static boolean hasExtension(Path path, String... extensions) {
    if (Files.isDirectory(path)) {
      return false;
    }
    return path.getFileSystem().getPathMatcher("glob:*.{" + String.join(",", extensions) + "}").matches(path.getFileName());
  }

  public static boolean isArchive(Path path) {
    return hasExtension(path, "jar", "apk", "zip");
  }
}
