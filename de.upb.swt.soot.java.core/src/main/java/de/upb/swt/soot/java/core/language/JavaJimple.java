package de.upb.swt.soot.java.core.language;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.common.ref.JCaughtExceptionRef;
import de.upb.swt.soot.core.types.NullType;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.VoidType;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;

/**
 * JavaJimple implements the Java specific terms for {@link Jimple}
 *
 * @author Markus Schmidt
 */
public class JavaJimple extends Jimple {

  private static final JavaJimple INSTANCE = new JavaJimple();

  public static JavaJimple getInstance() {
    return INSTANCE;
  }

  @Override
  public IdentifierFactory getIdentifierFactory() {
    return JavaIdentifierFactory.getInstance();
  }

  public static boolean isJavaKeywordType(Type t) {
    // TODO: [JMP] Ensure that the check is complete.
    return t instanceof PrimitiveType || t instanceof VoidType || t instanceof NullType;
  }

  public JCaughtExceptionRef newCaughtExceptionRef() {
    return new JCaughtExceptionRef(getIdentifierFactory().getType("java.lang.Throwable"));
  }

  // TODO: [ms] add constant instantiation too

}
