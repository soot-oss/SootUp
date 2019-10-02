package de.upb.swt.soot.core.signatures;

import de.upb.swt.soot.core.DefaultIdentifierFactory;
import de.upb.swt.soot.core.types.JavaClassType;

/**
 * Features class signatures for commonly used standard classes from the JDK
 *
 * @author Ben Hermann
 */
public final class CommonClassSignatures {

  public static final JavaClassType JavaLangObject =
      DefaultIdentifierFactory.getInstance().getClassType("Object", "java.lang");
}
