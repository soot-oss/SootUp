package de.upb.soot.signatures;

import java.util.HashMap;
import java.util.Map;

public class SignatureFactory {

  private static Map<String, ModuleSignature> modules = new HashMap<>();
  private static Map<String, PackageSignature> packages = new HashMap<>();
  private static Map<String, ClassSignature> classes = new HashMap<>();
  private static Map<String, MethodSignature> methods = new HashMap<>();

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
    String fqID = moduleName + "." + packageName + "." + className;
    ClassSignature classSignature = classes.get(fqID);
    if (classSignature == null) {
      PackageSignature packageSignature = getPackageSignature(packageName, moduleName);
      classSignature = new ClassSignature(className, packageSignature);
      classes.put(fqID, classSignature);
    }
    return classSignature;
  }

  public static ClassSignature getClassSignature(String className, String packageName) {
    return getClassSignature(className, packageName, null);
  }

  public static MethodSignature getMethodSignature(
      String methodName, String className, String packageName, String moduleName) {
    String fqID = moduleName + "." + packageName + "." + className + "." + methodName;
    MethodSignature methodSignature = methods.get(fqID);
    if (methodSignature == null) {
      ClassSignature classSignature = getClassSignature(packageName, className);
      methodSignature = new MethodSignature(methodName, classSignature);
      methods.put(fqID, methodSignature);
    }
    return methodSignature;
  }

  public static MethodSignature getMethodSignature(
      String methodName, String className, String packageName) {
    return getMethodSignature(methodName, className, packageName, null);
  }
}
