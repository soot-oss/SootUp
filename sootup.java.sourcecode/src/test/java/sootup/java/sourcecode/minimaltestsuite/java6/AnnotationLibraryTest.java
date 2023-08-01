package sootup.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import sootup.core.model.ClassModifier;
import sootup.core.model.SootClass;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

public class AnnotationLibraryTest extends MinimalSourceTestSuiteBase {

  @Ignore
  public void testAnnotation() {
    // TODO: [ms] annotations are not implemented yet
    System.out.println(getDeclaredClassSignature());
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(ClassModifier.isAnnotation(sootClass.getModifiers()));
  }
}
