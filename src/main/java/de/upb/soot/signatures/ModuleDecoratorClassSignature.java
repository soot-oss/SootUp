package de.upb.soot.signatures;

import com.google.common.base.Objects;

public class ModuleDecoratorClassSignature extends ClassSignature {

  private static final ModuleSignatureFactory factory = new ModuleSignatureFactory();

  private final ClassSignature wrappedSignature;
  private final ModuleSignature moduleSignature;

  /**
   * Decorator for a ClassSignature to add module information.
   * @param classSignature the singature to decorate
   * @param moduleSignature the module signature to add
   */
  public ModuleDecoratorClassSignature(ClassSignature classSignature, ModuleSignature moduleSignature) {
    super(classSignature.className, classSignature.packageSignature);
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
    ModuleDecoratorClassSignature that = (ModuleDecoratorClassSignature) o;
    boolean moduleEqual = Objects.equal(moduleSignature, that.moduleSignature);
    return moduleEqual && ((ModuleDecoratorClassSignature) o).wrappedSignature.equals(that.wrappedSignature);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), moduleSignature);
  }
}
