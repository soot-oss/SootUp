package de.upb.swt.soot.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;

import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import de.upb.swt.soot.java.core.AnnotationUsage;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.AnnotationType;
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
