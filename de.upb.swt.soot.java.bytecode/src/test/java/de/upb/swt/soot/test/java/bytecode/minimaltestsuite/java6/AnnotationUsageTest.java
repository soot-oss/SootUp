package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.jimple.common.constant.BooleanConstant;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.java.core.AnnotationUsage;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.JavaSootField;
import de.upb.swt.soot.java.core.JavaSootMethod;
import de.upb.swt.soot.java.core.jimple.basic.JavaLocal;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.AnnotationType;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.junit.Test;

public class AnnotationUsageTest extends MinimalBytecodeTestSuiteBase {

  // we can only read: RetentionPolicy.RUNTIME annotations

  @Test
  public void testAnnotationOnClassOrAnnotation() {
    // ElementType.ANNOTATION_TYPE can be applied to an annotation type.
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());

    Map<String, Object> annotationParamMap = new HashMap<>();
    annotationParamMap.put("sthBlue", IntConstant.getInstance(42));
    annotationParamMap.put("author", JavaJimple.getInstance().newStringConstant("GeorgeLucas"));

    assertEquals(
        Arrays.asList(
            new AnnotationUsage(
                new AnnotationType("NonInheritableOnClass", new PackageName(""), false),
                Collections.emptyMap()),
            new AnnotationUsage(
                new AnnotationType("OnClass", new PackageName(""), true), annotationParamMap)),
        sootClass.getAnnotations(Optional.of(customTestWatcher.getJavaView())));
  }

  @Test
  public void testAnnotationOnField() {
    // ElementType.FIELD can be applied to a field or property.
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
    final Optional<JavaSootField> agent = sootClass.getField("agent");
    assertTrue(agent.isPresent());

    Map<String, Object> annotationParamMap = new HashMap<>();
    annotationParamMap.put("isRipe", JavaJimple.getInstance().newStringConstant("true"));

    assertEquals(
        Collections.singletonList(
            new AnnotationUsage(
                new AnnotationType("OnField", new PackageName(""), false), annotationParamMap)),
        agent.get().getAnnotations(Optional.of(customTestWatcher.getJavaView())));
  }

  @Test
  public void testDefaultValues() {
    /*
     * Use a Stub class for annotation usage, so default values are not resolved against the AnnotationType, which would make the test useless.
     * Default values are already contained in every other test, but as they are implicit, they are the same for expected and actual test result
     * This test just makes sure, that default values are correctly resolved. The logic is the same for fields, methods or classes, so this test suffices.
     */

    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
    final Optional<JavaSootField> agent = sootClass.getField("agent");
    assertTrue(agent.isPresent());

    class AnnotationUsageStub extends AnnotationUsage {

      public AnnotationUsageStub(@Nonnull AnnotationType annotation) {
        super(annotation, Collections.emptyMap());
      }

      @Override
      public Map<String, Object> getValuesWithDefaults() {
        Map<String, Object> map = new HashMap<>();

        map.put("isRipe", JavaJimple.getInstance().newStringConstant("true"));
        map.put("sthNew", IntConstant.getInstance(789));

        return map;
      }
    }

    assertEquals(
        Collections.singletonList(
                new AnnotationUsageStub(new AnnotationType("OnField", new PackageName(""), false)))
            .toString(),
        agent.get().getAnnotations(Optional.of(customTestWatcher.getJavaView())).toString());
  }

  @Test
  public void testAnnotationOnMethod() {
    // ElementType.METHOD can be applied to a method-level annotation.
    AnnotationType at0 = this.identifierFactory.getAnnotationType("OnMethod");

    {
      JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
      final Optional<JavaSootMethod> someMethod =
          sootClass.getMethod(
              JavaIdentifierFactory.getInstance()
                  .getMethodSignature(
                      sootClass.getType(),
                      "someMethod",
                      "void",
                      Arrays.asList("int", "boolean", "int", "boolean"))
                  .getSubSignature());
      assertTrue(someMethod.isPresent());

      assertEquals(
          Collections.singletonList(new AnnotationUsage(at0, Collections.emptyMap())),
          someMethod.get().getAnnotations(Optional.of(customTestWatcher.getJavaView())));
    }

    // repeatable by repeating
    {
      JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
      final Optional<JavaSootMethod> someMethod =
          sootClass.getMethod("anotherMethod", Collections.emptyList());
      assertTrue(someMethod.isPresent());

      AnnotationType at = this.identifierFactory.getAnnotationType("OnMethodRepeatable");
      AnnotationType at2 = this.identifierFactory.getAnnotationType("OnMethodRepeatables");

      Map<String, Object> annotationParamMap = new HashMap<>();

      AnnotationUsage anno1 =
          new AnnotationUsage(
              at, Collections.singletonMap("countOnMe", IntConstant.getInstance(1)));
      AnnotationUsage anno2 =
          new AnnotationUsage(
              at, Collections.singletonMap("countOnMe", IntConstant.getInstance(2)));

      annotationParamMap.put("value", Arrays.asList(anno1, anno2));

      assertEquals(
          Collections.singletonList(new AnnotationUsage(at2, annotationParamMap)),
          someMethod.get().getAnnotations(Optional.of(customTestWatcher.getJavaView())));
    }

    // repeatable by using container
    {
      JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
      final Optional<JavaSootMethod> someMethod =
          sootClass.getMethod("<init>", Collections.emptyList());
      assertTrue(someMethod.isPresent());

      AnnotationType at = this.identifierFactory.getAnnotationType("OnMethodRepeatable");
      AnnotationType at2 = this.identifierFactory.getAnnotationType("OnMethodRepeatables");

      Map<String, Object> annotationParamMap = new HashMap<>();
      annotationParamMap.put(
          "containerValue", JavaJimple.getInstance().newStringConstant("betterValue"));

      AnnotationUsage anno1 =
          new AnnotationUsage(
              at, Collections.singletonMap("countOnMe", IntConstant.getInstance(42)));

      annotationParamMap.put("value", Collections.singletonList(anno1));

      assertEquals(
          Collections.singletonList(new AnnotationUsage(at2, annotationParamMap)),
          someMethod.get().getAnnotations(Optional.of(customTestWatcher.getJavaView())));
    }
  }

  @Test
  public void testAnnotationOnLocal() {
    // ElementType.LOCAL_VARIABLE can be applied to a local variable. -> per JLS 9.6.4.2 this
    // information is not contained in bytecode
    // ElementType.PARAMETER can be applied to the parameters of a method.

    {
      JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
      final Optional<JavaSootMethod> someMethod =
          sootClass.getMethod(
              JavaIdentifierFactory.getInstance()
                  .getMethodSignature(
                      sootClass.getType(),
                      "someMethod",
                      "void",
                      Arrays.asList("int", "boolean", "int", "boolean"))
                  .getSubSignature());
      assertTrue(someMethod.isPresent());
      Body body = someMethod.get().getBody();
      assert body != null;
      JavaLocal parameterLocal = (JavaLocal) body.getParameterLocal(0);

      // parameter local annotation
      // int
      assertEquals(Collections.emptyList(), parameterLocal.getAnnotations());

      parameterLocal = (JavaLocal) body.getParameterLocal(1);
      // boolean with default annotation
      assertEquals(
          Collections.singletonList(
              new AnnotationUsage(
                  new AnnotationType("OnParameter", new PackageName(""), false),
                  Collections.emptyMap())),
          parameterLocal.getAnnotations());

      parameterLocal = (JavaLocal) body.getParameterLocal(2);
      // int
      assertEquals(Collections.emptyList(), parameterLocal.getAnnotations());

      parameterLocal = (JavaLocal) body.getParameterLocal(3);
      // boolean with annotation with custom value
      Map<String, Object> annotationParamMap = new HashMap<>();
      annotationParamMap.put("isBigDuck", BooleanConstant.getTrue());
      assertEquals(
          Collections.singletonList(
              new AnnotationUsage(
                  new AnnotationType("OnParameter", new PackageName(""), false),
                  annotationParamMap)),
          parameterLocal.getAnnotations());
    }
  }
}
