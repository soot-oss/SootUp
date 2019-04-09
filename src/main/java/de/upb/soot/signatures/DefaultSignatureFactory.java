package de.upb.soot.signatures;

import com.google.common.base.Preconditions;
import de.upb.soot.core.SootClass;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ClassUtils;

/**
 * Factory to create valid signatures for Java classes in a classpath.
 *
 * @author Andreas Dann
 */
public class DefaultSignatureFactory implements SignatureFactory {

  private static final @Nonnull DefaultSignatureFactory INSTANCE = new DefaultSignatureFactory();

  public static @Nonnull DefaultSignatureFactory getInstance() {
    return INSTANCE;
  }

  /** Caches the created signatures for packages. */
  protected final Map<String, PackageSignature> packages = new HashMap<>();

  public DefaultSignatureFactory() {
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
   * Always creates a new ClassSignature. In opposite to PackageSignatures, ClassSignatures are not
   * cached because the are unique per class, and thus reusing them does not make sense.
   *
   * @param className the simple class name
   * @param packageName the Java package name; must not be null use empty string for the default
   *     package {@link PackageSignature#DEFAULT_PACKAGE} the Java package name
   * @return a ClassSignature for a Java class
   * @throws NullPointerException if the given package name is null. Use the empty string to denote
   *     the default package.
   */
  @Override
  public JavaClassSignature getClassSignature(final String className, final String packageName) {
    PackageSignature packageSignature = getPackageSignature(packageName);
    return new JavaClassSignature(className, packageSignature);
  }

  /**
   * Always creates a new ClassSignature.
   *
   * @param fullyQualifiedClassName the fully-qualified name of the class
   * @return a ClassSignature for a Java Class
   */
  @Override
  public JavaClassSignature getClassSignature(final String fullyQualifiedClassName) {
    String className = ClassUtils.getShortClassName(fullyQualifiedClassName);
    String packageName = ClassUtils.getPackageName(fullyQualifiedClassName);
    return getClassSignature(className, packageName);
  }

  /**
   * Returns a TypeSignature which can be a {@link JavaClassSignature},{@link
   * PrimitiveTypeSignature}, {@link VoidTypeSignature}, or {@link NullTypeSignature}.
   *
   * @param typeDesc the fully-qualified name of the class or for primitives its simple name, e.g.,
   *     int, null, void, ...
   * @return the type signature
   */
  @Override
  public TypeSignature getTypeSignature(final String typeDesc) {

    int len = typeDesc.length();
    int idx = 0;
    StringBuilder stringBuilder = new StringBuilder();
    int nrDims = 0;
    int closed = 0;

    // check if this is an array type ...
    while (idx != len) {
      char c = typeDesc.charAt(idx++);
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
      throw new IllegalArgumentException("Invalid type descriptor");
    }

    String typeName = stringBuilder.toString();

    // FIXME: [JMP] Is lower case correct here? 'Int' is not the same as 'int', because 'Int' is a
    // reference type.
    String typeNameLowerCase = typeName.toLowerCase();
    TypeSignature ret;

    switch (typeNameLowerCase) {
      case "null":
        ret = NullTypeSignature.getInstance();
        break;
      case "void":
        ret = VoidTypeSignature.getInstance();
        break;
      default:
        ret =
            this.getPrimitiveTypeSignature(typeNameLowerCase)
                .map(obj -> (TypeSignature) obj)
                .orElseGet(() -> getClassSignature(typeName));
    }

    if (nrDims > 0) {
      ret = new ArrayTypeSignature(ret, nrDims);
    }
    return ret;
  }

  @Override
  public @Nonnull Optional<PrimitiveTypeSignature> getPrimitiveTypeSignature(
      @Nonnull String typeName) {
    return PrimitiveTypeSignature.find(typeName);
  }

  @Override
  public ArrayTypeSignature getArrayTypeSignature(TypeSignature baseType, int dim) {
    return new ArrayTypeSignature(baseType, dim);
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
    JavaClassSignature declaringClass = getClassSignature(fullyQualifiedNameDeclClass);
    TypeSignature returnTypeSignature = getTypeSignature(fqReturnType);
    List<TypeSignature> parameterSignatures = new ArrayList<>();
    for (String fqParameterName : parameters) {
      TypeSignature parameterSignature = getTypeSignature(fqParameterName);
      parameterSignatures.add(parameterSignature);
    }
    return new MethodSignature(
        declaringClass, methodName, parameterSignatures, returnTypeSignature);
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
      final JavaClassSignature declaringClassSignature,
      final String fqReturnType,
      final List<String> parameters) {
    TypeSignature returnTypeSignature = getTypeSignature(fqReturnType);
    List<TypeSignature> parameterSignatures = new ArrayList<>();
    for (String fqParameterName : parameters) {
      TypeSignature parameterSignature = getTypeSignature(fqParameterName);
      parameterSignatures.add(parameterSignature);
    }
    return new MethodSignature(
        declaringClassSignature, methodName, parameterSignatures, returnTypeSignature);
  }

  @Override
  public MethodSignature getMethodSignature(
      final String methodName,
      final JavaClassSignature declaringClassSignature,
      final TypeSignature fqReturnType,
      final List<TypeSignature> parameters) {

    return new MethodSignature(declaringClassSignature, methodName, parameters, fqReturnType);
  }

  @Override
  @Nonnull
  public MethodSignature getMethodSignature(
      @Nonnull SootClass declaringClass, @Nonnull MethodSubSignature subSignature) {
    return this.getMethodSignature(declaringClass.getSignature(), subSignature);
  }

  @Override
  @Nonnull
  public MethodSignature getMethodSignature(
      @Nonnull JavaClassSignature declaringClassSignature,
      @Nonnull MethodSubSignature subSignature) {
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
    private static IllegalArgumentException createIllegalArgumentException() {
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
   * &lt;de.upb.soot.signatures.DefaultSignatureFactory: de.upb.soot.signatures.MethodSignature getMethodSignature(java.lang.String, de.upb.soot.signatures.JavaClassSignature)&gt;
   * </code></pre>
   *
   * <p><b><i>JavaDoc-like syntax examples:</i></b>
   *
   * <pre><code>
   * de.upb.soot.signatures.DefaultSignatureFactory#parseMethodSignature(java.lang.String): de.upb.soot.signatures.MethodSignature
   * de.upb.soot.signatures.DefaultSignatureFactory#getMethodSignature(java.lang.String, de.upb.soot.signatures.JavaClassSignature): de.upb.soot.signatures.MethodSignature
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
        throw MethodSignatureParserPatternHolder.createIllegalArgumentException();
      }
    }

    String className = matcher.group("class").trim();
    String methodName = matcher.group("method").trim();
    String returnName = matcher.group("return").trim();

    if (className.isEmpty() || methodName.isEmpty() || returnName.isEmpty())
      throw MethodSignatureParserPatternHolder.createIllegalArgumentException();

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
                        throw MethodSignatureParserPatternHolder.createIllegalArgumentException();

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
      @Nonnull Iterable<? extends TypeSignature> parameterSignatures,
      @Nonnull TypeSignature returnTypeSignature) {
    return new MethodSubSignature(name, parameterSignatures, returnTypeSignature);
  }

  private static final class MethodSubSignatureParserPatternHolder {
    @Nonnull
    private static final Pattern SOOT_METHOD_SUB_SIGNATURE_PATTERN =
        Pattern.compile("^(?<return>[^\\s]+)\\s+(?<method>[^(]+)\\((?<args>[^)]+)?\\)$");

    @Nonnull
    private static final Pattern JAVADOCLIKE_METHOD_SUB_SIGNATURE_PATTERN =
        Pattern.compile("^#(?<method>[^(]+)\\((?<args>[^)]+)?\\)\\s*:(?<return>.+)$");

    @Nonnull
    private static final Pattern ARGS_SPLITTER_PATTERN = Pattern.compile(",", Pattern.LITERAL);

    @Nonnull
    private static IllegalArgumentException createIllegalArgumentException() {
      return new IllegalArgumentException(
          "Invalid method sub-signature.\n\n"
              + "The method sub-signature must be conform either to the Soot syntax (\"<RETURNTYPE METHOD(PARAM1, PARAM2, PARAM3)>\") "
              + "or to the JavaDoc-like syntax (\"#METHOD(PARAM1, PARAM2, PARAM3): RETURNTYPE\").");
    }
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
   * &gt;de.upb.soot.signatures.MethodSignature getMethodSignature(java.lang.String, de.upb.soot.signatures.JavaClassSignature)&gt;
   * </code></pre>
   *
   * <p><b><i>JavaDoc-like syntax examples:</i></b>
   *
   * <pre><code>
   * #parseMethodSignature(java.lang.String): de.upb.soot.signatures.MethodSignature
   * #getMethodSignature(java.lang.String, de.upb.soot.signatures.JavaClassSignature): de.upb.soot.signatures.MethodSignature
   * </code></pre>
   *
   * @param subSignature A Soot- or Kotlin-like method sub-signature.
   * @return The parsed {@link MethodSubSignature}.
   * @author Jan Martin Persch
   */
  @Override
  @Nonnull
  public MethodSubSignature parseMethodSubSignature(@Nonnull String subSignature) {
    Matcher matcher =
        MethodSubSignatureParserPatternHolder.JAVADOCLIKE_METHOD_SUB_SIGNATURE_PATTERN.matcher(
            subSignature);

    if (!matcher.find()) {
      matcher =
          MethodSubSignatureParserPatternHolder.SOOT_METHOD_SUB_SIGNATURE_PATTERN.matcher(
              subSignature);

      if (!matcher.find()) {
        throw MethodSubSignatureParserPatternHolder.createIllegalArgumentException();
      }
    }

    String methodName = matcher.group("method").trim();
    String returnName = matcher.group("return").trim();

    if (methodName.isEmpty() || returnName.isEmpty())
      throw MethodSubSignatureParserPatternHolder.createIllegalArgumentException();

    String argsGroup = matcher.group("args");

    List<TypeSignature> argsList =
        argsGroup == null
            ? Collections.emptyList()
            : Arrays.stream(
                    MethodSubSignatureParserPatternHolder.ARGS_SPLITTER_PATTERN.split(
                        argsGroup, -1))
                .map(String::trim)
                .filter(
                    it -> {
                      if (it.isEmpty())
                        throw MethodSubSignatureParserPatternHolder
                            .createIllegalArgumentException();

                      return true;
                    })
                .map(this::getTypeSignature)
                .collect(Collectors.toList());

    return DefaultSignatureFactory.getInstance()
        .getMethodSubSignature(methodName, argsList, this.getTypeSignature(returnName));
  }

  private static final class FieldSignatureParserPatternHolder {
    @Nonnull
    private static final Pattern SOOT_FIELD_SIGNATURE_PATTERN =
        Pattern.compile("^<(?<class>[^:]+):\\s+(?<type>[^\\s]+)\\s+(?<field>.+)>$");

    @Nonnull
    private static final Pattern JAVADOCLIKE_FIELD_SIGNATURE_PATTERN =
        Pattern.compile("^(?<class>[^#]*)#(?<field>[^(]+):(?<type>.+)$");

    @Nonnull
    private static IllegalArgumentException createIllegalArgumentException() {
      return new IllegalArgumentException(
          "Invalid field signature.\n\n"
              + "The field signature must be conform either to the Soot syntax (\"<CLASS: TYPE FIELD>\") "
              + "or to the JavaDoc-like syntax (\"CLASS#FIELD: TYPE\").");
    }
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
    Matcher matcher =
        FieldSignatureParserPatternHolder.SOOT_FIELD_SIGNATURE_PATTERN.matcher(fieldSignature);

    if (!matcher.find()) {
      matcher =
          FieldSignatureParserPatternHolder.JAVADOCLIKE_FIELD_SIGNATURE_PATTERN.matcher(
              fieldSignature);

      if (!matcher.find()) {
        throw FieldSignatureParserPatternHolder.createIllegalArgumentException();
      }
    }

    String className = matcher.group("class").trim();
    String fieldName = matcher.group("field").trim();
    String typeName = matcher.group("type").trim();

    if (className.isEmpty() || fieldName.isEmpty() || typeName.isEmpty())
      throw FieldSignatureParserPatternHolder.createIllegalArgumentException();

    return DefaultSignatureFactory.getInstance()
        .getFieldSignature(fieldName, this.getClassSignature(className), typeName);
  }

  @Override
  public FieldSignature getFieldSignature(
      final String fieldName,
      final JavaClassSignature declaringClassSignature,
      final String fieldType) {
    TypeSignature typeSignature = getTypeSignature(fieldType);
    return new FieldSignature(declaringClassSignature, fieldName, typeSignature);
  }

  @Override
  public FieldSignature getFieldSignature(
      final String fieldName,
      final JavaClassSignature declaringClassSignature,
      final TypeSignature fieldType) {
    return new FieldSignature(declaringClassSignature, fieldName, fieldType);
  }

  @Override
  @Nonnull
  public FieldSignature getFieldSignature(
      @Nonnull JavaClassSignature declaringClassSignature,
      @Nonnull FieldSubSignature subSignature) {
    return new FieldSignature(declaringClassSignature, subSignature);
  }

  @Nonnull
  @Override
  public FieldSubSignature getFieldSubSignature(
      @Nonnull String name, @Nonnull TypeSignature typeSignature) {
    return new FieldSubSignature(name, typeSignature);
  }

  private static final class FieldSubSignatureParserPatternHolder {
    @Nonnull
    private static final Pattern SOOT_FIELD_SUB_SIGNATURE_PATTERN =
        Pattern.compile("^(?<type>[^\\s]+)\\s+(?<field>.+)$");

    @Nonnull
    private static final Pattern JAVADOCLIKE_FIELD_SUB_SIGNATURE_PATTERN =
        Pattern.compile("^#(?<field>[^(]+):(?<type>.+)$");

    @Nonnull
    private static IllegalArgumentException createIllegalArgumentException() {
      return new IllegalArgumentException(
          "Invalid field sub-signature.\n\n"
              + "The field sub-signature must be conform either to the Soot syntax (\"<TYPE FIELD>\") "
              + "or to the JavaDoc-like syntax (\"#FIELD: TYPE\").");
    }
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
    Matcher matcher =
        FieldSubSignatureParserPatternHolder.JAVADOCLIKE_FIELD_SUB_SIGNATURE_PATTERN.matcher(
            subSignature);

    if (!matcher.find()) {
      matcher =
          FieldSubSignatureParserPatternHolder.SOOT_FIELD_SUB_SIGNATURE_PATTERN.matcher(
              subSignature);

      if (!matcher.find()) {
        throw FieldSubSignatureParserPatternHolder.createIllegalArgumentException();
      }
    }

    String fieldName = matcher.group("field").trim();
    String typeName = matcher.group("type").trim();

    if (fieldName.isEmpty() || typeName.isEmpty())
      throw FieldSubSignatureParserPatternHolder.createIllegalArgumentException();

    return DefaultSignatureFactory.getInstance()
        .getFieldSubSignature(fieldName, this.getTypeSignature(typeName));
  }

  @Override
  @Nonnull
  public JavaClassSignature fromPath(@Nonnull final Path file) {
    String separator = file.getFileSystem().getSeparator();
    String path = file.toString();

    String fullyQualifiedName =
        FilenameUtils.removeExtension(
                path.startsWith(separator) ? path.substring(separator.length()) : path)
            .replace(separator, ".");

    return this.getClassSignature(fullyQualifiedName);
  }
}
