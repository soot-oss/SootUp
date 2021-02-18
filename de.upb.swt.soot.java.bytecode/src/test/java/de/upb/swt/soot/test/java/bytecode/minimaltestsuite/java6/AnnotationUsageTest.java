package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;

import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.java.core.AnnotationUsage;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.types.AnnotationType;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class AnnotationUsageTest extends MinimalBytecodeTestSuiteBase {

  // we can only read: RetentionPolicy.RUNTIME annotations
  // TODO: check if we can read RetentionPolicy.CLASS too
  // hint: dont forget ElementType.TYPE can be applied to any element of a class.
  // TODO: @Inherited -> do we need to check that additionally on superclass or is it enhanced by
  // the compile?

  // TODO: test multiple (repeated) annotations

  @Test
  public void testAnnotationOnPackage() {
    // TODO: ElementType.PACKAGE can be applied to a package declaration.
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
    assertEquals(
        Arrays.asList(new AnnotationType("OnPackage", new PackageName("test.annotation"))),
        sootClass.getAnnotations());
  }

  @Test
  public void testAnnotationOnClassOrAnnotation() {
    // ElementType.ANNOTATION_TYPE can be applied to an annotation type.
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
    assertEquals(
        Arrays.asList(
            new AnnotationUsage(
                new AnnotationType("OnClass", new PackageName("test.annotation")),
                Collections.emptyMap())),
        sootClass.getAnnotations());
  }

  /*
  @Test
  public void testAnnotationOnField() {
    // ElementType.FIELD can be applied to a field or property.
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
    final Optional<JavaSootField> agent = sootClass.getField("agent");
    assertTrue(agent.isPresent());
    assertEquals(
        Arrays.asList(new AnnotationType("OnField", new PackageName("test.annotation"))),
        agent.get().getAnnotations());
  }

  @Test
  public void testAnnotationOnMethod() {
    // ElementType.METHOD can be applied to a method-level annotation.
    {
      JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
      final Optional<JavaSootMethod> someMethod =
          sootClass.getMethod("someMethod", Collections.emptyList());
      assertTrue(someMethod.isPresent());
      assertEquals(
          Arrays.asList(new AnnotationType("OnMethod", new PackageName("test.annotation"))),
          someMethod.get().getAnnotations());
    }

    // ElementType.CONSTRUCTOR can be applied to a constructor.
    {
      JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
      final Optional<JavaSootMethod> someMethod =
          sootClass.getMethod("<init>", Collections.emptyList());
      assertTrue(someMethod.isPresent());
      assertEquals(
          Arrays.asList(new AnnotationType("OnMethod", new PackageName("test.annotation"))),
          someMethod.get().getAnnotations());
    }
  }

  @Test
  @Ignore
  public void testAnnotationOnLocal() {
    // ElementType.LOCAL_VARIABLE can be applied to a local variable.
    // ElementType.PARAMETER can be applied to the parameters of a method.
    // TODO: not implemented/modeled yet

    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
    assertEquals(
        Arrays.asList(new AnnotationType("OnLocal", new PackageName("test.annotation"))),
        sootClass.getAnnotations());
  }
   */
}
