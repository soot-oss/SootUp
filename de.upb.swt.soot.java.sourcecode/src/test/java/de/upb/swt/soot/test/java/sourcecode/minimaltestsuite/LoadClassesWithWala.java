package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite;

import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.sourcecode.frontend.WalaClassLoader;

public class LoadClassesWithWala {
  public WalaClassLoader loader;
  public JavaIdentifierFactory identifierFactory;
  public JavaClassType declareClassSig;

  public void classLoader(String srcDir, String className) {
    loader = new WalaClassLoader(srcDir);
    identifierFactory = JavaIdentifierFactory.getInstance();
    declareClassSig = identifierFactory.getClassType(className);
  }
}
