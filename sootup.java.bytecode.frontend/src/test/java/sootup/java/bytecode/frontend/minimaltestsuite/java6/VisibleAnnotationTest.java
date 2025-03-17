package sootup.java.bytecode.frontend.minimaltestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import org.junit.jupiter.api.Test;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaSootClass;

public class VisibleAnnotationTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void testVisibleAnnotationOnClassOrAnnotation() {
    /*
     * Actually, we could remove this test because sootClass.getAnnotations
     * does not take any hierarchy into account.
     */
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
    ClassType interfaceVisibleAnnotationType =
        identifierFactory.getClassType("InterfaceVisibleAnnotation");

    assertEquals(
        Collections.singletonList(
            new AnnotationUsage(interfaceVisibleAnnotationType, Collections.emptyMap())),
        sootClass.getAnnotations());
  }
}
