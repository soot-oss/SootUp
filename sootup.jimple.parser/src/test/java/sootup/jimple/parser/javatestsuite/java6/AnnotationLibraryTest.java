package sootup.jimple.parser.javatestsuite.java6;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.experimental.categories.Category;
import sootup.core.model.ClassModifier;
import sootup.core.model.SootClass;
import sootup.jimple.parser.categories.Java8Test;
import sootup.jimple.parser.javatestsuite.JimpleTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class AnnotationLibraryTest extends JimpleTestSuiteBase {

  @Ignore
  public void testAnnotation() {
    // TODO: annotations are not supported yet
    System.out.println(getDeclaredClassSignature());
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(ClassModifier.isAnnotation(sootClass.getModifiers()));
  }
}
