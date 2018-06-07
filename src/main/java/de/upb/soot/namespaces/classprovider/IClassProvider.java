package de.upb.soot.namespaces.classprovider;

import de.upb.soot.namespaces.FileType;
import de.upb.soot.namespaces.INamespace;

import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Responsible for creating {@link ClassSource}es based on the handled file type (.class, .jimple, .java, .dex, etc).
 *
 * @author Manuel Benz created on 22.05.18
 */
public interface IClassProvider {

  /**
   * Creates and returns a {@link ClassSource} for a specific source file. The file should be passed as {@link Path} and can
   * be located in an arbitrary {@link java.nio.file.FileSystem}. Implementations should use
   * {@link java.nio.file.Files#newInputStream(Path, OpenOption...)} to access the file.
   * 
   * @param ns
   *          The {@link INamespace} that holds the given file
   * @param sourcePath
   *          Path to the source file of the to-be-created {@link ClassSource}. The given path has to exist and requires to
   *          be handled by this {@link IClassProvider}. Implementations might double check this if wanted.
   * @return A not yet resolved {@link ClassSource}, backed up by the given file
   */
  Optional<ClassSource> getClass(INamespace ns, Path sourcePath);

  /**
   * Returns the file type that is handled by this provider, e.g. class, jimple, java
   * 
   * @return
   */
  FileType getHandledFileType();
}
