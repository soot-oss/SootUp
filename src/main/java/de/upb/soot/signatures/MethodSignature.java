package de.upb.soot.signatures;

/** Represents the fully qualified signature of a method. */
public class MethodSignature {
  /** The method's signature. */
  public final String methodId;

  /** The signature of the class that defines the method. */
  public final ClassSignature classSignature;

  /**
   * Internal: Constructs MethodSignature.
   * Instances should only be created a {@link SignatureFactory}
   * @param methodId the signature
   * @param classSignature the declaring class signature
   */
  protected MethodSignature(final String methodId, final ClassSignature classSignature) {
    this.methodId = methodId;
    this.classSignature = classSignature;
  }
}
