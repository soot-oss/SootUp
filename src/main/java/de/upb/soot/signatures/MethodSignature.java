package de.upb.soot.signatures;

import java.util.List;

/** Represents the fully qualified signature of a method. */
public class MethodSignature {
  /** The method's signature. */
  public final String methodName;

  /** The signature of the declaring class. */
  public final ClassSignature declClassSignature;

  /** The method's paremeters' signatures. */
  public final List<TypeSignature> parameterSignatures;

  /** The return type's signature. */
  public final TypeSignature returnTypeSignature;

  /**
   * Internal: Constructs a MethodSignature. Instances should only be created by a
   * {@link SignatureFactory}
   *
   * @param methodName the signature
   * @param declaringClass the declaring class signature
   */
  protected MethodSignature(
      final String methodName,
      final ClassSignature declaringClass,
      final List<TypeSignature> parameters,
      final TypeSignature returnType) {
    this.methodName = methodName;
    this.declClassSignature = declaringClass;
    this.parameterSignatures = parameters;
    this.returnTypeSignature = returnType;
  }
}
