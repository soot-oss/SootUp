package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.types.JavaClassType;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.Test;

public class AnnotationLibraryTest extends MinimalTestSuiteBase {

  @Override
  protected JavaClassType getDeclaredClassSignature() {
    return identifierFactory.getClassType(getClassName());
  }

  @Test
  public void defaultTest() {

    System.out.println(getDeclaredClassSignature());
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    // view.getClass().getAnnotationsByType(getDeclaredClassSignature());
    // SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(Modifier.isAnnotation(sootClass.getModifiers()));
  }
}
