package sootup.java.sourcecode.minimaltestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import sootup.core.model.ClassModifier;
import sootup.core.model.SootClass;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

@Tag("Java8")
public class AnnotationLibraryTest extends MinimalSourceTestSuiteBase {

  @Disabled
  public void testAnnotation() {
    // TODO: [ms] annotations are not implemented yet
    System.out.println(getDeclaredClassSignature());
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(ClassModifier.isAnnotation(sootClass.getModifiers()));
  }
}
