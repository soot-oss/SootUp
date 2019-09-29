package de.upb.soot.javasourcecodefrontend.minimaltestsuite;

import de.upb.soot.core.DefaultIdentifierFactory;
import de.upb.soot.javasourcecodefrontend.frontend.WalaClassLoader;
import de.upb.soot.core.types.JavaClassType;

public class LoadClassesWithWala {
  public WalaClassLoader loader;
  public DefaultIdentifierFactory identifierFactory;
  public JavaClassType declareClassSig;

  public void classLoader(String srcDir, String className) {
    loader = new WalaClassLoader(srcDir, null);
    identifierFactory = DefaultIdentifierFactory.getInstance();
    declareClassSig = identifierFactory.getClassType(className);
  }
}
