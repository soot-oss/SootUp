package de.upb.soot.signatures;

import com.google.common.base.Objects;

import de.upb.soot.namespaces.FileType;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;

/** Represents the unique fully-qualified name of a Class (aka its signature). */
public class ClassSignature extends TypeSignature {

  /** The simple class name. */
  public final String className;

  /** The package in which the class resides. */
  public final PackageSignature packageSignature;

  /**
   * Internal: Constructs the fully-qualified ClassSignature. Instances should only be created by a {@link SignatureFactory}
   *
   * @param className
   *          the simple name of the class, e.g., ClassA NOT my.package.ClassA
   * @param packageSignature
   *          the corresponding package
   */
  protected ClassSignature(final String className, final PackageSignature packageSignature) {
    this.className = className;
    this.packageSignature = packageSignature;
  }

  public static ClassSignature fromPath(Path path, SignatureFactory fac) {
    return fac.getClassSignature(FilenameUtils.removeExtension(path.toString()).replace('/', '.'));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClassSignature that = (ClassSignature) o;
    return Objects.equal(className, that.className) && Objects.equal(packageSignature, that.packageSignature);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(className, packageSignature);
  }

  /**
   * The fully-qualified name of the class. Concat package and class name , e.g., "java.lang.System".
   *
   * @return fully-qualified name
   */
  public String getFullyQualifiedName() {
    StringBuilder sb = new StringBuilder();
    if (!(packageSignature.packageName == null || packageSignature.packageName.isEmpty())) {
      sb.append(packageSignature.toString());
      sb.append('.');
    }
    sb.append(className);
    return sb.toString();
  }

  @Override
  public String toString() {
    return getFullyQualifiedName();
  }

  public Path toPath(FileType fileType) {
    return toPath(fileType, FileSystems.getDefault());
  }

  public Path toPath(FileType fileType, FileSystem fs) {
    return fs.getPath(getFullyQualifiedName().replace('.', '/') + "." + fileType.getExtension());
  }
}
