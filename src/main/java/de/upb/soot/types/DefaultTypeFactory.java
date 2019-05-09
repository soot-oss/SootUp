package de.upb.soot.types;

import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.PackageIdentifier;
import de.upb.soot.signatures.SignatureFactory;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ClassUtils;

public class DefaultTypeFactory implements TypeFactory {

  @Nonnull
  private static final DefaultTypeFactory INSTANCE =
      new DefaultTypeFactory(DefaultSignatureFactory::getInstance);

  public static DefaultTypeFactory getInstance() {
    return INSTANCE;
  }

  private final Supplier<? extends SignatureFactory> signatureFactorySupplier;

  public DefaultTypeFactory(
      @Nonnull Supplier<? extends SignatureFactory> signatureFactorySupplier) {
    this.signatureFactorySupplier = signatureFactorySupplier;
  }

  /**
   * Always creates a new ClassSignature. In opposite to PackageSignatures, ClassSignatures are not
   * cached because the are unique per class, and thus reusing them does not make sense.
   *
   * @param className the simple class name
   * @param packageName the Java package name; must not be null use empty string for the default
   *     package {@link PackageIdentifier#DEFAULT_PACKAGE} the Java package name
   * @return a ClassSignature for a Java class
   * @throws NullPointerException if the given package name is null. Use the empty string to denote
   *     the default package.
   */
  @Override
  public JavaClassType getClassType(final String className, final String packageName) {
    PackageIdentifier packageIdentifier =
        signatureFactorySupplier.get().getPackageSignature(packageName);
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
            this.getPrimitiveType(typeNameLowerCase)
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

    return this.getClassType(fullyQualifiedName);
  }
}
