package de.upb.swt.soot.java.core.signatures;

import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.core.DefaultIdentifierFactory;

/**
 * Features class signatures for commonly used standard classes from the JDK
 *
 * @author Ben Hermann
 */
public final class CommonClassSignatures {

  public static final ClassType JavaLangObject =
      DefaultIdentifierFactory.getInstance().getClassType("Object", "java.lang");
}
