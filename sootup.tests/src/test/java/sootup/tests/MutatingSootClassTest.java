package sootup.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.common.Local;
import sootup.core.model.Body;
import sootup.core.model.ClassModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ArrayType;
import sootup.core.types.PrimitiveType.IntType;
import sootup.core.types.VoidType;
import sootup.java.bytecode.frontend.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootClassSource;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.OverridingJavaClassSource;
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
    JavaClassType classType = view.getIdentifierFactory().getClassType("HelloWorld");

    // Create a signature for the method we want to analyze
    MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature(
                classType, "main", "void", Collections.singletonList("java.lang.String[]"));

    // Assert that class is present
    assertTrue(view.getClass(classType).isPresent());

    // Retrieve class
    JavaSootClass sootClass = view.getClass(classType).get();

    // Retrieve method
    assertTrue(view.getMethod(methodSignature).isPresent());
    JavaSootMethod method = view.getMethod(methodSignature).get();
    Body oldBody = method.getBody();

    // Create OverridingBodySource
    OverridingBodySource overridingBodySource =
        new OverridingBodySource(methodSignature, method.getBody());

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

    // Create new Method
    JavaSootMethod newMethod = method.withOverridingMethodSource(old -> newBodySource);

    OverridingJavaClassSource newClassSource =
        overridingJavaClassSource.withReplacedMethod(method, newMethod);
    SootClass newClass = sootClass.withClassSource(newClassSource);

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
  }
}
