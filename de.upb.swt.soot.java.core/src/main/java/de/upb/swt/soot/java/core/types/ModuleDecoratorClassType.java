package de.upb.swt.soot.java.core.types;

import com.google.common.base.Objects;
import de.upb.swt.soot.java.core.signatures.ModuleSignature;

public class ModuleDecoratorClassType extends JavaClassType {

  private final JavaClassType wrappedSignature;
  private final ModuleSignature moduleSignature;

  /**
   * Decorator for a ClassSignature to add module information.
   *
   * @param classSignature the singature to decorate
   * @param moduleSignature the module signature to add
   */
  public ModuleDecoratorClassType(JavaClassType classSignature, ModuleSignature moduleSignature) {
    super(classSignature.getClassName(), classSignature.getPackageName());
    this.wrappedSignature = classSignature;
    // FIXME: use factory
    this.moduleSignature = moduleSignature;
  }

  public ModuleSignature getModuleSignature() {
    return moduleSignature;
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
    ModuleDecoratorClassType that = (ModuleDecoratorClassType) o;
    boolean moduleEqual = Objects.equal(moduleSignature, that.moduleSignature);
    return moduleEqual
        && ((ModuleDecoratorClassType) o).wrappedSignature.equals(that.wrappedSignature);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), moduleSignature);
  }
}
