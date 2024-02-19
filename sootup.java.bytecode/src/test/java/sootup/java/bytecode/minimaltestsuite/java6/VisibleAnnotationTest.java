package sootup.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;

import categories.Java8Test;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.signatures.PackageName;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaSootClass;
import sootup.java.core.types.AnnotationType;

@Category(Java8Test.class)
public class VisibleAnnotationTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void testVisibleAnnotationOnClassOrAnnotation() {
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());

    assertEquals(
        Collections.singletonList(
            new AnnotationUsage(
                new AnnotationType("InterfaceVisibleAnnotation", new PackageName(""), false),
                Collections.emptyMap())),
        sootClass.getAnnotations(Optional.of(customTestWatcher.getJavaView())));
  }
}
