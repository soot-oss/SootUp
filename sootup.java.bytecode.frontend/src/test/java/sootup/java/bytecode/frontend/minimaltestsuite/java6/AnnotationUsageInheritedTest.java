package sootup.java.bytecode.frontend.minimaltestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.java.bytecode.frontend.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaSootClass;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;

public class AnnotationUsageInheritedTest extends MinimalBytecodeTestSuiteBase {

  private <T> Stream<T> toStream(Iterable<T> iterable) {
    return StreamSupport.stream(iterable.spliterator(), false);
  }

  @Test
  public void testInheritedAnnotationOnClass() {
    // this test just documents our current API: we do not take annotations of the
    // superclass hierarchy into account
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
    assertEquals(
        Collections.emptyList(),
        sootClass.getAnnotations()); // the annotation is attached to the superclass

    Optional<JavaClassType> superClassTypeOptional = sootClass.getSuperclass();
    assertTrue(superClassTypeOptional.isPresent());
    JavaClassType superClassType = superClassTypeOptional.get();

    Optional<JavaSootClass> superClassOptional = getJavaView().getClass(superClassType);
    assertTrue(superClassOptional.isPresent());

    List<AnnotationUsage> actualInheritedAnnotationUsages =
        toStream(superClassOptional.get().getAnnotations())
            .filter(
                au ->
                    toStream(getJavaView().getClass(au.getAnnotation()).get().getAnnotations())
                        .anyMatch(
                            innerAu -> "Inherited".equals(innerAu.getAnnotation().getClassName())))
            .collect(Collectors.toList());

    JavaClassType onClassType = identifierFactory.getClassType("OnClass");
    Map<String, Object> elementValueMap = new HashMap<>();
    elementValueMap.put("sthBlue", IntConstant.getInstance(42));
    elementValueMap.put("author", JavaJimple.getInstance().newStringConstant("GeorgeLucas"));
    assertEquals(
        Collections.singletonList(new AnnotationUsage(onClassType, elementValueMap)),
        actualInheritedAnnotationUsages);
  }
}
