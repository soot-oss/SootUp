package de.upb.soot.signatures;

import de.upb.soot.types.DefaultTypeFactory;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.TypeFactory;

/**
 * Features class signatures for commonly used standard classes from the JDK
 *
 * @author Ben Hermann
 */
public final class CommonClassSignatures {
  private static final TypeFactory TYPE_FACTORY =
      new DefaultTypeFactory(CommonClassSignatures::getSignatureFactory);
  private static final SignatureFactory SIGNATURE_FACTORY =
      new DefaultSignatureFactory(CommonClassSignatures::getTypeFactory);

  private static TypeFactory getTypeFactory() {
    return TYPE_FACTORY;
  }

  private static SignatureFactory getSignatureFactory() {
    return SIGNATURE_FACTORY;
  }

  public static final JavaClassType JavaLangObject =
      TYPE_FACTORY.getClassType("Object", "java.lang");
}
