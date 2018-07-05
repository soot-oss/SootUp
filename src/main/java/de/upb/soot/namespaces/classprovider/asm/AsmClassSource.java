package de.upb.soot.namespaces.classprovider.asm;

import de.upb.soot.namespaces.INamespace;
import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.signatures.ClassSignature;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

public class AsmClassSource extends ClassSource {
  /**
   * Creates and a {@link ClassSource} for a specific source file. The file should be passed as {@link Path} and can be
   * located in an arbitrary {@link FileSystem}. Implementations should use {@link Files#newInputStream(Path, OpenOption...)}
   * to access the file.
   *
   * @param srcNamespace
   *          The {@link INamespace} that holds the given file
   * @param sourcePath
   *          Path to the source file of the to-be-created {@link ClassSource}. The given path has to exist and requires to
   *          be handled by this {@link de.upb.soot.namespaces.classprovider.IClassProvider}. Implementations might double check this if wanted.
   * @param classSignature
   *          the signature that has been used to resolve this class
   * @return A not yet resolved {@link ClassSource}, backed up by the given file
   */
  protected AsmClassSource(INamespace srcNamespace, Path sourcePath, ClassSignature classSignature) {
    super(srcNamespace, sourcePath, classSignature);
  }
}
