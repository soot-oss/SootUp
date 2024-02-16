package sootup.java.bytecode.minimaltestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import sootup.core.signatures.PackageName;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaSootClass;
import sootup.java.core.types.AnnotationType;

@ExtendWith(MinimalBytecodeTestSuiteBase.CustomTestWatcher.class)
public class VisibleAnnotationTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void testVisibleAnnotationOnClassOrAnnotation() {
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());

    assertEquals(
        Arrays.asList(
            new AnnotationUsage(
                new AnnotationType("InterfaceVisibleAnnotation", new PackageName(""), false),
                Collections.emptyMap())),
        sootClass.getAnnotations(
            Optional.of(CustomTestWatcher.getCustomTestWatcher().getJavaView())));
  }
}
