package sootup.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;

import java.util.*;
import org.junit.Test;
import sootup.core.signatures.PackageName;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.AnnotationType;

public class NestedAnnotationTest extends MinimalBytecodeTestSuiteBase {

  /**
   * The test is to check nested annotations. The annotations are of the
   * form @MyOuterAnnotation(innerAnnotation=[@MyInnerAnnotation(secondInnerAnnotation=[@MySecondInnerAnnotation(value="second")])])
   */
  @Test
  public void testNestedAnnotation() {
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());

    Map<String, Object> secondInnerAnnotationParamMap = new LinkedHashMap<>();
    secondInnerAnnotationParamMap.put(
        "value", JavaJimple.getInstance().newStringConstant("second"));
    AnnotationUsage secondInnerAnnotation =
        new AnnotationUsage(
            new AnnotationType("MySecondInnerAnnotation", new PackageName(""), false),
            secondInnerAnnotationParamMap);

    Map<String, Object> innerAnnotationParamMap = new LinkedHashMap<>();
    innerAnnotationParamMap.put(
        "secondInnerAnnotation", Collections.singletonList(secondInnerAnnotation));

    AnnotationUsage innerAnnotation =
        new AnnotationUsage(
            new AnnotationType("MyInnerAnnotation", new PackageName(""), false),
            innerAnnotationParamMap);

    Map<String, Object> outerAnnotationParams = new LinkedHashMap<>();
    outerAnnotationParams.put("innerAnnotation", Collections.singletonList(innerAnnotation));
    AnnotationUsage outerAnnotation =
        new AnnotationUsage(
            new AnnotationType("MyOuterAnnotation", new PackageName(""), false),
            outerAnnotationParams);

    ArrayList<AnnotationUsage> expectedAnnotation = new ArrayList<>();
    expectedAnnotation.add(outerAnnotation);
    assertEquals(
        expectedAnnotation, sootClass.getAnnotations(Optional.of(customTestWatcher.getJavaView())));
    Set<? extends JavaSootMethod> methods = sootClass.getMethods();
    assertEquals(methods.size(), 1);
  }
}
