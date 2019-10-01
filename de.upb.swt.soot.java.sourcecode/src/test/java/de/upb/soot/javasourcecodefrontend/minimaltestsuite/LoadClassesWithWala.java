package de.upb.soot.javasourcecodefrontend.minimaltestsuite;

import de.upb.swt.soot.core.DefaultIdentifierFactory;
import de.upb.swt.soot.core.types.JavaClassType;
import de.upb.swt.soot.java.sourcecode.frontend.WalaClassLoader;

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
