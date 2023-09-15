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
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ClassUtils;
import sootup.core.IdentifierFactory;
import sootup.core.model.SootClass;
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
import sootup.java.core.types.AnnotationType;
import sootup.java.core.types.JavaClassType;

/**
 * The Java-specific implementation of {@link IdentifierFactory}. Should not be used for other
 * languages.
 */
public class JavaIdentifierFactory implements IdentifierFactory {

  @Nonnull private static final JavaIdentifierFactory INSTANCE = new JavaIdentifierFactory();

  /** Caches the created PackageNames for packages. */
  @Nonnull
  protected final Cache<String, PackageName> packageCache =
      CacheBuilder.newBuilder().weakValues().build();

  /** Caches annotation types */
  @Nonnull
  protected final Cache<String, AnnotationType> annotationTypeCache =
      CacheBuilder.newBuilder().weakValues().build();

  /** Caches class types */
  @Nonnull
  protected final Cache<String, JavaClassType> classTypeCache =
      CacheBuilder.newBuilder().weakValues().build();

  @Nonnull
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
  @Nonnull
  public Optional<PrimitiveType> getPrimitiveType(@Nonnull String typeName) {
    return Optional.ofNullable(primitiveTypeMap.get(typeName));
  }

  @Nonnull
  public Collection<PrimitiveType> getAllPrimitiveTypes() {
    return Collections.unmodifiableCollection(primitiveTypeMap.values());
  }

  @Override
  @Nonnull
  public JavaClassType getBoxedType(@Nonnull PrimitiveType primitiveType) {
    String name = primitiveType.getName();
    StringBuilder boxedname = new StringBuilder(name);
    boxedname.setCharAt(0, Character.toUpperCase(boxedname.charAt(0)));
    return getClassType(boxedname.toString(), "java.lang");
  }

  @Override
  public ArrayType getArrayType(Type baseType, int dim) {
    return new ArrayType(baseType, dim);
  }

  public AnnotationType getAnnotationType(final String fullyQualifiedClassName) {
    String className = ClassUtils.getShortClassName(fullyQualifiedClassName);
    String packageName = ClassUtils.getPackageName(fullyQualifiedClassName);

    return annotationTypeCache
        .asMap()
        .computeIfAbsent(
            className + packageName,
            (k) -> new AnnotationType(className, getPackageName(packageName)));
  }

  @Override
  @Nonnull
  public JavaClassType fromPath(@Nonnull final Path rootDirectory, @Nonnull final Path file) {

    final int nameCountBaseDir =
        rootDirectory.toString().isEmpty() ? 0 : rootDirectory.getNameCount();

    String fullyQualifiedName =
        FilenameUtils.removeExtension(
            file.subpath(nameCountBaseDir, file.getNameCount())
                .toString()
                .replace(file.getFileSystem().getSeparator(), "."));

    return getClassType(fullyQualifiedName);
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
  public PackageName getPackageName(@Nonnull final String packageName) {
    return packageCache.asMap().computeIfAbsent(packageName, PackageName::new);
  }

  /**
   * Always creates a new MethodSignature AND a new ClassSignature.
   *
   * @param methodName the method's name
   * @param fullyQualifiedNameDeclClass the fully-qualified name of the declaring class
   * @param parameters the methods parameters fully-qualified name or a primitive's name
   * @param fqReturnType the fully-qualified name of the return type or a primitive's name
   * @return a MethodSignature
   */
  @Override
  public MethodSignature getMethodSignature(
      final String methodName,
      final String fullyQualifiedNameDeclClass,
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
  @Nonnull
  public MethodSignature getMethodSignature(
      @Nonnull SootClass declaringClass, @Nonnull MethodSubSignature subSignature) {
    return getMethodSignature(declaringClass.getType(), subSignature);
  }

  @Override
  @Nonnull
  public MethodSignature getMethodSignature(
      @Nonnull ClassType declaringClassSignature, @Nonnull MethodSubSignature subSignature) {
    return new MethodSignature(declaringClassSignature, subSignature);
  }

  private static final class MethodSignatureParserPatternHolder {
    @Nonnull
    private static final Pattern SOOT_METHOD_SIGNATURE_PATTERN =
        Pattern.compile(
            "^<(?<class>[^:]+):\\s+(?<return>[^\\s]+)\\s+(?<method>[^(]+)\\((?<args>[^)]+)?\\)>$");

    @Nonnull
    private static final Pattern JAVADOCLIKE_METHOD_SIGNATURE_PATTERN =
        Pattern.compile(
            "^(?<class>[^#]+)#(?<method>[^(]+)\\((?<args>[^)]+)?\\)\\s*:(?<return>.+)$");

    @Nonnull
    private static final Pattern ARGS_SPLITTER_PATTERN = Pattern.compile(",", Pattern.LITERAL);

    @Nonnull
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
  @Nonnull
  public MethodSignature parseMethodSignature(@Nonnull String methodSignature) {
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

    if (className.isEmpty() || methodName.isEmpty() || returnName.isEmpty())
      throw MethodSignatureParserPatternHolder.createInvalidMethodSignatureException();

    String argsGroup = matcher.group("args");

    List<String> argsList =
        argsGroup == null
            ? Collections.emptyList()
            : Arrays.stream(
                    MethodSignatureParserPatternHolder.ARGS_SPLITTER_PATTERN.split(argsGroup, -1))
                .map(String::trim)
                .filter(
                    it -> {
                      if (it.isEmpty())
                        throw MethodSignatureParserPatternHolder
                            .createInvalidMethodSignatureException();

                      return true;
                    })
                .collect(Collectors.toList());

    return getMethodSignature(methodName, className, returnName, argsList);
  }

  @Nonnull
  @Override
  public MethodSubSignature getMethodSubSignature(
      @Nonnull String name,
      @Nonnull Type returnType,
      @Nonnull Iterable<? extends Type> parameterSignatures) {
    return new MethodSubSignature(name, parameterSignatures, returnType);
  }

  @Nonnull
  private static final Pattern SOOT_METHOD_SUB_SIGNATURE_PATTERN =
      Pattern.compile("^(?<return>[^\\s]+)\\s+(?<method>[^(]+)\\((?<args>[^)]+)?\\)$");

  @Nonnull
  private static final Pattern JAVADOCLIKE_METHOD_SUB_SIGNATURE_PATTERN =
      Pattern.compile("^#(?<method>[^(]+)\\((?<args>[^)]+)?\\)\\s*:(?<return>.+)$");

  @Nonnull
  private static final Pattern ARGS_SPLITTER_PATTERN = Pattern.compile(",", Pattern.LITERAL);

  @Nonnull
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
  @Nonnull
  public MethodSubSignature parseMethodSubSignature(@Nonnull String subSignature) {
    Matcher matcher = JAVADOCLIKE_METHOD_SUB_SIGNATURE_PATTERN.matcher(subSignature);

    if (!matcher.find()) {
      matcher = SOOT_METHOD_SUB_SIGNATURE_PATTERN.matcher(subSignature);

      if (!matcher.find()) {
        throw createInvalidMethodSubSignatureException();
      }
    }

    String methodName = matcher.group("method").trim();
    String returnName = matcher.group("return").trim();

    if (methodName.isEmpty() || returnName.isEmpty())
      throw createInvalidMethodSubSignatureException();

    String argsGroup = matcher.group("args");

    List<Type> argsList =
        argsGroup == null
            ? Collections.emptyList()
            : Arrays.stream(ARGS_SPLITTER_PATTERN.split(argsGroup, -1))
                .map(String::trim)
                .filter(
                    it -> {
                      if (it.isEmpty()) throw createInvalidMethodSubSignatureException();

                      return true;
                    })
                .map(this::getType)
                .collect(Collectors.toList());

    return getMethodSubSignature(methodName, getType(returnName), argsList);
  }

  @Nonnull
  private static final Pattern SOOT_FIELD_SIGNATURE_PATTERN =
      Pattern.compile("^<(?<class>[^:]+):\\s+(?<type>[^\\s]+)\\s+(?<field>.+)>$");

  @Nonnull
  private static final Pattern JAVADOCLIKE_FIELD_SIGNATURE_PATTERN =
      Pattern.compile("^(?<class>[^#]*)#(?<field>[^(]+):(?<type>.+)$");

  @Nonnull
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
  @Nonnull
  public FieldSignature parseFieldSignature(@Nonnull String fieldSignature) {
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

    if (className.isEmpty() || fieldName.isEmpty() || typeName.isEmpty())
      throw createInvalidFieldSignatureException();

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
  @Nonnull
  public FieldSignature getFieldSignature(
      @Nonnull ClassType declaringClassSignature, @Nonnull FieldSubSignature subSignature) {
    return new FieldSignature(declaringClassSignature, subSignature);
  }

  @Nonnull
  @Override
  public FieldSubSignature getFieldSubSignature(@Nonnull String name, @Nonnull Type type) {
    return new FieldSubSignature(name, type);
  }

  @Nonnull
  private static final Pattern SOOT_FIELD_SUB_SIGNATURE_PATTERN =
      Pattern.compile("^(?<type>[^\\s]+)\\s+(?<field>.+)$");

  @Nonnull
  private static final Pattern JAVADOCLIKE_FIELD_SUB_SIGNATURE_PATTERN =
      Pattern.compile("^#(?<field>[^(]+):(?<type>.+)$");

  @Nonnull
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
  @Nonnull
  public FieldSubSignature parseFieldSubSignature(@Nonnull String subSignature) {
    Matcher matcher = JAVADOCLIKE_FIELD_SUB_SIGNATURE_PATTERN.matcher(subSignature);

    if (!matcher.find()) {
      matcher = SOOT_FIELD_SUB_SIGNATURE_PATTERN.matcher(subSignature);

      if (!matcher.find()) {
        throw createInvalidFieldSubSignatureException();
      }
    }

    String fieldName = matcher.group("field").trim();
    String typeName = matcher.group("type").trim();

    if (fieldName.isEmpty() || typeName.isEmpty()) throw createInvalidFieldSubSignatureException();

    return getFieldSubSignature(fieldName, getType(typeName));
  }
}
