package de.upb.soot.signatures;

import com.google.common.base.Preconditions;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ClassUtils;

/**
 * Factory to create valid signatures for Java classes in a classpath.
 *
 * @author Andreas Dann
 */
public class DefaultSignatureFactory implements SignatureFactory {

  /** Caches the created signatures for packages. */
  protected final Map<String, PackageSignature> packages = new HashMap<>();

  protected DefaultSignatureFactory() {
    /** Represents the default package. */
    packages.put(PackageSignature.DEFAULT_PACKAGE.packageName, PackageSignature.DEFAULT_PACKAGE);
  }

  /**
   * Returns a unique PackageSignature. The method looks up a cache if it already contains a signature with the given package
   * name. If the cache lookup fails a new signature is created.
   *
   * @param packageName
   *          the Java package name; must not be null use empty string for the default package
   *          {@link PackageSignature#DEFAULT_PACKAGE}
   * @return a PackageSignature
   * @throws NullPointerException
   *           if the given package name is null. Use the empty string to denote the default package.
   */
  @Override
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
   * Always creates a new ClassSignature. In opposite to PackageSignatures, ClassSignatures are not cached because the are
   * unique per class, and thus reusing them does not make sense.
   *
   * @param className
   *          the simple class name
   * @param packageName
   *          the Java package name; must not be null use empty string for the default package
   *          {@link PackageSignature#DEFAULT_PACKAGE} the Java package name
   * @return a ClassSignature for a Java class
   * @throws NullPointerException
   *           if the given package name is null. Use the empty string to denote the default package.
   */
  @Override
  public ClassSignature getClassSignature(final String className, final String packageName) {
    PackageSignature packageSignature = getPackageSignature(packageName);
    return new ClassSignature(className, packageSignature);
  }

  /**
   * Always creates a new ClassSignature.
   *
   * @param fullyQualifiedClassName
   *          the fully-qualified name of the class
   * @return a ClassSignature for a Java Class
   */
  @Override
  public ClassSignature getClassSignature(final String fullyQualifiedClassName) {
    String className = ClassUtils.getShortClassName(fullyQualifiedClassName);
    String packageName = ClassUtils.getPackageName(fullyQualifiedClassName);
    return getClassSignature(className, packageName);
  }

  /**
   * Returns a TypeSignature which can be a {@link ClassSignature},{@link PrimitiveTypeSignature}, {@link VoidTypeSignature},
   * or {@link NullTypeSignature}.
   *
   * @param typeName
   *          the fully-qualified name of the class or for primitives its simple name, e.g., int, null, void, ...
   * @return the type signature
   */
  @Override
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
   * @param methodName
   *          the method's name
   * @param fullyQualifiedNameDeclClass
   *          the fully-qualified name of the declaring class
   * @param parameters
   *          the methods parameters fully-qualified name or a primitive's name
   * @param fqReturnType
   *          the fully-qualified name of the return type or a primitive's name
   * @return a MethodSignature
   */
  @Override
  public MethodSignature getMethodSignature(final String methodName, final String fullyQualifiedNameDeclClass,
      final String fqReturnType, final List<String> parameters) {
    ClassSignature declaringClass = getClassSignature(fullyQualifiedNameDeclClass);
    TypeSignature returnTypeSignature = getTypeSignature(fqReturnType);
    List<TypeSignature> parameterSignatures = new ArrayList<>();
    for (String fqParameterName : parameters) {
      TypeSignature parameterSignature = getTypeSignature(fqParameterName);
      parameterSignatures.add(parameterSignature);
    }
    return new MethodSignature(methodName, declaringClass, returnTypeSignature, parameterSignatures);
  }

  /**
   * Always creates a new MethodSignature reusing the given ClassSignature.
   *
   * @param methodName
   *          the method's name
   * @param declaringClassSignature
   *          the ClassSignature of the declaring class
   * @param parameters
   *          the methods parameters fully-qualified name or a primitive's name
   * @param fqReturnType
   *          the fully-qualified name of the return type or a primitive's name
   * @return a MethodSignature
   */
  @Override
  public MethodSignature getMethodSignature(final String methodName, final ClassSignature declaringClassSignature,
      final String fqReturnType, final List<String> parameters) {
    TypeSignature returnTypeSignature = getTypeSignature(fqReturnType);
    List<TypeSignature> parameterSignatures = new ArrayList<>();
    for (String fqParameterName : parameters) {
      TypeSignature parameterSignature = getTypeSignature(fqParameterName);
      parameterSignatures.add(parameterSignature);
    }
    return new MethodSignature(methodName, declaringClassSignature, returnTypeSignature, parameterSignatures);
  }

  @Override
  public ClassSignature fromPath(final Path file) {
    String fullyQualifiedName = FilenameUtils.removeExtension(file.toString()).replace('/', '.');
    return this.getClassSignature(fullyQualifiedName);
  }
}
