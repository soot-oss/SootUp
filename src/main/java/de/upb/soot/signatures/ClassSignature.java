package de.upb.soot.signatures;

/** Represents the unique fully-qualified name of a Class (aka its signature). */
public class ClassSignature {

  /** The simple class name. */
  public final String className;

  /** The package in which the class resides. */
  public final PackageSignature packageSignature;

  /**
   * Internal: Constructs the fully-qualified ClassSignature. Instances should only be created by a
   * {@link SignatureFactory}
   *
   * @param className the simple name of the class, e.g., ClassA NOT my.package.ClassA
   * @param packageSignature the corresponding package
   */
  protected ClassSignature(final String className, final PackageSignature packageSignature) {
    this.className = className;
    this.packageSignature = packageSignature;
  }
}
