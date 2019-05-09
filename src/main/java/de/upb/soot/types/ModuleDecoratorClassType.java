package de.upb.soot.types;

import com.google.common.base.Objects;
import de.upb.soot.ModuleFactories;
import de.upb.soot.signatures.ModuleSignature;
import de.upb.soot.signatures.ModuleSignatureFactory;

public class ModuleDecoratorClassType extends JavaClassType {

  private static final ModuleFactories FACTORIES = ModuleFactories.create();
  private static final ModuleTypeFactory TYPE_FACTORY = FACTORIES.getTypeFactory();
  private static final ModuleSignatureFactory SIGNATURE_FACTORY = FACTORIES.getSignatureFactory();

  private final JavaClassType wrappedSignature;
  private final ModuleSignature moduleSignature;

  /**
   * Decorator for a ClassSignature to add module information.
   *
   * @param classSignature the singature to decorate
   * @param moduleSignature the module signature to add
   */
  public ModuleDecoratorClassType(JavaClassType classSignature, ModuleSignature moduleSignature) {
    super(classSignature.getClassName(), classSignature.getPackageSignature());
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
