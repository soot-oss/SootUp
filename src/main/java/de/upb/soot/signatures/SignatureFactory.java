package de.upb.soot.signatures;

import java.util.HashMap;
import java.util.Map;

public class SignatureFactory {

  private static Map<String, ModuleSignature> modules = new HashMap<>();
  private static Map<String, PackageSignature> packages = new HashMap<>();

  public static ModuleSignature getModuleSignature(String moduleName) {
    if (moduleName == null) {
      return null;
    }
    ModuleSignature moduleSignature = modules.get(moduleName);
    if (moduleSignature == null) {
      moduleSignature = new ModuleSignature(moduleName);
      modules.put(moduleName, moduleSignature);
    }
    return moduleSignature;
  }

  public static PackageSignature getPackageSignature(String packageName, String moduleName) {
    String fqID = moduleName + "." + packageName;
    PackageSignature packageSignature = packages.get(fqID);
    if (packageSignature == null) {
      ModuleSignature moduleSignature = getModuleSignature(moduleName);
      packageSignature = new PackageSignature(packageName, moduleSignature);
      packages.put(fqID, packageSignature);
    }
    return packageSignature;
  }

  public static PackageSignature getPackageSignature(String packageName) {
    return getPackageSignature(packageName, null);
  }

  public static ClassSignature getClassSignature(
      String className, String packageName, String moduleName) {
    PackageSignature packageSignature = getPackageSignature(packageName, moduleName);
    ClassSignature classSignature = new ClassSignature(className, packageSignature);
    return classSignature;
  }

  public static ClassSignature getClassSignature(String className, String packageName) {
    return getClassSignature(className, packageName, null);
  }

  public static MethodSignature getMethodSignature(
      String methodName, ClassSignature classSignature) {
    MethodSignature methodSignature = new MethodSignature(methodName, classSignature);
    return methodSignature;
  }
}
