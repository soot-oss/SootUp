package de.upb.sse.sootup.test.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;

import de.upb.sse.sootup.core.jimple.common.constant.IntConstant;
import de.upb.sse.sootup.core.signatures.PackageName;
import de.upb.sse.sootup.java.core.AnnotationUsage;
import de.upb.sse.sootup.java.core.JavaSootClass;
import de.upb.sse.sootup.java.core.language.JavaJimple;
import de.upb.sse.sootup.java.core.types.AnnotationType;
import de.upb.sse.sootup.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;

public class AnnotationUsageInheritedTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void testInheritedAnnotationOnClass() {
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
    Map<String, Object> annotationParamMap = new HashMap<>();

    annotationParamMap.put("sthBlue", IntConstant.getInstance(42));
    annotationParamMap.put("author", JavaJimple.getInstance().newStringConstant("GeorgeLucas"));

    assertEquals(
        Arrays.asList(
            new AnnotationUsage(
                new AnnotationType("OnClass", new PackageName(""), true), annotationParamMap)),
        sootClass.getAnnotations(Optional.of(customTestWatcher.getJavaView())));
  }
}
