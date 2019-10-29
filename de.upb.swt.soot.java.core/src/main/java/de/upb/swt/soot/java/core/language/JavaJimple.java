package de.upb.swt.soot.java.core.language;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.java.core.DefaultIdentifierFactory;

public class JavaJimple extends Jimple {

  private static IdentifierFactory getIdentifierFactory() {
    return DefaultIdentifierFactory.getInstance();
  }
}
