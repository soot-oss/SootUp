package sootup.java.bytecode.frontend.minimaltestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.common.constant.BooleanConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaAnnotationSootClass;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootField;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.jimple.basic.JavaLocal;
import sootup.java.core.language.JavaJimple;

public class AnnotationUsageTest extends MinimalBytecodeTestSuiteBase {

  // we can only read: RetentionPolicy.RUNTIME annotations

  private boolean hasAnnotationByName(ClassType classType, String annotationName) {
    JavaSootClass sootClass = getJavaView().getClass(classType).get();
    Iterable<AnnotationUsage> annotations = sootClass.getAnnotations();
    return StreamSupport.stream(annotations.spliterator(), false)
        .anyMatch(au -> au.getAnnotation().getClassName().equals(annotationName));
  }

  private boolean hasInheritedMetaAnnotation(ClassType classType) {
    return hasAnnotationByName(classType, "Inherited");
  }

  @Test
  public void testAnnotationOnClassOrAnnotation() {
    // ElementType.ANNOTATION_TYPE can be applied to an annotation type.
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
    ClassType nonInheritableOnClassType = identifierFactory.getClassType("NonInheritableOnClass");

    ClassType onClassType = identifierFactory.getClassType("OnClass");
    Map<String, Object> elementValueMap = new HashMap<>();
    elementValueMap.put("sthBlue", IntConstant.getInstance(42));
    elementValueMap.put("author", JavaJimple.getInstance().newStringConstant("GeorgeLucas"));

    assertEquals(
        Arrays.asList(
            new AnnotationUsage(nonInheritableOnClassType, Collections.emptyMap()),
            new AnnotationUsage(onClassType, elementValueMap)),
        sootClass.getAnnotations());
    assertFalse(hasInheritedMetaAnnotation(nonInheritableOnClassType));
    assertTrue(hasInheritedMetaAnnotation(onClassType));
  }

  @Test
  public void testAnnotationOnField() {
    // ElementType.FIELD can be applied to a field or property.
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
    final Optional<JavaSootField> agent = sootClass.getField("agent");
    assertTrue(agent.isPresent());

    ClassType onFieldType = identifierFactory.getClassType("OnField");
    Map<String, Object> annotationParamMap = new HashMap<>();
    annotationParamMap.put("isRipe", JavaJimple.getInstance().newStringConstant("true"));

    assertEquals(
        Collections.singletonList(new AnnotationUsage(onFieldType, annotationParamMap)),
        agent.get().getAnnotations());
  }

  @Test
  public void testAnnotatedAnnotationInterface() {
    ClassType annotatedAnnotationInterfaceType =
        identifierFactory.getClassType("AnnotatedAnnotationInterface");
    Optional<JavaAnnotationSootClass> classOptional =
        getJavaView().getAnnotationClass(annotatedAnnotationInterfaceType);
    assertTrue(classOptional.isPresent());
    JavaAnnotationSootClass annotationSootClass = classOptional.get();

    // class has a OnClass annotation
    ClassType onClassType = identifierFactory.getClassType("OnClass");
    AnnotationUsage annotationUsage = new AnnotationUsage(onClassType, Collections.emptyMap());
    assertEquals(Collections.singletonList(annotationUsage), annotationSootClass.getAnnotations());

    // value method has a OnMethod annotation
    ClassType onMethodType = identifierFactory.getClassType("OnMethod");
    Map<String, Object> elementValueMap = new HashMap<>();
    elementValueMap.put("sthBorrowed", IntConstant.getInstance(-1));
    annotationUsage = new AnnotationUsage(onMethodType, elementValueMap);
    JavaSootMethod valueMethod = annotationSootClass.getMethodsByName("value").iterator().next();
    assertEquals(Collections.singletonList(annotationUsage), valueMethod.getAnnotations());
  }

  @Test
  public void testDefaultValues() {
    ClassType onFieldType = identifierFactory.getClassType("OnField");
    Optional<JavaAnnotationSootClass> classOptional = getJavaView().getAnnotationClass(onFieldType);
    assertTrue(classOptional.isPresent());
    JavaAnnotationSootClass annotationSootClass = classOptional.get();

    Map<String, Object> elementValueMap = new HashMap<>();
    elementValueMap.put("isRipe", JavaJimple.getInstance().newStringConstant("false"));
    elementValueMap.put("sthNew", IntConstant.getInstance(789));

    assertEquals(elementValueMap, annotationSootClass.getDefaultValues());
  }

  @Test
  public void testArrayDefaultValues() {
    ClassType arrayConstantType = identifierFactory.getClassType("ArrayConstant");
    Optional<JavaAnnotationSootClass> classOptional =
        getJavaView().getAnnotationClass(arrayConstantType);
    assertTrue(classOptional.isPresent());
    JavaAnnotationSootClass annotationSootClass = classOptional.get();

    Map<String, Object> elementValueMap = new HashMap<>();
    elementValueMap.put(
        "value",
        Arrays.asList(
            JavaJimple.getInstance().newStringConstant("first"),
            JavaJimple.getInstance().newStringConstant("second")));
    assertEquals(elementValueMap, annotationSootClass.getDefaultValues());
  }

  @Test
  public void testEnumDefaultValues() {
    ClassType enumAnnotationType = identifierFactory.getClassType("EnumAnnotation");
    Optional<JavaAnnotationSootClass> classOptional =
        getJavaView().getAnnotationClass(enumAnnotationType);
    assertTrue(classOptional.isPresent());
    JavaAnnotationSootClass annotationSootClass = classOptional.get();
    SootClass enumClass =
        loadClass(
            identifierFactory.getClassType(
                getDeclaredClassSignature().getFullyQualifiedName() + "$Enums"));

    Map<String, Object> elementValueMap = new HashMap<>();
    elementValueMap.put(
        "array",
        Arrays.asList(
            JavaJimple.getInstance()
                .newEnumConstant("ENUM1", enumClass.getType().getFullyQualifiedName()),
            JavaJimple.getInstance()
                .newEnumConstant("ENUM2", enumClass.getType().getFullyQualifiedName())));
    elementValueMap.put(
        "single",
        JavaJimple.getInstance()
            .newEnumConstant("ENUM3", enumClass.getType().getFullyQualifiedName()));
    assertEquals(elementValueMap, annotationSootClass.getDefaultValues());
  }

  @Test
  public void testClassDefaultValues() {
    ClassType classAnnotationType = identifierFactory.getClassType("ClassAnnotation");
    Optional<JavaAnnotationSootClass> classOptional =
        getJavaView().getAnnotationClass(classAnnotationType);
    assertTrue(classOptional.isPresent());
    JavaAnnotationSootClass annotationSootClass = classOptional.get();

    Map<String, Object> elementValueMap = new HashMap<>();
    elementValueMap.put(
        "array",
        Arrays.asList(
            JavaJimple.getInstance().newClassConstant("Ljava/lang/Boolean;"),
            JavaJimple.getInstance().newClassConstant("Ljava/lang/Double;")));
    elementValueMap.put("single", JavaJimple.getInstance().newClassConstant("Ljava/lang/Integer;"));
    assertEquals(elementValueMap, annotationSootClass.getDefaultValues());
  }

  @Test
  public void testContainerAnnotationDefaultValues() {
    ClassType onMethodRepeatablesType = identifierFactory.getClassType("OnMethodRepeatables");
    Optional<JavaAnnotationSootClass> classOptional =
        getJavaView().getAnnotationClass(onMethodRepeatablesType);
    assertTrue(classOptional.isPresent());
    JavaAnnotationSootClass annotationSootClass = classOptional.get();

    ClassType onMethodRepeatableType = identifierFactory.getClassType("OnMethodRepeatable");
    AnnotationUsage baseAnnotationUsage =
        new AnnotationUsage(
            onMethodRepeatableType,
            Collections.singletonMap("countOnMe", IntConstant.getInstance(1337)));

    Map<String, Object> elementValueMap = new HashMap<>();
    elementValueMap.put(
        "containerValue", JavaJimple.getInstance().newStringConstant("defaultValue"));
    elementValueMap.put("value", Collections.singletonList(baseAnnotationUsage));
    assertEquals(elementValueMap, annotationSootClass.getDefaultValues());
  }

  @Test
  public void testAnnotationWithNestedAnnotationDefaultValues() {
    ClassType annotationInterfaceNested2Type =
        identifierFactory.getClassType("AnnotationInterfaceNested2");
    Optional<JavaAnnotationSootClass> classOptional =
        getJavaView().getAnnotationClass(annotationInterfaceNested2Type);
    assertTrue(classOptional.isPresent());
    JavaAnnotationSootClass annotationSootClass = classOptional.get();

    ClassType onMethodType = identifierFactory.getClassType("OnMethod");
    Map<String, Object> onMethodElementValueMap = new HashMap<>();
    onMethodElementValueMap.put("isDuck", BooleanConstant.getTrue());
    onMethodElementValueMap.put("sthBorrowed", IntConstant.getInstance(4711));
    AnnotationUsage onMethodAnnotationUsage =
        new AnnotationUsage(onMethodType, onMethodElementValueMap);

    ClassType nestedAnnotationType = identifierFactory.getClassType("AnnotationInterfaceNested");
    Map<String, Object> nestedElementValueMap = new HashMap<>();
    nestedElementValueMap.put("value", onMethodAnnotationUsage);
    AnnotationUsage nestedAnnotationUsage =
        new AnnotationUsage(nestedAnnotationType, nestedElementValueMap);

    Map<String, Object> elementValueMap = new HashMap<>();
    elementValueMap.put("value", nestedAnnotationUsage);
    assertEquals(elementValueMap, annotationSootClass.getDefaultValues());
  }

  @Test
  public void testAnnotationOnMethod() {
    // ElementType.METHOD can be applied to a method-level annotation.
    {
      ClassType onMethodType = identifierFactory.getClassType("OnMethod");
      JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
      final Optional<JavaSootMethod> someMethod =
          sootClass.getMethod(
              identifierFactory
                  .getMethodSignature(
                      sootClass.getType(),
                      "someMethod",
                      "void",
                      Arrays.asList("int", "boolean", "int", "boolean"))
                  .getSubSignature());
      assertTrue(someMethod.isPresent());

      assertEquals(
          Collections.singletonList(new AnnotationUsage(onMethodType, Collections.emptyMap())),
          someMethod.get().getAnnotations());
    }

    // repeatable by repeating
    {
      JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
      final Optional<JavaSootMethod> someMethod =
          sootClass.getMethod("anotherMethod", Collections.emptyList());
      assertTrue(someMethod.isPresent());

      ClassType onMethodRepeatableType = identifierFactory.getClassType("OnMethodRepeatable");

      AnnotationUsage anno1 =
          new AnnotationUsage(
              onMethodRepeatableType,
              Collections.singletonMap("countOnMe", IntConstant.getInstance(1)));
      AnnotationUsage anno2 =
          new AnnotationUsage(
              onMethodRepeatableType,
              Collections.singletonMap("countOnMe", IntConstant.getInstance(2)));

      Map<String, Object> elementValueMap = new HashMap<>();
      elementValueMap.put("value", Arrays.asList(anno1, anno2));

      ClassType onMethodRepeatablesType = identifierFactory.getClassType("OnMethodRepeatables");
      assertEquals(
          Collections.singletonList(new AnnotationUsage(onMethodRepeatablesType, elementValueMap)),
          someMethod.get().getAnnotations());
    }

    // repeatable by using container
    {
      JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
      final Optional<JavaSootMethod> someMethod =
          sootClass.getMethod("<init>", Collections.emptyList());
      assertTrue(someMethod.isPresent());

      ClassType onMethodRepeatableType = identifierFactory.getClassType("OnMethodRepeatable");

      Map<String, Object> elementValueMap = new HashMap<>();
      elementValueMap.put(
          "containerValue", JavaJimple.getInstance().newStringConstant("betterValue"));

      AnnotationUsage anno1 =
          new AnnotationUsage(
              onMethodRepeatableType,
              Collections.singletonMap("countOnMe", IntConstant.getInstance(42)));

      elementValueMap.put("value", Collections.singletonList(anno1));

      ClassType onMethodRepeatablesType = identifierFactory.getClassType("OnMethodRepeatables");
      assertEquals(
          Collections.singletonList(new AnnotationUsage(onMethodRepeatablesType, elementValueMap)),
          someMethod.get().getAnnotations());
    }
  }

  @Test
  public void testAnnotationWithArrayOnMethod() {
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
    final Optional<JavaSootMethod> method =
        sootClass.getMethod("arrayConstant", Collections.emptyList());
    assertTrue(method.isPresent());

    ClassType arrayConstantType = identifierFactory.getClassType("ArrayConstant");
    Map<String, Object> elementValueMap = new HashMap<>();
    elementValueMap.put(
        "value",
        Arrays.asList(
            JavaJimple.getInstance().newStringConstant("test"),
            JavaJimple.getInstance().newStringConstant("test1")));
    assertEquals(
        Collections.singletonList(new AnnotationUsage(arrayConstantType, elementValueMap)),
        method.get().getAnnotations());
  }

  @Test
  public void testAnnotationWithEnumOnMethod() {
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
    final Optional<JavaSootMethod> method = sootClass.getMethod("enums", Collections.emptyList());
    assertTrue(method.isPresent());
    SootClass enumClass =
        loadClass(
            identifierFactory.getClassType(
                getDeclaredClassSignature().getFullyQualifiedName() + "$Enums"));
    assertTrue(enumClass.isEnum());

    ClassType enumAnnotationType = identifierFactory.getClassType("EnumAnnotation");
    Map<String, Object> elementValueMap = new HashMap<>();
    elementValueMap.put(
        "array",
        Arrays.asList(
            JavaJimple.getInstance()
                .newEnumConstant("ENUM3", enumClass.getType().getFullyQualifiedName()),
            JavaJimple.getInstance()
                .newEnumConstant("ENUM2", enumClass.getType().getFullyQualifiedName())));
    elementValueMap.put(
        "single",
        JavaJimple.getInstance()
            .newEnumConstant("ENUM1", enumClass.getType().getFullyQualifiedName()));
    assertEquals(
        Collections.singletonList(new AnnotationUsage(enumAnnotationType, elementValueMap)),
        method.get().getAnnotations());
  }

  @Test
  public void testAnnotationWithClassesOnMethod() {
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
    final Optional<JavaSootMethod> method = sootClass.getMethod("classes", Collections.emptyList());
    assertTrue(method.isPresent());

    ClassType classAnnotationType = identifierFactory.getClassType("ClassAnnotation");
    Map<String, Object> elementValueMap = new HashMap<>();
    elementValueMap.put(
        "array",
        Arrays.asList(
            JavaJimple.getInstance().newClassConstant("Ljava/lang/Integer;"),
            JavaJimple.getInstance().newClassConstant("Ljava/lang/String;")));
    elementValueMap.put("single", JavaJimple.getInstance().newClassConstant("Ljava/lang/Double;"));
    assertEquals(
        Collections.singletonList(new AnnotationUsage(classAnnotationType, elementValueMap)),
        method.get().getAnnotations());
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
              identifierFactory
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

      ClassType onParameterType = identifierFactory.getClassType("OnParameter");
      parameterLocal = (JavaLocal) body.getParameterLocal(1);
      // boolean with default annotation
      assertEquals(
          Collections.singletonList(new AnnotationUsage(onParameterType, Collections.emptyMap())),
          parameterLocal.getAnnotations());

      parameterLocal = (JavaLocal) body.getParameterLocal(2);
      // int
      assertEquals(Collections.emptyList(), parameterLocal.getAnnotations());

      parameterLocal = (JavaLocal) body.getParameterLocal(3);
      // boolean with annotation with custom value
      Map<String, Object> annotationParamMap = new HashMap<>();
      annotationParamMap.put("isBigDuck", BooleanConstant.getTrue());
      assertEquals(
          Collections.singletonList(new AnnotationUsage(onParameterType, annotationParamMap)),
          parameterLocal.getAnnotations());
    }
  }
}
