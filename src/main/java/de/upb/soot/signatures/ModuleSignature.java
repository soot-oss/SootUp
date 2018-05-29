package de.upb.soot.signatures;

/** Represents a Java 9 module. */
public class ModuleSignature {
  /** The name of the module. */
  public final String moduleName;

  /**
   * Construct Module Signature of a Java 9 module.
   * Instances should only be created a {@link SignatureFactory}
   *
   * @param moduleName module's name
   */
  protected ModuleSignature(final String moduleName) {
    this.moduleName = moduleName;
  }
}
