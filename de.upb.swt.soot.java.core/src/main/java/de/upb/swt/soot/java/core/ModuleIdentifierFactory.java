package de.upb.swt.soot.java.core;

import com.google.common.base.Preconditions;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.java.core.signatures.ModulePackageName;
import de.upb.swt.soot.java.core.signatures.ModuleSignature;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.HashMap;
import java.util.Map;

public final class ModuleIdentifierFactory extends JavaIdentifierFactory {

  public static final JavaClassType MODULE_INFO_CLASS =
      new JavaClassType("module-info", PackageName.DEFAULT_PACKAGE);

  private static final Map<String, ModuleSignature> modules = new HashMap<>();

  private static final ModuleIdentifierFactory INSTANCE = new ModuleIdentifierFactory();

  public static ModuleIdentifierFactory getInstance() {
    return INSTANCE;
  }

  static {
    /*
     * Represents the unnamed module in Java's module system. Every type that is not defined in any known module but loaded
     * from the classpath is associated with this unnamed module, so as to ensure that every type is associated with a
     * module.
     *
     * <p>{@link ModuleSignature#UNNAMED_MODULE}
     */
    modules.put(ModuleSignature.UNNAMED_MODULE.getModuleName(), ModuleSignature.UNNAMED_MODULE);
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
    PackageName packageIdentifier = getPackageSignature(packageName, moduleName);
    return new JavaClassType(className, packageIdentifier);
  }

  /**
   * Returns a unique ModuleSignature. The methodRef looks up a cache if it already contains a
   * signature with the given module name. If the cache lookup fails a new signature is created.
   * Returns a unique ModuleSignature. The methodRef looks up a cache if it already contains a
   * signature with the given module name. If the cache lookup fails a new signature is created.
   *
   * @param moduleName the module name; Must not be null. Use the empty string for the unnamed
   *     module
   * @return a ModuleSignature
   * @throws NullPointerException if the given module name is null. Use the empty string to denote
   *     the unnamed module.
   */
  public static ModuleSignature getModuleSignature(final String moduleName) {
    Preconditions.checkNotNull(moduleName);
    ModuleSignature moduleSignature = modules.get(moduleName);
    if (moduleSignature == null) {
      moduleSignature = new ModuleSignature(moduleName);
      modules.put(moduleName, moduleSignature);
    }
    return moduleSignature;
  }

  @Override
  public ModulePackageName getPackageName(final String packageName) {
    return getPackageSignature(packageName, ModuleSignature.UNNAMED_MODULE.getModuleName());
  }

  /**
   * Returns a unique PackageName. The methodRef looks up a cache if it already contains a signature
   * with the given package and module name. If the cache lookup fails a new signature is created.
   *
   * @param packageName the package name; must not be null use empty string for the default package
   * @param moduleName the module containing the package; must not be null use empty string for the
   *     unnamed module {@link ModuleSignature#UNNAMED_MODULE}
   * @return a ModulePackageName
   * @throws NullPointerException if the given module name or package name is null. Use the empty
   *     string to denote the unnamed module or the default package.
   */
  public ModulePackageName getPackageSignature(final String packageName, final String moduleName) {
    Preconditions.checkNotNull(moduleName);
    Preconditions.checkNotNull(packageName);
    String fqId = moduleName + "." + packageName;
    ModulePackageName packageSignature = (ModulePackageName) packages.get(fqId);
    if (packageSignature == null) {
      ModuleSignature moduleSignature = getModuleSignature(moduleName);
      packageSignature = new ModulePackageName(packageName, moduleSignature);
      packages.put(fqId, packageSignature);
    }
    return packageSignature;
  }
}
