package de.upb.sse.sootup.examples.mutatingSootClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.upb.sse.sootup.core.frontend.OverridingBodySource;
import de.upb.sse.sootup.core.inputlocation.AnalysisInputLocation;
import de.upb.sse.sootup.core.jimple.basic.Local;
import de.upb.sse.sootup.core.model.Body;
import de.upb.sse.sootup.core.model.SootClass;
import de.upb.sse.sootup.core.model.SootMethod;
import de.upb.sse.sootup.core.signatures.MethodSignature;
import de.upb.sse.sootup.core.signatures.MethodSubSignature;
import de.upb.sse.sootup.core.signatures.PackageName;
import de.upb.sse.sootup.core.types.ArrayType;
import de.upb.sse.sootup.core.types.ClassType;
import de.upb.sse.sootup.core.types.PrimitiveType.IntType;
import de.upb.sse.sootup.core.types.VoidType;
import de.upb.sse.sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import de.upb.sse.sootup.java.core.JavaProject;
import de.upb.sse.sootup.java.core.JavaSootClass;
import de.upb.sse.sootup.java.core.JavaSootClassSource;
import de.upb.sse.sootup.java.core.OverridingJavaClassSource;
import de.upb.sse.sootup.java.core.language.JavaJimple;
import de.upb.sse.sootup.java.core.language.JavaLanguage;
import de.upb.sse.sootup.java.core.types.JavaClassType;
import de.upb.sse.sootup.java.core.views.JavaView;
import java.nio.file.Paths;
import java.util.Collections;
import org.junit.Test;

/**
 * This example shows how to change a method inside a SootClass using OverridingBodySources and
 * OverridingClassSources.
 *
 * @author Bastian Haverkamp
 */
public class MutatingSootClass {

  @Test
  public void test() {
    // Create a AnalysisInputLocation, which points to a directory. All class files will be loaded
    // from the directory
    AnalysisInputLocation<JavaSootClass> inputLocation =
        new PathBasedAnalysisInputLocation(Paths.get("src/test/resources/BasicSetup/binary"), null);

    // Specify the language of the JavaProject. This is especially relevant for Multi-release jars,
    // where classes are loaded depending on the language level of the analysis
    JavaLanguage language = new JavaLanguage(8);

    // Create a new JavaProject based on the input location
    JavaProject project = JavaProject.builder(language).addInputLocation(inputLocation).build();

    // Create a signature for the class we want to analyze
    ClassType classType = project.getIdentifierFactory().getClassType("HelloWorld");

    // Create a signature for the method we want to analyze
    MethodSignature methodSignature =
        project
            .getIdentifierFactory()
            .getMethodSignature(
                classType, "main", "void", Collections.singletonList("java.lang.String[]"));

    // Create a view for project, which allows us to retrieve classes
    JavaView view = project.createView();

    // Assert that class is present
    assertTrue(view.getClass(classType).isPresent());

    // Retrieve class
    SootClass<JavaSootClassSource> sootClass = view.getClass(classType).get();

    // Retrieve method
    assertTrue(view.getMethod(methodSignature).isPresent());
    SootMethod method = view.getMethod(methodSignature).get();
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
    SootMethod newMethod = method.withOverridingMethodSource(old -> newBodySource);

    OverridingJavaClassSource newClassSource =
        overridingJavaClassSource.withReplacedMethod(method, newMethod);
    SootClass<JavaSootClassSource> newClass = sootClass.withClassSource(newClassSource);

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