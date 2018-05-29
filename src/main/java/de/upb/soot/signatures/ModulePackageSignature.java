package de.upb.soot.signatures;

/** Represents the signature of a Java 9 package, referencing its module. */
public class ModulePackageSignature extends PackageSignature {

  /** The module in which this package resides. */
  public final ModuleSignature moduleSignature;

  /**
   * Internal: Construct Package Signature for Java 9 Packages.
   * Instances should only be created by a {@link SignatureFactory)
   *
   * @param packageName the package's name
   * @param moduleSignature the module declaring the package
   */
  protected ModulePackageSignature(
      final String packageName, final ModuleSignature moduleSignature) {
    super(packageName);
    this.moduleSignature = moduleSignature;
  }
}
