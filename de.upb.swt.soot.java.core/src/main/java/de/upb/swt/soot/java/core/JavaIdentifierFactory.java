package de.upb.swt.soot.java.core;

import com.google.common.base.Preconditions;
import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.FieldSubSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.signatures.MethodSubSignature;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.types.*;
import de.upb.swt.soot.java.core.types.JavaClassType;
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

public class JavaIdentifierFactory implements IdentifierFactory {

  @Nonnull private static final JavaIdentifierFactory INSTANCE = new JavaIdentifierFactory();

  /** Caches the created PackageNames for packages. */
  final Map<String, PackageName> packages = new HashMap<>();

  public static JavaIdentifierFactory getInstance() {
    return INSTANCE;
  }

  JavaIdentifierFactory() {
    /* Represents the default package. */
    packages.put(PackageName.DEFAULT_PACKAGE.getPackageName(), PackageName.DEFAULT_PACKAGE);
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
    return new JavaClassType(className, packageIdentifier);
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
    Type ret;

    switch (typeNameLowerCase) {
      case "null":
        ret = NullType.getInstance();
        break;
      case "void":
        ret = VoidType.getInstance();
        break;
      default:
        ret =
            getPrimitiveType(typeNameLowerCase)
                .map(obj -> (Type) obj)
                .orElseGet(() -> getClassType(typeName));
    }

    if (nrDims > 0) {
      ret = new ArrayType(ret, nrDims);
    }
    return ret;
  }

  @Override
  public @Nonnull Optional<PrimitiveType> getPrimitiveType(@Nonnull String typeName) {
    return PrimitiveType.find(typeName);
  }

  @Override
  public ArrayType getArrayType(Type baseType, int dim) {
    return new ArrayType(baseType, dim);
  }

  @Override
  @Nonnull
  public JavaClassType fromPath(@Nonnull final Path file) {
    String separator = file.getFileSystem().getSeparator();
    String path = file.toString();

    String fullyQualifiedName =
        FilenameUtils.removeExtension(
                path.startsWith(separator) ? path.substring(separator.length()) : path)
            .replace(separator, ".");

    return getClassType(fullyQualifiedName);
  }

  /**
   * Returns a unique PackageName. The methodRef looks up a cache if it already contains a signature
   * with the given package name. If the cache lookup fails a new signature is created.
   *
   * @param packageName the Java package name; must not be null use empty string for the default
   *     package {@link PackageName#DEFAULT_PACKAGE}
   * @return a PackageName
   * @throws NullPointerException if the given package name is null. Use the empty string to denote
   *     the default package.
   */
  @Override
  public PackageName getPackageName(final String packageName) {
    Preconditions.checkNotNull(packageName);
    PackageName packageIdentifier = packages.get(packageName);
    if (packageIdentifier == null) {
      packageIdentifier = new PackageName(packageName);
      packages.put(packageName, packageIdentifier);
    }
    return packageIdentifier;
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
   * @param methodName the methodRef's name
   * @param declaringClassSignature the ClassSignature of the declaring class
   * @param parameters the methods parameters fully-qualified name or a primitive's name
   * @param fqReturnType the fully-qualified name of the return type or a primitive's name
   * @return a MethodSignature
   */
  @Override
  public MethodSignature getMethodSignature(
      final String methodName,
      final ClassType declaringClassSignature,
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
      final String methodName,
      final ClassType declaringClassSignature,
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
   * &lt;de.upb.soot.signatures.Remove: de.upb.soot.signatures.MethodSignature parseMethodSignature(java.lang.String)&gt;
   * &lt;de.upb.soot.signatures.Remove: de.upb.soot.signatures.MethodSignature getMethodSignature(java.lang.String, de.upb.soot.types.JavaClassType)&gt;
   * </code></pre>
   *
   * <p><b><i>JavaDoc-like syntax examples:</i></b>
   *
   * <pre><code>
   * de.upb.soot.signatures.Remove#parseMethodSignature(java.lang.String): de.upb.soot.signatures.MethodSignature
   * de.upb.soot.signatures.Remove#getMethodSignature(java.lang.String, de.upb.soot.types.JavaClassType): de.upb.soot.signatures.MethodSignature
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
                .map(typeName -> getType(typeName))
                .collect(Collectors.toList());

    return getMethodSubSignature(methodName, argsList, getType(returnName));
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
   * &lt;de.upb.soot.signatures.Remove: de.upb.soot.signatures.Remove INSTANCE&gt;
   * </code></pre>
   *
   * <p><b><i>JavaDoc-like syntax examples:</i></b>
   *
   * <pre><code>
   * de.upb.soot.signatures.Remove#INSTANCE: de.upb.soot.signatures.Remove
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
   * &lt;de.upb.soot.signatures.Remove INSTANCE&gt;
   * </code></pre>
   *
   * <p><b><i>JavaDoc-like syntax example:</i></b>
   *
   * <pre><code>
   * #INSTANCE: de.upb.soot.signatures.Remove
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
