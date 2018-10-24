package de.upb.soot.signatures;

import com.google.common.base.Objects;

/** Represents a Java 9 module. */
public class ModuleSignature implements ISignature {
  /**
   * The unnamed module. If a request is made to load a type whose package is not defined in any module then the module
   * system load it from the classpath. To ensure that every type is associated with a module, the type is associated with
   * the unnamed module. @see <a
   * href=http://openjdk.java.net/projects/jigsaw/spec/sotms/#the-unnamed-module>http://openjdk.java.net/projects/jigsaw/spec/sotms/#the-unnamed-module</a>
   */
  public static final ModuleSignature UNNAMED_MODULE = new ModuleSignature("");

  /** The name of the module. */
  public final String moduleName;

  /**
   * Construct Module Signature of a Java 9 module. Instances should only be created a {@link DefaultSignatureFactory}
   *
   * @param moduleName
   *          module's name
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

  @Override
  public String toString() {
    return moduleName;
  }
}
