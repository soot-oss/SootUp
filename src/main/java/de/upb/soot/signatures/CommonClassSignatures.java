package de.upb.soot.signatures;

/**
 * Features class signatures for commonly used standard classes from the JDK
 *
 * @author Ben Hermann
 */
public final class CommonClassSignatures {
  private static final SignatureFactory factory = new DefaultSignatureFactory();

  public static final JavaClassType JavaLangObject = factory.getClassType("Object", "java.lang");
}
