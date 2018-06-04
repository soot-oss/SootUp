package de.upb.soot.signatures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;

/**
 * Factory to create valid signatures for Java classes in a classpath.
 *
 * @author Andreas Dann
 */
public class SignatureFactory {

  /** Caches the created signatures for packages. */
  protected Map<String, PackageSignature> packages = new HashMap<>();

  protected SignatureFactory() {
    // TODO: Does it make sense to create/initalize default signatures for primitives, int, byte,
    // boolean, ...?

  }

  /**
   * Returns a unique PackageSignature. The method looks up a cache if it already contains a
   * signature with the given package name. If the cache lookup fails a new signature is created.
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
   * Always creates a new ClassSignature.
   *
   * @param fullyQualifiedClassName the fully-qualified name of the class
   * @return a ClassSignature for a Java Class
   */
  public ClassSignature getClassSignature(final String fullyQualifiedClassName) {
    String className = ClassUtils.getShortClassName(fullyQualifiedClassName);
    String packageName = ClassUtils.getPackageName(fullyQualifiedClassName);
    return getClassSignature(className, packageName);
  }

  /**
   * Always creates a new MethodSignature.
   *
   * @param methodName the method's name
   * @param fqDeclaringClassName the fully-qualified name of the declaring class
   * @param parameters the methods parameters fullyqualified
   * @param fqReturnType the fully-qualified name of the return type
   * @return a MethodSignature
   */
  public MethodSignature getMethodSignature(
      final String methodName,
      final String fqDeclaringClassName,
      final List<String> parameters,
      final String fqReturnType) {
    ClassSignature declaringClass = getClassSignature(fqDeclaringClassName);
    ClassSignature returnTypeSignature = getClassSignature(fqReturnType);
    List<ClassSignature> parameterSignatures = new ArrayList<>();
    for (String fqParameterName : parameters) {
      ClassSignature parameterSignature = getClassSignature(fqParameterName);
      parameterSignatures.add(parameterSignature);
    }
    MethodSignature methodSignature =
        new MethodSignature(methodName, declaringClass, parameterSignatures, returnTypeSignature);
    return methodSignature;
  }
}
