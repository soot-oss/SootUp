package de.upb.soot.signatures;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory to create valid signatures for Java classes in a modulepath.
 *
 * @author Andreas Dann
 */
public class ModuleSignatureFactory extends SignatureFactory {

  protected Map<String, ModuleSignature> modules = new HashMap<>();

  protected ModuleSignatureFactory() {

    /**
     * Represents the unnamed module in Java's module system. Every type that is not defined in any
     * known module but loaded from the classpath is associated with this unnamed module, so as to
     * ensure that every type is associated with a module.
     *
     * <p>{@link ModuleSignature#UNNAMED_MODULE}
     */
    modules.put(
        ModuleSignature.UNNAMED_MODULE.moduleName,
        ModuleSignature.UNNAMED_MODULE);
  }

  /**
   * Returns a unique ModuleSignature. The method looks up a cache if it already contains a
   * signature with the given module name. If the cache lookup fails a new signature is created.
   *
   * @param moduleName the module name; must not be null use empty string for the unnamed module
   * @return a ModuleSignature
   */
  public ModuleSignature getModuleSignature(final String moduleName) {
    Preconditions.checkNotNull(moduleName);
    ModuleSignature moduleSignature = modules.get(moduleName);
    if (moduleSignature == null) {
      moduleSignature = new ModuleSignature(moduleName);
      modules.put(moduleName, moduleSignature);
    }
    return moduleSignature;
  }

  @Override
  public ModulePackageSignature getPackageSignature(final String packageName) {
    return getPackageSignature(packageName, ModuleSignature.UNNAMED_MODULE.moduleName);
  }

  /**
   * Returns a unique PackageSignature. The method looks up a cache if it already contains a
   * signature with the given package and module name. If the cache lookup fails a new signature is
   * created.
   *
   * @param packageName the package name; must not be null use empty string for the default package
   * @param moduleName the module containing the package; must not be null use empty string for the
   *     unnamed module {@link ModuleSignature#UNNAMED_MODULE}
   * @return a ModulePackageSignature
   */
  public ModulePackageSignature getPackageSignature(
      final String packageName, final String moduleName) {
    Preconditions.checkNotNull(moduleName);
    Preconditions.checkNotNull(packageName);
    String fqId = moduleName + "." + packageName;
    ModulePackageSignature packageSignature = (ModulePackageSignature) packages.get(fqId);
    if (packageSignature == null) {
      ModuleSignature moduleSignature = getModuleSignature(moduleName);
      packageSignature = new ModulePackageSignature(packageName, moduleSignature);
      packages.put(fqId, packageSignature);
    }
    return packageSignature;
  }

  @Override
  public ClassSignature getClassSignature(final String className, final String packageName) {
    return getClassSignature(
        className, packageName, ModuleSignature.UNNAMED_MODULE.moduleName);
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
   */
  public ClassSignature getClassSignature(
      final String className, final String packageName, final String moduleName) {
    PackageSignature packageSignature = getPackageSignature(packageName, moduleName);
    ClassSignature classSignature = new ClassSignature(className, packageSignature);
    return classSignature;
  }
}
