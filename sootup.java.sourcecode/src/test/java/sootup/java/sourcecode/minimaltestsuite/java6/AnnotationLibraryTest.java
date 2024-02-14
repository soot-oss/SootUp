package sootup.java.sourcecode.minimaltestsuite.java6;


import org.junit.jupiter.api.Disabled;
import sootup.core.model.ClassModifier;
import sootup.core.model.SootClass;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnnotationLibraryTest extends MinimalSourceTestSuiteBase {

  @Disabled
  public void testAnnotation() {
    // TODO: [ms] annotations are not implemented yet
    System.out.println(getDeclaredClassSignature());
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(ClassModifier.isAnnotation(sootClass.getModifiers()));
  }
}
