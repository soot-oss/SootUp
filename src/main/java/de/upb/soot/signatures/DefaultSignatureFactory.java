package de.upb.soot.signatures;

import com.google.common.base.Preconditions;
import de.upb.soot.core.SootClass;
import de.upb.soot.types.DefaultTypeFactory;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.Type;
import de.upb.soot.types.TypeFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Factory to create valid signatures for Java classes in a classpath.
 *
 * @author Andreas Dann
 */
public class DefaultSignatureFactory implements SignatureFactory {

  private static final @Nonnull DefaultSignatureFactory INSTANCE =
      new DefaultSignatureFactory(DefaultTypeFactory::getInstance);

  public static @Nonnull DefaultSignatureFactory getInstance() {
    return INSTANCE;
  }

  /** Caches the created signatures for packages. */
  protected final Map<String, PackageSignature> packages = new HashMap<>();

  private final Supplier<? extends TypeFactory> typeFactorySupplier;

  public DefaultSignatureFactory(@Nonnull Supplier<? extends TypeFactory> typeFactorySupplier) {
    this.typeFactorySupplier = typeFactorySupplier;
    /* Represents the default package. */
    packages.put(
        PackageSignature.DEFAULT_PACKAGE.getPackageName(), PackageSignature.DEFAULT_PACKAGE);
  }

  /**
   * Returns a unique PackageSignature. The methodRef looks up a cache if it already contains a
   * signature with the given package name. If the cache lookup fails a new signature is created.
   *
   * @param packageName the Java package name; must not be null use empty string for the default
   *     package {@link PackageSignature#DEFAULT_PACKAGE}
   * @return a PackageSignature
   * @throws NullPointerException if the given package name is null. Use the empty string to denote
   *     the default package.
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
   * Always creates a new MethodSignature AND a new ClassSignature.
   *
   * @param methodName the methodRef's name
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
    JavaClassType declaringClass =
        typeFactorySupplier.get().getClassType(fullyQualifiedNameDeclClass);
    Type returnType = typeFactorySupplier.get().getType(fqReturnType);
    List<Type> parameterSignatures = new ArrayList<>();
    for (String fqParameterName : parameters) {
      Type parameterSignature = typeFactorySupplier.get().getType(fqParameterName);
      parameterSignatures.add(parameterSignature);
    }
    return new MethodSignature(declaringClass, methodName, parameterSignatures, returnType);
  }

  /**
   * Always creates a new MethodSignature reusing the given ClassSignature.
   *
   * @param methodName the methodRef's name
   * @param declaringClassSignature the ClassSignature of the declaring class
   * @param parameters the methods parameters fully-qualified name or a primitive's name
   * @param fqReturnType the fully-qualified name of the return type or a primitive's name
   * @return a MethodSignature
   */
  @Override
  public MethodSignature getMethodSignature(
      final String methodName,
      final JavaClassType declaringClassSignature,
      final String fqReturnType,
      final List<String> parameters) {
    Type returnType = typeFactorySupplier.get().getType(fqReturnType);
    List<Type> parameterSignatures = new ArrayList<>();
    for (String fqParameterName : parameters) {
      Type parameterSignature = typeFactorySupplier.get().getType(fqParameterName);
      parameterSignatures.add(parameterSignature);
    }
    return new MethodSignature(
        declaringClassSignature, methodName, parameterSignatures, returnType);
  }

  @Override
  public MethodSignature getMethodSignature(
      final String methodName,
      final JavaClassType declaringClassSignature,
      final Type fqReturnType,
      final List<Type> parameters) {

    return new MethodSignature(declaringClassSignature, methodName, parameters, fqReturnType);
  }

  @Override
  @Nonnull
  public MethodSignature getMethodSignature(
      @Nonnull SootClass declaringClass, @Nonnull MethodSubSignature subSignature) {
    return this.getMethodSignature(declaringClass.getType(), subSignature);
  }

  @Override
  @Nonnull
  public MethodSignature getMethodSignature(
      @Nonnull JavaClassType declaringClassSignature, @Nonnull MethodSubSignature subSignature) {
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
   * &lt;de.upb.soot.signatures.DefaultSignatureFactory: de.upb.soot.signatures.MethodSignature parseMethodSignature(java.lang.String)&gt;
   * &lt;de.upb.soot.signatures.DefaultSignatureFactory: de.upb.soot.signatures.MethodSignature getMethodSignature(java.lang.String, de.upb.soot.types.JavaClassType)&gt;
   * </code></pre>
   *
   * <p><b><i>JavaDoc-like syntax examples:</i></b>
   *
   * <pre><code>
   * de.upb.soot.signatures.DefaultSignatureFactory#parseMethodSignature(java.lang.String): de.upb.soot.signatures.MethodSignature
   * de.upb.soot.signatures.DefaultSignatureFactory#getMethodSignature(java.lang.String, de.upb.soot.types.JavaClassType): de.upb.soot.signatures.MethodSignature
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

    return DefaultSignatureFactory.getInstance()
        .getMethodSignature(methodName, className, returnName, argsList);
  }

  @Nonnull
  @Override
  public MethodSubSignature getMethodSubSignature(
      @Nonnull String name,
      @Nonnull Iterable<? extends Type> parameterSignatures,
      @Nonnull Type returnType) {
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
   * &gt;de.upb.soot.signatures.MethodSignature parseMethodSignature(java.lang.String)&gt;
   * &gt;de.upb.soot.signatures.MethodSignature getMethodSignature(java.lang.String, de.upb.soot.types.JavaClassType)&gt;
   * </code></pre>
   *
   * <p><b><i>JavaDoc-like syntax examples:</i></b>
   *
   * <pre><code>
   * #parseMethodSignature(java.lang.String): de.upb.soot.signatures.MethodSignature
   * #getMethodSignature(java.lang.String, de.upb.soot.types.JavaClassType): de.upb.soot.signatures.MethodSignature
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
                .map(typeName -> typeFactorySupplier.get().getType(typeName))
                .collect(Collectors.toList());

    return DefaultSignatureFactory.getInstance()
        .getMethodSubSignature(methodName, argsList, typeFactorySupplier.get().getType(returnName));
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
   * &lt;de.upb.soot.signatures.DefaultSignatureFactory: de.upb.soot.signatures.DefaultSignatureFactory INSTANCE&gt;
   * </code></pre>
   *
   * <p><b><i>JavaDoc-like syntax examples:</i></b>
   *
   * <pre><code>
   * de.upb.soot.signatures.DefaultSignatureFactory#INSTANCE: de.upb.soot.signatures.DefaultSignatureFactory
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

    return DefaultSignatureFactory.getInstance()
        .getFieldSignature(fieldName, typeFactorySupplier.get().getClassType(className), typeName);
  }

  @Override
  public FieldSignature getFieldSignature(
      final String fieldName, final JavaClassType declaringClassSignature, final String fieldType) {
    Type type = typeFactorySupplier.get().getType(fieldType);
    return new FieldSignature(declaringClassSignature, fieldName, type);
  }

  @Override
  public FieldSignature getFieldSignature(
      final String fieldName, final JavaClassType declaringClassSignature, final Type fieldType) {
    return new FieldSignature(declaringClassSignature, fieldName, fieldType);
  }

  @Override
  @Nonnull
  public FieldSignature getFieldSignature(
      @Nonnull JavaClassType declaringClassSignature, @Nonnull FieldSubSignature subSignature) {
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
   * &lt;de.upb.soot.signatures.DefaultSignatureFactory INSTANCE&gt;
   * </code></pre>
   *
   * <p><b><i>JavaDoc-like syntax example:</i></b>
   *
   * <pre><code>
   * #INSTANCE: de.upb.soot.signatures.DefaultSignatureFactory
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

    return DefaultSignatureFactory.getInstance()
        .getFieldSubSignature(fieldName, typeFactorySupplier.get().getType(typeName));
  }
}
