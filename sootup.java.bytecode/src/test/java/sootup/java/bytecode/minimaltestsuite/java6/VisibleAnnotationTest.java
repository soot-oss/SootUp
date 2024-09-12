package sootup.java.bytecode.minimaltestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertEquals;

import categories.TestCategories;
import java.util.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.types.ClassType;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaSootClass;

@Tag(TestCategories.JAVA_8_CATEGORY)
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
