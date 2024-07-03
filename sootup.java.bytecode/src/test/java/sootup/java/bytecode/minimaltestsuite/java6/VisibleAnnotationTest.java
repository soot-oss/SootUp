package sootup.java.bytecode.minimaltestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertEquals;

import categories.TestCategories;
import java.util.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.signatures.PackageName;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaSootClass;
import sootup.java.core.types.AnnotationType;

@Tag(TestCategories.JAVA_8_CATEGORY)
public class VisibleAnnotationTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void testVisibleAnnotationOnClassOrAnnotation() {
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());

    assertEquals(
        Collections.singletonList(
            new AnnotationUsage(
                new AnnotationType("InterfaceVisibleAnnotation", new PackageName(""), false),
                Collections.emptyMap())),
        sootClass.getAnnotations(Optional.of(MinimalBytecodeTestSuiteBase.getJavaView())));
  }
}
