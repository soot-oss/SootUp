package sootup.jimple.frontend.javatestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import sootup.core.model.ClassModifier;
import sootup.core.model.SootClass;
import sootup.jimple.frontend.javatestsuite.JimpleTestSuiteBase;

/** @author Kaustubh Kelkar */
@Tag("Java8")
public class AnnotationLibraryTest extends JimpleTestSuiteBase {

  @Disabled
  public void testAnnotation() {
    // TODO: annotations are not supported yet
    System.out.println(getDeclaredClassSignature());
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(ClassModifier.isAnnotation(sootClass.getModifiers()));
  }
}
