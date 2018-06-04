package de.upb.soot.signatures;

import com.google.common.base.Objects;

/**
 * Represents a Java Package.
 *
 * @author Andreas Dann
 */
public class PackageSignature {
  /** The name of the package. */
  public final String packageName;

  /**
   * Internal: Constructs a Package Signature of a Java package. Instances should only be created by
   * a {@link SignatureFactory}
   *
   * @param packageName the package's name
   */
  protected PackageSignature(final String packageName) {
    this.packageName = packageName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PackageSignature that = (PackageSignature) o;
    return Objects.equal(packageName, that.packageName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(packageName);
  }
}
