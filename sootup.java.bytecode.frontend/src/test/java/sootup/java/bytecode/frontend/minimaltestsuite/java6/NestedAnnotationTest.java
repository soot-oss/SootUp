package sootup.java.bytecode.frontend.minimaltestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import org.junit.jupiter.api.Test;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.language.JavaJimple;

public class NestedAnnotationTest extends MinimalBytecodeTestSuiteBase {

  /**
   * The test is to check nested annotations. The annotations are of the
   * form @MyOuterAnnotation(innerAnnotation=@MyInnerAnnotation(secondInnerAnnotation=@MySecondInnerAnnotation(value="second")))
   */
  @Test
  public void testNestedAnnotation() {
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());

    ClassType mySecondInnerAnnotationType =
        identifierFactory.getClassType("MySecondInnerAnnotation");
    Map<String, Object> secondInnerAnnotationParamMap = new LinkedHashMap<>();
    secondInnerAnnotationParamMap.put(
        "value", JavaJimple.getInstance().newStringConstant("second"));
    AnnotationUsage secondInnerAnnotation =
        new AnnotationUsage(mySecondInnerAnnotationType, secondInnerAnnotationParamMap);

    ClassType myInnerAnnotationType = identifierFactory.getClassType("MyInnerAnnotation");
    Map<String, Object> innerAnnotationParamMap = new LinkedHashMap<>();
    innerAnnotationParamMap.put("secondInnerAnnotation", secondInnerAnnotation);

    AnnotationUsage innerAnnotation =
        new AnnotationUsage(myInnerAnnotationType, innerAnnotationParamMap);

    ClassType myOuterAnnotationType = identifierFactory.getClassType("MyOuterAnnotation");
    Map<String, Object> outerAnnotationParams = new LinkedHashMap<>();
    outerAnnotationParams.put("innerAnnotation", innerAnnotation);
    AnnotationUsage outerAnnotation =
        new AnnotationUsage(myOuterAnnotationType, outerAnnotationParams);

    ArrayList<AnnotationUsage> expectedAnnotation = new ArrayList<>();
    expectedAnnotation.add(outerAnnotation);
    assertEquals(expectedAnnotation, sootClass.getAnnotations());
    Set<? extends JavaSootMethod> methods = sootClass.getMethods();
    assertEquals(methods.size(), 1);
  }
}
