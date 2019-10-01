package de.upb.soot.javasourcecodefrontend.frontend;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.java.sourcecode.frontend.WalaClassLoader;
import java.util.Optional;

public class WalaClassLoaderTestUtils {
  public static Optional<SootMethod> getSootMethod(
      WalaClassLoader walaClassLoader, MethodSignature signature) {
    // We let getClassSource do the hard work for us. This also
    // initializes the SootMethod correctly to know about its declaring
    // class.
    return walaClassLoader
        .getClassSource(signature.getDeclClassType())
        .map(cs -> new SootClass(cs, SourceType.Application))
        .flatMap(sootClass -> sootClass.getMethod(signature));
  }
}
