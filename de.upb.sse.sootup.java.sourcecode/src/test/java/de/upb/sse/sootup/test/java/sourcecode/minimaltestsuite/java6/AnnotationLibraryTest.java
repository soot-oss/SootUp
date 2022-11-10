package de.upb.sse.sootup.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import de.upb.sse.sootup.core.model.Modifier;
import de.upb.sse.sootup.core.model.SootClass;
import de.upb.sse.sootup.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import org.junit.Ignore;

public class AnnotationLibraryTest extends MinimalSourceTestSuiteBase {

  @Ignore
  public void testAnnotation() {
    // TODO: [ms] annotations are not implemented yet
    System.out.println(getDeclaredClassSignature());
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(Modifier.isAnnotation(sootClass.getModifiers()));
  }
}
