package de.upb.soot.signatures;

import java.util.HashMap;
import java.util.Map;

/** @author Andreas Dann */
public class SignatureFactory {

  protected Map<String, PackageSignature> packages = new HashMap<>();

  protected SignatureFactory() {}

  public PackageSignature getPackageSignature(final String packageName) {
    PackageSignature packageSignature = packages.get(packageName);
    if (packageSignature == null) {
      packageSignature = new PackageSignature(packageName);
      packages.put(packageName, packageSignature);
    }
    return packageSignature;
  }

  public ClassSignature getClassSignature(final String className, final String packageName) {
    PackageSignature packageSignature = getPackageSignature(packageName);
    ClassSignature classSignature = new ClassSignature(className, packageSignature);
    return classSignature;
  }

  public MethodSignature getMethodSignature(
      final String methodName, final ClassSignature classSignature) {
    MethodSignature methodSignature = new MethodSignature(methodName, classSignature);
    return methodSignature;
  }
}
