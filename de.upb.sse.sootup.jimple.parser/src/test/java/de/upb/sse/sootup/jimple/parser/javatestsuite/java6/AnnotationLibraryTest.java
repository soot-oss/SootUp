package de.upb.sse.sootup.jimple.parser.javatestsuite.java6;

import static org.junit.Assert.assertTrue;

import de.upb.sse.sootup.core.model.Modifier;
import de.upb.sse.sootup.core.model.SootClass;
import de.upb.sse.sootup.jimple.parser.categories.Java8Test;
import de.upb.sse.sootup.jimple.parser.javatestsuite.JimpleTestSuiteBase;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class AnnotationLibraryTest extends JimpleTestSuiteBase {

  @Ignore
  public void testAnnotation() {
    // TODO: annotations are not supported yet
    System.out.println(getDeclaredClassSignature());
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(Modifier.isAnnotation(sootClass.getModifiers()));
  }
}
