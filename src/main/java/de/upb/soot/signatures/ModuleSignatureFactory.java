package de.upb.soot.signatures;

import java.util.HashMap;
import java.util.Map;

public class ModuleSignatureFactory extends SignatureFactory {

  protected Map<String, ModuleSignature> modules = new HashMap<>();

  public static final ModuleSignature EMPTY_MODULE_SIGNATURE = new ModuleSignature(null);

  protected ModuleSignatureFactory() {
    modules.put(null, EMPTY_MODULE_SIGNATURE);
  }

  public ModuleSignature getModuleSignature(final String moduleName) {

    ModuleSignature moduleSignature = modules.get(moduleName);
    if (moduleSignature == null) {
      moduleSignature = new ModuleSignature(moduleName);
      modules.put(moduleName, moduleSignature);
    }
    return moduleSignature;
  }

  @Override
  public PackageSignature getPackageSignature(final String packageName) {
    return getPackageSignature(packageName, null);
  }

  public PackageSignature getPackageSignature(final String packageName, final String moduleName) {
    String fqID = moduleName + "." + packageName;
    PackageSignature packageSignature = packages.get(fqID);
    if (packageSignature == null) {
      ModuleSignature moduleSignature = getModuleSignature(moduleName);
      packageSignature = new ModulePackageSignature(packageName, moduleSignature);
      packages.put(fqID, packageSignature);
    }
    return packageSignature;
  }

  @Override
  public ClassSignature getClassSignature(final String className, final String packageName) {
    PackageSignature packageSignature = getPackageSignature(packageName, null);
    ClassSignature classSignature = new ClassSignature(className, packageSignature);
    return classSignature;
  }

  public ClassSignature getClassSignature(
      final String className, final String packageName, final String moduleName) {
    PackageSignature packageSignature = getPackageSignature(packageName, moduleName);
    ClassSignature classSignature = new ClassSignature(className, packageSignature);
    return classSignature;
  }
}
