package de.upb.soot.signatures;

import de.upb.soot.namespaces.FileType;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

/** Represents the unique fully-qualified name of a Class (aka its signature). */
public class JavaClassSignature extends TypeSignature {

  /** The simple class name. */
  public final String className;

  /** The package in which the class resides. */
  public final PackageSignature packageSignature;

  /**
   * Internal: Constructs the fully-qualified ClassSignature. Instances should only be created by a
   * {@link DefaultSignatureFactory}
   *
   * @param className
   *          the simple name of the class, e.g., ClassA NOT my.package.ClassA
   * @param packageSignature
   *          the corresponding package
   */
  protected JavaClassSignature(final String className, final PackageSignature packageSignature) {
    this.className = className;
    this.packageSignature = packageSignature;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    JavaClassSignature that = (JavaClassSignature) o;
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
    if (!Strings.isNullOrEmpty(packageSignature.packageName)) {
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

  public boolean isModuleInfo() {
    return this.className.equals(ModuleSignatureFactory.MODULE_INFO_CLASS.className);
  }
}
