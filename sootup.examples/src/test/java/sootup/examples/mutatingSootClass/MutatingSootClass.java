package sootup.examples.mutatingSootClass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.Collections;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.Local;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ArrayType;
import sootup.core.types.PrimitiveType.IntType;
import sootup.core.types.VoidType;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.OverridingJavaClassSource;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

/**
 * This example shows how to change a method inside a SootClass using OverridingBodySources and
 * OverridingClassSources.
 *
 * @author Bastian Haverkamp
 */
@Tag("Java8")
public class MutatingSootClass {

  @Disabled
  public void test() {
    // Create a AnalysisInputLocation, which points to a directory. All class files will be loaded
    // from the directory
    AnalysisInputLocation inputLocation =
        PathBasedAnalysisInputLocation.create(
            Paths.get("src/test/resources/BasicSetup/binary"), null);

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

    System.out.println(oldBody);

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

    // Create OverridingClassSource
    OverridingJavaClassSource overridingJavaClassSource =
        new OverridingJavaClassSource(sootClass.getClassSource());

    // Create new Method
    JavaSootMethod newMethod = method.withOverridingMethodSource(old -> newBodySource);

    OverridingJavaClassSource newClassSource =
        overridingJavaClassSource.withReplacedMethod(method, newMethod);
    SootClass newClass = sootClass.withClassSource(newClassSource);

    System.out.println(newClass.getMethods().stream().findFirst().get().getBody());

    // assert that only our newly created local exists
    assertEquals(
        newLocal,
        newClass
            .getMethod(
                new MethodSubSignature(
                    "main",
                    Collections.singletonList(
                        new ArrayType(
                            new JavaClassType("String", new PackageName("java.lang")), 1)),
                    VoidType.getInstance()))
            .get().getBody().getLocals().stream()
            .findFirst()
            .get());

    // assert that old soot class remains unchanged
    assertFalse(
        sootClass
            .getMethod(
                new MethodSubSignature("<init>", Collections.emptyList(), VoidType.getInstance()))
            .get()
            .getBody()
            .getLocals()
            .isEmpty());
    assertTrue(
        sootClass
            .getMethod(
                new MethodSubSignature(
                    "main",
                    Collections.singletonList(
                        new ArrayType(
                            new JavaClassType("String", new PackageName("java.lang")), 1)),
                    VoidType.getInstance()))
            .get().getBody().getLocals().stream()
            .noneMatch(local -> local.equals(newLocal)));

    // Please note that the jimple code of our newly modified method is not correct anymore, as we
    // deleted the two old locals which are used in the body.
  }
}
