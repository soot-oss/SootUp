package de.upb.soot.signatures;

/**
 * Represents a Java Package.
 *
 * @author Andreas Dann
 */
public class PackageSignature {
  /** The name of the package. */
  public final String packageName;

  /**
   * Internal: Construct Package Signature of a Java package.
   * Instances should only be created a {@link SignatureFactory)
   *
   * @param packageName the package's name
   */
  protected PackageSignature(final String packageName) {
    this.packageName = packageName;
  }
}
