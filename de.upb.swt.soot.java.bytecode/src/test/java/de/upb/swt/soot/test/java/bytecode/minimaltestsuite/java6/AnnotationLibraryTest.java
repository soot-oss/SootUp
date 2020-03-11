package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class AnnotationLibraryTest extends MinimalBytecodeTestSuiteBase {

  @Ignore
  public void testAnnotation() {
    // TODO: annotations are not supported yet
    System.out.println(getDeclaredClassSignature());
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(Modifier.isAnnotation(sootClass.getModifiers()));
  }
}
