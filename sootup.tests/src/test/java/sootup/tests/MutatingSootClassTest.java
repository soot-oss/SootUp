package sootup.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.common.Local;
import sootup.core.model.Body;
import sootup.core.model.ClassModifier;
import sootup.core.model.FieldModifier;
import sootup.core.model.LinePosition;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.FieldSubSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ArrayType;
import sootup.core.types.PrimitiveType.BooleanType;
import sootup.core.types.PrimitiveType.IntType;
import sootup.core.types.VoidType;
import sootup.java.bytecode.frontend.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.*;
import sootup.java.core.JavaSootField.JavaSootFieldBuilder;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

public class MutatingSootClassTest {

  @Test
  public void test() {
    // Create a AnalysisInputLocation, which points to a directory. All class files will be loaded
    // from the directory
    AnalysisInputLocation inputLocation =
        PathBasedAnalysisInputLocation.create(
            Paths.get("src/test/resources/mutation/binary"), SourceType.Application);

    // Create a view for project, which allows us to retrieve classes
    JavaView view = new JavaView(inputLocation);

    // Create a signature for the class we want to analyze
    JavaIdentifierFactory identifierFactory = view.getIdentifierFactory();
    JavaClassType classType = identifierFactory.getClassType("HelloWorld");

    // Create a signature for the method we want to analyze
    MethodSignature methodSignature =
        identifierFactory.getMethodSignature(classType, identifierFactory.getMainSubSignature());

    // Assert that class is present
    assertTrue(view.getClass(classType).isPresent());

    // Retrieve class
    JavaSootClass sootClass = view.getClass(classType).get();

    // Retrieve method
    assertTrue(view.getMethod(methodSignature).isPresent());
    JavaSootMethod method = view.getMethod(methodSignature).get();
    Body oldBody = method.getBody();

    // Create Local
    Local newLocal = JavaJimple.newLocal("helloWorldLocal", IntType.getInt());

    // Specify new Method Body
    Body newBody = oldBody.withLocals(Collections.singleton(newLocal));

    // Modify body source
    OverridingBodySource newBodySource =
        new OverridingBodySource(method.getBodySource()).withBody(newBody);

    // Create OverridingJavaClassSource
    OverridingJavaClassSource overridingJavaClassSource =
        new OverridingJavaClassSource((JavaSootClassSource) sootClass.getClassSource());

    assertTrue(overridingJavaClassSource.resolveOuterClass().isEmpty());
    assertTrue(overridingJavaClassSource.resolveFields().isEmpty());
    assertTrue(overridingJavaClassSource.resolveInterfaces().isEmpty());
    assertEquals(NoPositionInformation.getInstance(), overridingJavaClassSource.resolvePosition());
    Set<ClassModifier> modifiers = Set.of(ClassModifier.PUBLIC, ClassModifier.SUPER);
    assertEquals(modifiers, overridingJavaClassSource.resolveModifiers());
    assertFalse(overridingJavaClassSource.resolveAnnotations().iterator().hasNext());
    overridingJavaClassSource
        .resolveSuperclass()
        .ifPresent(
            classType1 -> assertEquals("java.lang.Object", classType1.getFullyQualifiedName()));

    // Create new Method
    JavaSootMethod newMethod = method.withOverridingMethodSource(old -> newBodySource);
    JavaSootField newField =
        JavaSootFieldBuilder.builder()
            .withModifier(Collections.singletonList(FieldModifier.PUBLIC))
            .withPosition(NoPositionInformation.getInstance())
            .withSignature(
                new FieldSignature(
                    sootClass.getType(), new FieldSubSignature("g", IntType.getInt())))
            .build();

    OverridingJavaClassSource newClassSource =
        overridingJavaClassSource
            .withReplacedMethod(method, newMethod)
            .withFields(Collections.singletonList(newField));
    SootClass newClass = sootClass.withClassSource(newClassSource);

    assertEquals(newField, newClass.getField("g").orElse(null));

    // assert that only our newly created local exists
    SootMethod methodNew =
        newClass
            .getMethod(
                new MethodSubSignature(
                    "main",
                    Collections.singletonList(
                        new ArrayType(
                            new JavaClassType("String", new PackageName("java.lang")), 1)),
                    VoidType.getInstance()))
            .orElse(null);
    assertNotNull(methodNew);
    assertEquals(newLocal, methodNew.getBody().getLocals().stream().findFirst().orElse(null));

    // assert that old soot class remains unchanged
    SootMethod constructorNew =
        sootClass
            .getMethod(
                new MethodSubSignature("<init>", Collections.emptyList(), VoidType.getInstance()))
            .orElse(null);
    assertNotNull(constructorNew);
    assertFalse(constructorNew.getBody().getLocals().isEmpty());

    SootMethod oldMethod =
        sootClass
            .getMethod(
                new MethodSubSignature(
                    "main",
                    Collections.singletonList(
                        new ArrayType(
                            new JavaClassType("String", new PackageName("java.lang")), 1)),
                    VoidType.getInstance()))
            .orElse(null);
    assertNotNull(oldMethod);
    assertTrue(oldMethod.getBody().getLocals().stream().noneMatch(local -> local.equals(newLocal)));

    JavaSootField replacedField =
        newField
            .withModifiers(Collections.singletonList(FieldModifier.PRIVATE))
            .withSignature(
                new FieldSignature(
                    newField.getDeclaringClassType(),
                    new FieldSubSignature(newField.getName(), BooleanType.getInstance())));
    OverridingJavaClassSource newerClassSource =
        overridingJavaClassSource
            .withReplacedField(newField, replacedField)
            .withModifiers(Set.of(ClassModifier.PRIVATE))
            .withPosition(new LinePosition(1))
            .withOuterClass(Optional.empty())
            .withInterfaces(Set.of());
    SootClass newerClass = sootClass.withClassSource(newerClassSource);

    assertEquals(Set.of(ClassModifier.PRIVATE), newerClass.getModifiers());
    assertEquals(1, newerClass.getPosition().getFirstLine());
    SootField checkedField = newerClass.getField("g").orElse(null);
    assertNotNull(checkedField);
    assertEquals(BooleanType.getInstance(), checkedField.getType());
    assertEquals(Set.of(FieldModifier.PRIVATE), checkedField.getModifiers());
  }
}
