package de.upb.soot.signatures;

import com.google.common.base.Objects;

/** Represents the signature of a Java 9 package, referencing its module. */
public class ModulePackageSignature extends PackageSignature {

  /** The module in which this package resides. */
  public final ModuleSignature moduleSignature;

  /**
   * Internal: Constructs a Package Signature for Java 9 Packages. Instances should only be created by a
   * {@link DefaultSignatureFactory}
   *
   * @param packageName
   *          the package's name
   * @param moduleSignature
   *          the module declaring the package
   */
  protected ModulePackageSignature(final String packageName, final ModuleSignature moduleSignature) {
    super(packageName);
    this.moduleSignature = moduleSignature;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ModulePackageSignature that = (ModulePackageSignature) o;
    return Objects.equal(moduleSignature, that.moduleSignature) && Objects.equal(packageName, that.packageName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), moduleSignature);
  }
}
