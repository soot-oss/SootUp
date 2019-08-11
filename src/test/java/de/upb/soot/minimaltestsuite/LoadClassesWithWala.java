package de.upb.soot.minimaltestsuite;

import de.upb.soot.DefaultIdentifierFactory;
import de.upb.soot.types.JavaClassType;

public class LoadClassesWithWala {
  public de.upb.soot.frontends.java.WalaClassLoader loader;
  public DefaultIdentifierFactory identifierFactory;
  public JavaClassType declareClassSig;

  public void classLoader(String srcDir, String className) {
    loader = new de.upb.soot.frontends.java.WalaClassLoader(srcDir, null);
    identifierFactory = DefaultIdentifierFactory.getInstance();
    declareClassSig = identifierFactory.getClassType(className);
  }
}
