package de.upb.soot.namespaces.classprovider;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;

import de.upb.soot.namespaces.INamespace;
import de.upb.soot.signatures.ClassSignature;

import java.nio.file.OpenOption;
import java.nio.file.Path;

/**
 * Basic class for storing information that is needed to reify a {@link de.upb.soot.core.SootClass}.
 *
 * @author Manuel Benz created on 22.05.18
 * @author Ben Hermann
 * @author Linghui Luo
 *
 **/
public class ClassSource {
  private final INamespace srcNamespace;
  // TODO: AD unfortunately I need to change it in the ModuleFinder, since I only know a module's name after resolving its
  // module-info.class
  private ClassSignature classSignature;
  private final Path sourcePath;

  /**
   * Creates and a {@link ClassSource} for a specific source file. The file should be passed as {@link Path} and can be
   * located in an arbitrary {@link java.nio.file.FileSystem}. Implementations should use
   * {@link java.nio.file.Files#newInputStream(Path, OpenOption...)} to access the file.
   *
   * @param srcNamespace
   *          The {@link INamespace} that holds the given file
   * @param sourcePath
   *          Path to the source file of the to-be-created {@link ClassSource}. The given path has to exist and requires to
   *          be handled by this {@link IClassProvider}. Implementations might double check this if wanted.
   * @param classSignature
   *          the signature that has been used to resolve this class
   * @return A not yet resolved {@link ClassSource}, backed up by the given file A not yet resolved {@link ClassSource},
   *         backed up by the given file
   */
  public ClassSource(INamespace srcNamespace, Path sourcePath, ClassSignature classSignature) {
    checkNotNull(srcNamespace);

    this.srcNamespace = srcNamespace;
    this.classSignature = classSignature;
    this.sourcePath = sourcePath;
  }

  public ClassSignature getClassSignature() {
    return classSignature;
  }

  public Path getSourcePath() {
    return sourcePath;
  }

  /**
   * Create or provide a representation of the actual manifestation of the class.
   * 
   * @return
   */
  public Object getContent() {
    // TODO: Find a better common supertype for this.
    return srcNamespace.getClassProvider().getContent(this);
  }

  public de.upb.soot.namespaces.classprovider.IClassProvider getClassProvider() {
    return srcNamespace.getClassProvider();
  }

  public void setClassSignature(ClassSignature classSignature) {
    this.classSignature = classSignature;
  }

  /**
   * Even if a the signature changes, the classource remains the same, e.g., if it is associated to an automatic module s
   * 
   * @param o
   *          the object to compare with
   * @return both objects are logically equal
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClassSource that = (ClassSource) o;
    return Objects.equal(srcNamespace, that.srcNamespace) && Objects.equal(sourcePath, that.sourcePath);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(srcNamespace, sourcePath);
  }

}
