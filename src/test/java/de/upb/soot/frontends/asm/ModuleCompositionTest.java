package de.upb.soot.frontends.asm;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.soot.Project;
import de.upb.soot.core.Body;
import de.upb.soot.core.ClassType;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.IMethodSourceContent;
import de.upb.soot.frontends.JavaClassSource;
import de.upb.soot.namespaces.JavaClassPathNamespace;
import de.upb.soot.namespaces.JavaSourcePathNamespace;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.FieldSubSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.signatures.MethodSubSignature;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.views.IView;
import java.io.File;
import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Defines tests for module composition.
 *
 * @author Jan Martin Persch
 */
@Category(Java8Test.class)
public class ModuleCompositionTest {

  @Test
  public void apiExamples() {
    // TODO. add assertions
    // System.out.println("--- EXAMPLE 1: On-Demand Loading ---");
    // System.out.println();

    String jarFile = "target/test-classes/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar";

    assertTrue(new File(jarFile).exists());

    // Create a project
    Project p = new Project(new JavaClassPathNamespace(jarFile));

    // Get the view
    IView view = p.createOnDemandView();

    // Create java class signature
    JavaClassType utilsClassSignature = p.getTypeFactory().getClassType("de.upb.soot.Utils");

    // Resolve signature to `SootClass`
    SootClass utilsClass =
        utilsClassSignature.resolve(view).orElseThrow(IllegalStateException::new);

    // Print all methods that are loaded on-demand
    // System.out.println("Methods of " + utilsClassSignature + " class:");
    utilsClass.getMethods().stream().map(it -> " - " + it).forEach(System.out::println);

    // System.out.println();

    // Parse sub-signature for "optionalToStream" method
    MethodSubSignature optionalToStreamMethodSubSignature =
        DefaultSignatureFactory.getInstance()
            .parseMethodSubSignature(
                "java.util.stream.Stream iteratorToStream(java.util.Iterator)");

    // Print sub-signature
    // System.out.println("Method to find: " + optionalToStreamMethodSubSignature);

    // Get method for sub-signature
    SootMethod foundMethod =
        utilsClass
            .getMethod(optionalToStreamMethodSubSignature)
            .orElseThrow(IllegalStateException::new);

    // Print method
    // System.out.println("Found method:   " + foundMethod);
    // System.out.println();

    // Print method content
    // System.out.println("Method body:   " + foundMethod);
    // System.out.println(foundMethod.getActiveBody());

    // System.out.println();
    // System.out.println("--- EXAMPLE 2: Using Builders ---");
    // System.out.println();

    // Parse sub-signature for "name" field
    FieldSubSignature nameFieldSubSignature =
        DefaultSignatureFactory.getInstance().parseFieldSubSignature("java.lang.String name");

    // Create the class signature
    JavaClassType classSignature = view.getTypeFactory().getClassType("x.y.z.foo.Bar");

    // Build a soot class
    SootClass c =
        SootClass.builder()
            .withResolvingLevel(ResolvingLevel.BODIES)
            .withClassSource(
                new JavaClassSource(
                    new JavaSourcePathNamespace(Collections.emptySet()), null, classSignature))
            .withClassType(ClassType.Application)
            .withModifiers(Modifier.PUBLIC)
            .withFields(
                SootField.builder()
                    .withSignature(nameFieldSubSignature.toFullSignature(classSignature))
                    .withModifiers(Modifier.PUBLIC)
                    .build())
            .withMethods(
                SootMethod.builder()
                    .withSource(
                        new IMethodSourceContent() {
                          @Override
                          @Nullable
                          public Body resolveBody(@Nonnull SootMethod m) {
                            return null;
                          }

                          @Override
                          @Nonnull
                          public MethodSignature getSignature() {
                            return DefaultSignatureFactory.getInstance()
                                .getMethodSignature(utilsClass, optionalToStreamMethodSubSignature);
                          }
                        })
                    .withSignature(
                        optionalToStreamMethodSubSignature.toFullSignature(classSignature))
                    .withModifiers(Modifier.PUBLIC)
                    .build())
            .build();

    // Print some information
    // System.out.println("Field sub-signature: " + nameFieldSubSignature);
    // System.out.println("Class signature:     " + c);
    // System.out.println();
    // System.out.println("Field:         " + c.getField(nameFieldSubSignature));
    // System.out.println("Field by name: " + c.getField(nameFieldSubSignature.getName()));
    // System.out.println("Method:        " + c.getMethod(optionalToStreamMethodSubSignature));
  }
}
