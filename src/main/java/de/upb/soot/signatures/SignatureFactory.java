package de.upb.soot.signatures;

import com.google.common.base.Preconditions;

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
    /** Represents the default package. */
    packages.put(PackageSignature.DEFAULT_PACKAGE.packageName, PackageSignature.DEFAULT_PACKAGE);
  }

  /**
   * Returns a unique PackageSignature. The method looks up a cache if it already contains a
   * signature with the given package name. If the cache lookup fails a new signature is created.
   *
   * @param packageName the Java package name; must not be null use empty string for the default
   *     package {@link PackageSignature#DEFAULT_PACKAGE}
   * @return a PackageSignature
   */
  public PackageSignature getPackageSignature(final String packageName) {
    Preconditions.checkNotNull(packageName);
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
   * @param className
   * @param packageName the Java package name; must not be null use empty string for the default
   *     package {@link PackageSignature#DEFAULT_PACKAGE} the Java package name
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
   * Returns a TypeSignature which can be a {@link ClassSignature},{@link PrimitiveTypeSignature},
   * {@link VoidTypeSignature}, or {@link NullTypeSignature}.
   *
   * @param typeName the fully-qualified name of the class or for primitives its simple name, e.g.,
   *     int, null, void, ...
   * @return the type signature
   */
  public TypeSignature getTypeSignature(final String typeName) {
    switch (typeName.toLowerCase()) {
      case "byte":
        return PrimitiveTypeSignature.BYTE_TYPE_SIGNATURE;
      case "short":
        return PrimitiveTypeSignature.SHORT_TYPE_SIGNATURE;
      case "int":
        return PrimitiveTypeSignature.INT_TYPE_SIGNATURE;
      case "long":
        return PrimitiveTypeSignature.LONG_TYPE_SIGNATURE;
      case "float":
        return PrimitiveTypeSignature.FLOAT_TYPE_SIGNATURE;
      case "double":
        return PrimitiveTypeSignature.DOUBLE_TYPE_SIGNATURE;
      case "char":
        return PrimitiveTypeSignature.CHAR_TYPE_SIGNATURE;
      case "boolean":
        return PrimitiveTypeSignature.BOOLEAN_TYPE_SIGNATURE;
      case "null":
        return NullTypeSignature.NULL_TYPE_SIGNATURE;
      case "void":
        return VoidTypeSignature.VOID_TYPE_SIGNATURE;
      default:
        return getClassSignature(typeName);
    }
  }

  /**
   * Always creates a new MethodSignature AND a new ClassSignature.
   *
   * @param methodName the method's name
   * @param fqDeclaringClassName the fully-qualified name of the declaring class
   * @param parameters the methods parameters fully-qualified name or a primitive's name
   * @param fqReturnType the fully-qualified name of the return type or a primitive's name
   * @return a MethodSignature
   */
  public MethodSignature getMethodSignature(
      final String methodName,
      final String fqDeclaringClassName,
      final String fqReturnType,
      final List<String> parameters) {
    ClassSignature declaringClass = getClassSignature(fqDeclaringClassName);
    TypeSignature returnTypeSignature = getTypeSignature(fqReturnType);
    List<TypeSignature> parameterSignatures = new ArrayList<>();
    for (String fqParameterName : parameters) {
      TypeSignature parameterSignature = getTypeSignature(fqParameterName);
      parameterSignatures.add(parameterSignature);
    }
    MethodSignature methodSignature =
        new MethodSignature(methodName, declaringClass, returnTypeSignature, parameterSignatures);
    return methodSignature;
  }

  /**
   * Always creates a new MethodSignature reusing the given ClassSignature.
   *
   * @param methodName the method's name
   * @param declaringClassSignature the ClassSignature of the declaring class
   * @param parameters the methods parameters fully-qualified name or a primitive's name
   * @param fqReturnType the fully-qualified name of the return type or a primitive's name
   * @return a MethodSignature
   */
  public MethodSignature getMethodSignature(
      final String methodName,
      final ClassSignature declaringClassSignature,
      final String fqReturnType,
      final List<String> parameters) {
    TypeSignature returnTypeSignature = getTypeSignature(fqReturnType);
    List<TypeSignature> parameterSignatures = new ArrayList<>();
    for (String fqParameterName : parameters) {
      TypeSignature parameterSignature = getTypeSignature(fqParameterName);
      parameterSignatures.add(parameterSignature);
    }
    MethodSignature methodSignature =
        new MethodSignature(
            methodName, declaringClassSignature, returnTypeSignature, parameterSignatures);
    return methodSignature;
  }
}
