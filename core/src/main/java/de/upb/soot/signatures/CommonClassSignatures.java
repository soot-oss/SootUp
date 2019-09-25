package de.upb.soot.signatures;

import de.upb.soot.DefaultIdentifierFactory;
import de.upb.soot.types.JavaClassType;

/**
 * Features class signatures for commonly used standard classes from the JDK
 *
 * @author Ben Hermann
 */
public final class CommonClassSignatures {

  public static final JavaClassType JavaLangObject =
      DefaultIdentifierFactory.getInstance().getClassType("Object", "java.lang");
}
