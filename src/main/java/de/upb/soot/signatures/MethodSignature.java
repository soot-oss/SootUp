package de.upb.soot.signatures;

/** Represents the fully qualified signature of a method. */
public class MethodSignature {
  /** The method's signature. */
  public final String methodID;

  /** The signature of the class that defines the method. */
  public final ClassSignature classSignature;

  /**
   * Internal: Constructs MethodSignature.
   * Instances should only be created a {@link SignatureFactory)
   * @param methodID the signature
   * @param classSignature the declaring class signature
   */
  protected MethodSignature(final String methodID, final ClassSignature classSignature) {
    this.methodID = methodID;
    this.classSignature = classSignature;
  }
}
