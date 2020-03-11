package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
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
