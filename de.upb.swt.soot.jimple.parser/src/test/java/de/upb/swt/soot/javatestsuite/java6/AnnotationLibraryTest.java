package de.upb.swt.soot.javatestsuite.java6;

import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootClass;
import org.junit.Ignore;
import org.junit.Test;

public class AnnotationLibraryTest extends MinimalTestSuiteBase {

  @Test
  public void test() {}

  @Ignore
  public void testAnnotation() {
    System.out.println(getDeclaredClassSignature());
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(Modifier.isAnnotation(sootClass.getModifiers()));
  }
}
