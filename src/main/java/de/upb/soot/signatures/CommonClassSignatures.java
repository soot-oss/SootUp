package de.upb.soot.signatures;

/**
 * Features class signatures for commonly used standard classes from the JDK
 *
 * @author Ben Hermann
 * 
 */
public final class CommonClassSignatures {
  private static final SignatureFactory factory;
  static {
    factory = new DefaultSignatureFactory();
  }

  public static final JavaClassSignature JavaLangObject = factory.getClassSignature("Object", "java.lang");
}
