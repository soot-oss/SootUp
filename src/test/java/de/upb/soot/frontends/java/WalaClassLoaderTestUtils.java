package de.upb.soot.frontends.java;

import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootMethod;
import de.upb.soot.core.SourceType;
import de.upb.soot.signatures.MethodSignature;
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
