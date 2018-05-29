package de.upb.soot.signatures;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory to create valid signatures for Java classes in a classpath.
 *
 * @author Andreas Dann
 */
public class SignatureFactory {

  /** Caches the created signatures for packages. */
  protected Map<String, PackageSignature> packages = new HashMap<>();

  protected SignatureFactory() {}

  /**
   * Returns a unique PackageSignature.
   * The method looks up a cache if it already contains a signature with the
   * given package name. If the cache lookup fails a new signature is created.
   *
   * @param packageName the Java package name
   * @return a PackageSignature
   */
  public PackageSignature getPackageSignature(final String packageName) {
    PackageSignature packageSignature = packages.get(packageName);
    if (packageSignature == null) {
      packageSignature = new PackageSignature(packageName);
      packages.put(packageName, packageSignature);
    }
    return packageSignature;
  }

  /**
   * Always creates a new ClassSignature. In opposite to PackageSignatures, ClassSignatures are not
   * cached because the are unique per class, and thus reusing them does not make sense.
   *
   * @param className the simple name of the class
   * @param packageName the Java package name
   * @return a ClassSignature for a Java class
   */
  public ClassSignature getClassSignature(final String className, final String packageName) {
    PackageSignature packageSignature = getPackageSignature(packageName);
    ClassSignature classSignature = new ClassSignature(className, packageSignature);
    return classSignature;
  }

  /**
   * Always creates a new MethodSignature.
   *
   * @param methodName the signature of the method
   * @param classSignature the signature of the declaring class
   * @return a MethodSignature
   */
  public MethodSignature getMethodSignature(
      final String methodName, final ClassSignature classSignature) {
    MethodSignature methodSignature = new MethodSignature(methodName, classSignature);
    return methodSignature;
  }
}
