package de.upb.soot.signatures;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import de.upb.soot.namespaces.FileType;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

/** Represents the unique fully-qualified name of a Class (aka its signature). */
public class JavaClassSignature extends TypeSignature {

  /** The simple class name. */
  public final String className;

  /** The package in which the class resides. */
  public final PackageSignature packageSignature;

  /** Whether the class is an inner class **/
  public final boolean isInnerClass;

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
    String realClassName = className;
    boolean innerClass = false;
    // use $ to separate inner and outer class name
    if (realClassName.contains(".")) {
      realClassName = realClassName.replace(".", "$");
    }
    // if the constructor was invoked with an ASM classname
    if (realClassName.contains("$")) {
      innerClass = true;
    }
    this.className = realClassName;
    this.packageSignature = packageSignature;
    this.isInnerClass = innerClass;
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
    return Objects.equal(className, that.className) && Objects.equal(packageSignature, that.packageSignature)
        && isInnerClass == that.isInnerClass;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(className, packageSignature, isInnerClass);
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
    String fileName = getFullyQualifiedName();
    // for a java file the file name of the inner class is the name of outerclass
    // e.g., for an inner class org.acme.Foo$Bar, the filename is org/acme/Foo.java
    if (fileType == FileType.JAVA && this.isInnerClass) {
      int idxInnerClassChar = fileName.indexOf("$");
      if (idxInnerClassChar != -1) {
        fileName = fileName.substring(0, idxInnerClassChar);
      }
    }

    return fs.getPath(fileName.replace('.', '/') + "." + fileType.getExtension());
  }

  public boolean isModuleInfo() {
    return this.className.equals(ModuleSignatureFactory.MODULE_INFO_CLASS.className);
  }
}
