package de.upb.soot.signatures;

import com.google.common.base.Objects;

/** Represents a Java 9 module. */
public class ModuleSignature {
  /** The unnamed module. */
  public static final ModuleSignature UNNAMED_MODULE_SIGNATURE = new ModuleSignature(null);
  /** The name of the module. */
  public final String moduleName;

  /**
   * Construct Module Signature of a Java 9 module. Instances should only be created a {@link
   * SignatureFactory}
   *
   * @param moduleName module's name
   */
  protected ModuleSignature(final String moduleName) {
    this.moduleName = moduleName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ModuleSignature that = (ModuleSignature) o;
    return Objects.equal(moduleName, that.moduleName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(moduleName);
  }
}
