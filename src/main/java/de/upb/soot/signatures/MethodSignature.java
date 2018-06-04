package de.upb.soot.signatures;

import java.util.List;

/** Represents the fully qualified signature of a method. */
public class MethodSignature {
  /** The method's signature. */
  public final String methodName;

  /** The signature of the declaring class. */
  public final ClassSignature declClassSignature;

  /** The method's paremeters' signatures. */
  public final List<ClassSignature> parameterSignatures;

  /** The return type's signature. */
  public final ClassSignature returnTypeSignature;

  /**
   * Internal: Constructs MethodSignature. Instances should only be created a {@link
   * SignatureFactory}
   *
   * @param methodName the signature
   * @param declaringClass the declaring class signature
   */
  protected MethodSignature(
      final String methodName,
      final ClassSignature declaringClass,
      final List<ClassSignature> parameters,
      final ClassSignature returnType) {
    this.methodName = methodName;
    this.declClassSignature = declaringClass;
    this.parameterSignatures = parameters;
    this.returnTypeSignature = returnType;
  }
}
