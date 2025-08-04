package sootup.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Christian Brüggemann, Markus Schmidt
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ClassUtils;
import org.jspecify.annotations.NonNull;
import sootup.core.IdentifierFactory;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.FieldSubSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.NullType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.types.VoidType;
import sootup.java.core.types.JavaClassType;

/**
 * The Java-specific implementation of {@link IdentifierFactory}. Should not be used for other
 * languages.
 */
public class JavaIdentifierFactory implements IdentifierFactory {

  @NonNull private static final JavaIdentifierFactory INSTANCE = new JavaIdentifierFactory();

  @NonNull
  public static final MethodSubSignature STATIC_INITIALIZER =
      new MethodSubSignature("<clinit>", Collections.emptyList(), VoidType.getInstance());

  @NonNull
  private static final Pattern SOOT_FIELD_SUB_SIGNATURE_PATTERN =
      Pattern.compile("^(?<type>[^\\s]+)\\s+(?<field>.+)$");

  @NonNull
  private static final Pattern JAVADOCLIKE_FIELD_SUB_SIGNATURE_PATTERN =
      Pattern.compile("^#(?<field>[^(]+):(?<type>.+)$");

  /** Caches the created PackageNames for packages. */
  @NonNull
  protected final Cache<String, PackageName> packageCache =
      CacheBuilder.newBuilder().weakValues().build();

  /** Caches class types */
  @NonNull
  protected final Cache<String, JavaClassType> classTypeCache =
      CacheBuilder.newBuilder().weakValues().build();

  @NonNull
  protected final Map<String, PrimitiveType> primitiveTypeMap = Maps.newHashMapWithExpectedSize(8);

  public static JavaIdentifierFactory getInstance() {
    return INSTANCE;
  }

  JavaIdentifierFactory() {
    /* Represents the default package. */
    packageCache.put(PackageName.DEFAULT_PACKAGE.getName(), PackageName.DEFAULT_PACKAGE);

    // initialize primitive map
    primitiveTypeMap.put(
        PrimitiveType.LongType.getInstance().getName(), PrimitiveType.LongType.getInstance());
    primitiveTypeMap.put(
        PrimitiveType.IntType.getInstance().getName(), PrimitiveType.IntType.getInstance());
    primitiveTypeMap.put(
        PrimitiveType.ShortType.getInstance().getName(), PrimitiveType.ShortType.getInstance());
    primitiveTypeMap.put(
        PrimitiveType.CharType.getInstance().getName(), PrimitiveType.CharType.getInstance());
    primitiveTypeMap.put(
        PrimitiveType.ByteType.getInstance().getName(), PrimitiveType.ByteType.getInstance());
    primitiveTypeMap.put(
        PrimitiveType.BooleanType.getInstance().getName(), PrimitiveType.BooleanType.getInstance());
    primitiveTypeMap.put(
        PrimitiveType.DoubleType.getInstance().getName(), PrimitiveType.DoubleType.getInstance());
    primitiveTypeMap.put(
        PrimitiveType.FloatType.getInstance().getName(), PrimitiveType.FloatType.getInstance());
  }

  /**
   * Always creates a new ClassSignature. In opposite to PackageSignatures, ClassSignatures are not
   * cached because the are unique per class, and thus reusing them does not make sense.
   *
   * @param className the simple class name
   * @param packageName the Java package name; must not be null use empty string for the default
   *     package {@link PackageName#DEFAULT_PACKAGE} the Java package name
   * @return a ClassSignature for a Java class
   * @throws NullPointerException if the given package name is null. Use the empty string to denote
   *     the default package.
   */
  @Override
  public JavaClassType getClassType(final String className, final String packageName) {
    PackageName packageIdentifier = getPackageName(packageName);
    return classTypeCache
        .asMap()
        .computeIfAbsent(
            className + packageName, (k) -> new JavaClassType(className, packageIdentifier));
  }

  /**
   * Always creates a new ClassSignature.
   *
   * @param fullyQualifiedClassName the fully-qualified name of the class
   * @return a ClassSignature for a Java Class
   */
  @Override
  public JavaClassType getClassType(final String fullyQualifiedClassName) {
    String className = ClassUtils.getShortClassName(fullyQualifiedClassName);
    String packageName = ClassUtils.getPackageName(fullyQualifiedClassName);
    return getClassType(className, packageName);
  }

  /**
   * Returns a Type which can be a {@link JavaClassType},{@link PrimitiveType}, {@link VoidType}, or
   * {@link NullType}.
   *
   * @param typeDesc the fully-qualified name of the class or for primitives its simple name, e.g.,
   *     int, null, void, ...
   * @return the type signature
   */
  @Override
  public Type getType(final String typeDesc) {
    int len = typeDesc.length();
    StringBuilder stringBuilder = new StringBuilder();
    int nrDims = 0;
    int closed = 0;

    // check if this is an array type ...
    for (int i = 0; i < len; i++) {
      char c = typeDesc.charAt(i);
      switch (c) {
        case '[':
          ++nrDims;
          break;
        case ']':
          ++closed;
          break;
        default:
          stringBuilder.append(c);
          break;
      }
    }
    if (nrDims != closed) {
      throw new IllegalArgumentException("Invalid type descriptor(" + typeDesc + ")");
    }

    String typeName = stringBuilder.toString();

    Type ret;
    switch (typeName) {
      case "":
        throw new IllegalArgumentException("Invalid! Typedescriptor is empty.");
      case "null":
        ret = NullType.getInstance();
        break;
      case "void":
        ret = VoidType.getInstance();
        break;
      default:
        ret =
            getPrimitiveType(typeName)
                .map(obj -> (Type) obj)
                .orElseGet(() -> getClassType(typeName));
    }

    if (nrDims > 0) {
      ret = new ArrayType(ret, nrDims);
    }
    return ret;
  }

  @Override
  @NonNull
  public Optional<PrimitiveType> getPrimitiveType(@NonNull String typeName) {
    return Optional.ofNullable(primitiveTypeMap.get(typeName));
  }

  @NonNull
  public Collection<PrimitiveType> getAllPrimitiveTypes() {
    return Collections.unmodifiableCollection(primitiveTypeMap.values());
  }

  @Override
  @NonNull
  public JavaClassType getBoxedType(@NonNull PrimitiveType primitiveType) {
    String name = primitiveType.getName();
    StringBuilder boxedname = new StringBuilder(name);
    boxedname.setCharAt(0, Character.toUpperCase(boxedname.charAt(0)));
    return getClassType(boxedname.toString(), "java.lang");
  }

  @Override
  public ArrayType getArrayType(Type baseType, int dim) {
    return new ArrayType(baseType, dim);
  }

  /**
   * Returns a unique PackageName. The method looks up a cache if it already contains a signature
   * with the given package name. If the cache lookup fails a new signature is created.
   *
   * @param packageName the Java package name; must not be null use empty string for the default
   *     package {@link PackageName#DEFAULT_PACKAGE}
   * @return a PackageName
   * @throws NullPointerException if the given package name is null. Use the empty string to denote
   *     the default package.
   */
  @Override
  public PackageName getPackageName(@NonNull final String packageName) {
    return packageCache.asMap().computeIfAbsent(packageName, PackageName::new);
  }

  /**
   * Always creates a new MethodSignature AND a new ClassSignature.
   *
   * @param fullyQualifiedNameDeclClass the fully-qualified name of the declaring class
   * @param methodName the method's name
   * @param fqReturnType the fully-qualified name of the return type or a primitive's name
   * @param parameters the methods parameters fully-qualified name or a primitive's name
   * @return a MethodSignature
   */
  @Override
  public MethodSignature getMethodSignature(
      final String fullyQualifiedNameDeclClass,
      final String methodName,
      final String fqReturnType,
      final List<String> parameters) {
    JavaClassType declaringClass = getClassType(fullyQualifiedNameDeclClass);
    Type returnType = getType(fqReturnType);
    List<Type> parameterSignatures = new ArrayList<>();
    for (String fqParameterName : parameters) {
      Type parameterSignature = getType(fqParameterName);
      parameterSignatures.add(parameterSignature);
    }
    return new MethodSignature(declaringClass, methodName, parameterSignatures, returnType);
  }

  /**
   * Always creates a new MethodSignature reusing the given ClassSignature.
   *
   * @param declaringClassSignature the ClassSignature of the declaring class
   * @param methodName the method's name
   * @param fqReturnType the fully-qualified name of the return type or a primitive's name
   * @param parameters the methods parameters fully-qualified name or a primitive's name
   * @return a MethodSignature
   */
  @Override
  public MethodSignature getMethodSignature(
      final ClassType declaringClassSignature,
      final String methodName,
      final String fqReturnType,
      final List<String> parameters) {
    Type returnType = getType(fqReturnType);
    List<Type> parameterSignatures = new ArrayList<>();
    for (String fqParameterName : parameters) {
      Type parameterSignature = getType(fqParameterName);
      parameterSignatures.add(parameterSignature);
    }
    return new MethodSignature(
        declaringClassSignature, methodName, parameterSignatures, returnType);
  }

  @Override
  public MethodSignature getMethodSignature(
      final ClassType declaringClassSignature,
      final String methodName,
      final Type fqReturnType,
      final List<Type> parameters) {

    return new MethodSignature(declaringClassSignature, methodName, parameters, fqReturnType);
  }

  @Override
  @NonNull
  public MethodSignature getMethodSignature(
      @NonNull ClassType declaringClassSignature, @NonNull MethodSubSignature subSignature) {
    return new MethodSignature(declaringClassSignature, subSignature);
  }

  private static final class MethodSignatureParserPatternHolder {
    @NonNull
    private static final Pattern SOOT_METHOD_SIGNATURE_PATTERN =
        Pattern.compile(
            "^<(?<class>[^:]+):\\s*(?<return>[^\\s]+)\\s+(?<method>[^(]+)\\((?<args>[^)]+)?\\)>$");

    @NonNull
    private static final Pattern JAVADOCLIKE_METHOD_SIGNATURE_PATTERN =
        Pattern.compile(
            "^(?<class>[^#]+)#(?<method>[^(]+)\\((?<args>[^)]+)?\\)\\s*:(?<return>.+)$");

    @NonNull
    private static final Pattern ARGS_SPLITTER_PATTERN = Pattern.compile(",", Pattern.LITERAL);

    @NonNull
    private static IllegalArgumentException createInvalidMethodSignatureException() {
      return new IllegalArgumentException(
          "Invalid method signature.\n\n"
              + "The method signature must be conform either to the Soot syntax (\"<CLASS: RETURNTYPE METHOD(PARAM1, PARAM2, PARAM3)>\") "
              + "or to the JavaDoc-like syntax (\"CLASS#METHOD(PARAM1, PARAM2, PARAM3): RETURNTYPE\").");
    }
  }

  /**
   * Parses a {@link MethodSignature} either from a Soot or from a JavaDoc-like signature
   * specification.
   *
   * <p>Soot syntax: <code>&lt;CLASS: RETURNTYPE METHOD(PARAM1, PARAM2, PARAM3)&gt;</code>
   *
   * <p>JavaDoc-like syntax: <code>CLASS#METHOD(PARAM1, PARAM2, PARAM3): RETURNTYPE</code>
   *
   * <p><b><i>Soot syntax examples:</i></b>
   *
   * <pre><code>
   * &lt;de.upb.sootup.signatures.Remove: de.upb.sootup.signatures.MethodSignature parseMethodSignature(java.lang.String)&gt;
   * &lt;de.upb.sootup.signatures.Remove: de.upb.sootup.signatures.MethodSignature getMethodSignature(java.lang.String, de.upb.sootup.types.JavaClassType)&gt;
   * </code></pre>
   *
   * <p><b><i>JavaDoc-like syntax examples:</i></b>
   *
   * <pre><code>
   * de.upb.sootup.signatures.Remove#parseMethodSignature(java.lang.String): de.upb.sootup.signatures.MethodSignature
   * de.upb.sootup.signatures.Remove#getMethodSignature(java.lang.String, de.upb.sootup.types.JavaClassType): de.upb.sootup.signatures.MethodSignature
   * </code></pre>
   *
   * @param methodSignature A Soot- or JavaDoc-like method signature.
   * @return The parsed {@link MethodSignature}.
   * @author Jan Martin Persch
   */
  @Override
  @NonNull
  public MethodSignature parseMethodSignature(@NonNull String methodSignature) {
    Matcher matcher =
        MethodSignatureParserPatternHolder.SOOT_METHOD_SIGNATURE_PATTERN.matcher(methodSignature);

    if (!matcher.find()) {
      matcher =
          MethodSignatureParserPatternHolder.JAVADOCLIKE_METHOD_SIGNATURE_PATTERN.matcher(
              methodSignature);

      if (!matcher.find()) {
        throw MethodSignatureParserPatternHolder.createInvalidMethodSignatureException();
      }
    }

    String className = matcher.group("class").trim();
    String methodName = matcher.group("method").trim();
    String returnName = matcher.group("return").trim();

    if (className.isEmpty() || methodName.isEmpty() || returnName.isEmpty()) {
      throw MethodSignatureParserPatternHolder.createInvalidMethodSignatureException();
    }

    String argsGroup = matcher.group("args");

    List<String> argsList =
        argsGroup == null
            ? Collections.emptyList()
            : Arrays.stream(
                    MethodSignatureParserPatternHolder.ARGS_SPLITTER_PATTERN.split(argsGroup, -1))
                .map(String::trim)
                .filter(
                    it -> {
                      if (it.isEmpty()) {
                        throw MethodSignatureParserPatternHolder
                            .createInvalidMethodSignatureException();
                      }

                      return true;
                    })
                .collect(Collectors.toList());

    return getMethodSignature(className, methodName, returnName, argsList);
  }

  @NonNull
  @Override
  public MethodSubSignature getMethodSubSignature(
      @NonNull String name,
      @NonNull Type returnType,
      @NonNull Iterable<? extends Type> parameterSignatures) {
    return new MethodSubSignature(name, parameterSignatures, returnType);
  }

  @NonNull
  private static final Pattern SOOT_METHOD_SUB_SIGNATURE_PATTERN =
      Pattern.compile("^(?<return>[^\\s]+)\\s+(?<method>[^(]+)\\((?<args>[^)]+)?\\)$");

  @NonNull
  private static final Pattern JAVADOCLIKE_METHOD_SUB_SIGNATURE_PATTERN =
      Pattern.compile("^#(?<method>[^(]+)\\((?<args>[^)]+)?\\)\\s*:(?<return>.+)$");

  @NonNull
  private static final Pattern ARGS_SPLITTER_PATTERN = Pattern.compile(",", Pattern.LITERAL);

  @NonNull
  private static IllegalArgumentException createInvalidMethodSubSignatureException() {
    return new IllegalArgumentException(
        "Invalid method sub-signature.\n\n"
            + "The method sub-signature must be conform either to the Soot syntax (\"<RETURNTYPE METHOD(PARAM1, PARAM2, PARAM3)>\") "
            + "or to the JavaDoc-like syntax (\"#METHOD(PARAM1, PARAM2, PARAM3): RETURNTYPE\").");
  }

  /**
   * Parses a {@link MethodSubSignature} either from a Soot or from a JavaDoc-like signature
   * specification.
   *
   * <p>Soot syntax: <code>&lt;RETURNTYPE METHOD(PARAM1, PARAM2, PARAM3)&gt;</code>
   *
   * <p>JavaDoc-like syntax: <code>#METHOD(PARAM1, PARAM2, PARAM3): RETURNTYPE</code>
   *
   * <p><b><i>Soot syntax examples:</i></b>
   *
   * <pre><code>
   * &gt;de.upb.sootup.signatures.MethodSignature parseMethodSignature(java.lang.String)&gt;
   * &gt;de.upb.sootup.signatures.MethodSignature getMethodSignature(java.lang.String, de.upb.sootup.types.JavaClassType)&gt;
   * </code></pre>
   *
   * <p><b><i>JavaDoc-like syntax examples:</i></b>
   *
   * <pre><code>
   * #parseMethodSignature(java.lang.String): de.upb.sootup.signatures.MethodSignature
   * #getMethodSignature(java.lang.String, de.upb.sootup.types.JavaClassType): de.upb.sootup.signatures.MethodSignature
   * </code></pre>
   *
   * @param subSignature A Soot- or Kotlin-like method sub-signature.
   * @return The parsed {@link MethodSubSignature}.
   * @author Jan Martin Persch
   */
  @Override
  @NonNull
  public MethodSubSignature parseMethodSubSignature(@NonNull String subSignature) {
    Matcher matcher = JAVADOCLIKE_METHOD_SUB_SIGNATURE_PATTERN.matcher(subSignature);

    if (!matcher.find()) {
      matcher = SOOT_METHOD_SUB_SIGNATURE_PATTERN.matcher(subSignature);

      if (!matcher.find()) {
        throw createInvalidMethodSubSignatureException();
      }
    }

    String methodName = matcher.group("method").trim();
    String returnName = matcher.group("return").trim();

    if (methodName.isEmpty() || returnName.isEmpty()) {
      throw createInvalidMethodSubSignatureException();
    }

    String argsGroup = matcher.group("args");

    List<Type> argsList =
        argsGroup == null
            ? Collections.emptyList()
            : Arrays.stream(ARGS_SPLITTER_PATTERN.split(argsGroup, -1))
                .map(String::trim)
                .filter(
                    it -> {
                      if (it.isEmpty()) {
                        throw createInvalidMethodSubSignatureException();
                      }

                      return true;
                    })
                .map(this::getType)
                .collect(Collectors.toList());

    return getMethodSubSignature(methodName, getType(returnName), argsList);
  }

  @NonNull
  private static final Pattern SOOT_FIELD_SIGNATURE_PATTERN =
      Pattern.compile("^<(?<class>[^:]+):\\s+(?<type>[^\\s]+)\\s+(?<field>.+)>$");

  @NonNull
  private static final Pattern JAVADOCLIKE_FIELD_SIGNATURE_PATTERN =
      Pattern.compile("^(?<class>[^#]*)#(?<field>[^(]+):(?<type>.+)$");

  @NonNull
  private static IllegalArgumentException createInvalidFieldSignatureException() {
    return new IllegalArgumentException(
        "Invalid field signature.\n\n"
            + "The field signature must be conform either to the Soot syntax (\"<CLASS: TYPE FIELD>\") "
            + "or to the JavaDoc-like syntax (\"CLASS#FIELD: TYPE\").");
  }

  /**
   * Parses a {@link MethodSignature} either from a Soot or from a JavaDoc-like signature
   * specification.
   *
   * <p>Soot syntax: <code>&lt;CLASS: TYPE FIELD&gt;</code>
   *
   * <p>JavaDoc-like syntax: <code>CLASS#FIELD: TYPE</code>
   *
   * <p><b><i>Soot syntax examples:</i></b>
   *
   * <pre><code>
   * &lt;de.upb.sootup.signatures.Remove: de.upb.sootup.signatures.Remove INSTANCE&gt;
   * </code></pre>
   *
   * <p><b><i>JavaDoc-like syntax examples:</i></b>
   *
   * <pre><code>
   * de.upb.sootup.signatures.Remove#INSTANCE: de.upb.sootup.signatures.Remove
   * </code></pre>
   *
   * @param fieldSignature A Soot- or JavaDoc-like field signature.
   * @return The parsed {@link MethodSignature}.
   * @author Jan Martin Persch
   */
  @Override
  @NonNull
  public FieldSignature parseFieldSignature(@NonNull String fieldSignature) {
    Matcher matcher = SOOT_FIELD_SIGNATURE_PATTERN.matcher(fieldSignature);

    if (!matcher.find()) {
      matcher = JAVADOCLIKE_FIELD_SIGNATURE_PATTERN.matcher(fieldSignature);

      if (!matcher.find()) {
        throw createInvalidFieldSignatureException();
      }
    }

    String className = matcher.group("class").trim();
    String fieldName = matcher.group("field").trim();
    String typeName = matcher.group("type").trim();

    if (className.isEmpty() || fieldName.isEmpty() || typeName.isEmpty()) {
      throw createInvalidFieldSignatureException();
    }

    return getFieldSignature(fieldName, getClassType(className), typeName);
  }

  @Override
  public FieldSignature getFieldSignature(
      final String fieldName, final ClassType declaringClassSignature, final String fieldType) {
    Type type = getType(fieldType);
    return new FieldSignature(declaringClassSignature, fieldName, type);
  }

  @Override
  public FieldSignature getFieldSignature(
      final String fieldName, final ClassType declaringClassSignature, final Type fieldType) {
    return new FieldSignature(declaringClassSignature, fieldName, fieldType);
  }

  @Override
  @NonNull
  public FieldSignature getFieldSignature(
      @NonNull ClassType declaringClassSignature, @NonNull FieldSubSignature subSignature) {
    return new FieldSignature(declaringClassSignature, subSignature);
  }

  @NonNull
  @Override
  public FieldSubSignature getFieldSubSignature(@NonNull String name, @NonNull Type type) {
    return new FieldSubSignature(name, type);
  }

  @NonNull
  private static IllegalArgumentException createInvalidFieldSubSignatureException() {
    return new IllegalArgumentException(
        "Invalid field sub-signature.\n\n"
            + "The field sub-signature must be conform either to the Soot syntax (\"<TYPE FIELD>\") "
            + "or to the JavaDoc-like syntax (\"#FIELD: TYPE\").");
  }

  /**
   * Parses a {@link FieldSubSignature} either from a Soot or from a JavaDoc-like signature
   * specification.
   *
   * <p>Soot syntax: <code>&lt;TYPE FIELD&gt;</code>
   *
   * <p>JavaDoc-like syntax: <code>#FIELD: TYPE</code>
   *
   * <p><b><i>Soot syntax example:</i></b>
   *
   * <pre><code>
   * &lt;de.upb.sootup.signatures.Remove INSTANCE&gt;
   * </code></pre>
   *
   * <p><b><i>JavaDoc-like syntax example:</i></b>
   *
   * <pre><code>
   * #INSTANCE: de.upb.sootup.signatures.Remove
   * </code></pre>
   *
   * @param subSignature A Soot- or Kotlin-like method sub-signature.
   * @return The parsed {@link FieldSubSignature}.
   * @author Jan Martin Persch
   */
  @NonNull
  public FieldSubSignature parseFieldSubSignature(@NonNull String subSignature) {
    Matcher matcher = JAVADOCLIKE_FIELD_SUB_SIGNATURE_PATTERN.matcher(subSignature);

    if (!matcher.find()) {
      matcher = SOOT_FIELD_SUB_SIGNATURE_PATTERN.matcher(subSignature);

      if (!matcher.find()) {
        throw createInvalidFieldSubSignatureException();
      }
    }

    String fieldName = matcher.group("field").trim();
    String typeName = matcher.group("type").trim();

    if (fieldName.isEmpty() || typeName.isEmpty()) {
      throw createInvalidFieldSubSignatureException();
    }

    return getFieldSubSignature(fieldName, getType(typeName));
  }

  @Override
  public MethodSignature getStaticInitializerSignature(ClassType declaringClassSignature) {
    return getMethodSignature(declaringClassSignature, STATIC_INITIALIZER);
  }

  @Override
  public boolean isStaticInitializerSubSignature(@NonNull MethodSubSignature methodSubSignature) {
    return methodSubSignature.equals(STATIC_INITIALIZER);
  }

  @Override
  public boolean isConstructorSignature(@NonNull MethodSignature methodSignature) {
    return isConstructorSubSignature(methodSignature.getSubSignature());
  }

  @Override
  public boolean isConstructorSubSignature(@NonNull MethodSubSignature methodSubSignature) {
    return methodSubSignature.getName().equals("<init>")
        && methodSubSignature.getType() == VoidType.getInstance();
  }

  @Override
  public boolean isMainSubSignature(@NonNull MethodSubSignature methodSubSignature) {
    if (methodSubSignature.getName().equals("main")) {
      final List<Type> parameterTypes = methodSubSignature.getParameterTypes();
      if (parameterTypes.size() == 1) {
        return parameterTypes.get(0).toString().equals("java.lang.String[]");
      }
    }
    return false;
  }
}
