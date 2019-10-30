package de.upb.swt.soot.java.core.language;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.jimple.Jimple;
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

  // TODO: [ms] add constant instantiation too

}
