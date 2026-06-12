package sootup.examples.mutatingSootClass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.Collections;
import org.junit.jupiter.api.Disabled;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.Local;
import sootup.core.model.Body;
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
import sootup.java.core.*;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

/**
 * This example shows how to change a method inside a SootClass using OverridingBodySources and
 * OverridingClassSources.
 *
 * @author Bastian Haverkamp
 */
@Disabled
public class MutatingTest {

  @Disabled
  public void testMutation() {
    // Create a AnalysisInputLocation, which points to a directory. All class files will be loaded
    // from the directory
    AnalysisInputLocation inputLocation =
        PathBasedAnalysisInputLocation.create(
            Paths.get("src/test/resources/BasicSetup/binary"), SourceType.Application);

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

    System.out.println(oldBody);

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

    // Create new Method
    JavaSootMethod newMethod = method.withOverridingMethodSource(old -> newBodySource);

    OverridingJavaClassSource newClassSource =
        overridingJavaClassSource.withReplacedMethod(method, newMethod);
    SootClass newClass = sootClass.withClassSource(newClassSource);

    newClass.getMethods().stream().findFirst().ifPresent(a -> System.out.println(a.getBody()));

    // assert that only our newly created local exists
    SootMethod main =
        newClass
            .getMethod(
                new MethodSubSignature(
                    "main",
                    Collections.singletonList(
                        new ArrayType(
                            new JavaClassType("String", new PackageName("java.lang")), 1)),
                    VoidType.getInstance()))
            .orElse(null);

    assertNotNull(main);
    assertEquals(newLocal, main.getBody().getLocals().stream().findFirst().orElse(null));

    // assert that old soot class remains unchanged
    SootMethod constructor =
        sootClass
            .getMethod(
                new MethodSubSignature("<init>", Collections.emptyList(), VoidType.getInstance()))
            .orElse(null);
    assertNotNull(constructor);
    assertFalse(constructor.getBody().getLocals().isEmpty());
    SootMethod olderMain =
        sootClass
            .getMethod(
                new MethodSubSignature(
                    "main",
                    Collections.singletonList(
                        new ArrayType(
                            new JavaClassType("String", new PackageName("java.lang")), 1)),
                    VoidType.getInstance()))
            .orElse(null);
    assertNotNull(olderMain);
    assertTrue(olderMain.getBody().getLocals().stream().noneMatch(local -> local.equals(newLocal)));

    // Please note that the jimple code of our newly modified method is not correct anymore, as we
    // deleted the two old locals which are used in the body.
  }
}
