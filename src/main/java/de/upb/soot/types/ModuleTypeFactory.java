package de.upb.soot.types;

import de.upb.soot.signatures.ModuleSignature;
import de.upb.soot.signatures.ModuleSignatureFactory;
import de.upb.soot.signatures.PackageSignature;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class ModuleTypeFactory extends DefaultTypeFactory {

  @Nonnull private final Supplier<ModuleSignatureFactory> signatureFactorySupplier;

  public ModuleTypeFactory(@Nonnull Supplier<ModuleSignatureFactory> signatureFactorySupplier) {
    super(signatureFactorySupplier);
    this.signatureFactorySupplier = signatureFactorySupplier;
  }

  @Override
  public JavaClassType getClassType(final String className, final String packageName) {
    return getClassType(className, packageName, ModuleSignature.UNNAMED_MODULE.getModuleName());
  }

  /**
   * Always creates a new ClassSignature. In opposite to PackageSignatures and ModuleSignatures,
   * ClassSignatures are not cached because the are unique per class, and thus reusing them does not
   * make sense.
   *
   * @param className the simple name of the class
   * @param packageName the declaring package
   * @param moduleName the declaring module
   * @return a ClassSignature for a Java 9 class
   * @throws NullPointerException if the given module name or package name is null. Use the empty
   *     string to denote the unnamed module or the default package.
   */
  public JavaClassType getClassType(
      final String className, final String packageName, final String moduleName) {
    PackageSignature packageSignature =
        signatureFactorySupplier.get().getPackageSignature(packageName, moduleName);
    return new JavaClassType(className, packageSignature);
  }
}
