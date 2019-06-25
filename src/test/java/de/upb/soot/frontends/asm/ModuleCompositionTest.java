package de.upb.soot.frontends.asm;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.soot.DefaultIdentifierFactory;
import de.upb.soot.Project;
import de.upb.soot.core.Body;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.core.SourceType;
import de.upb.soot.frontends.MethodSource;
import de.upb.soot.frontends.java.EagerJavaClassSource;
import de.upb.soot.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.soot.inputlocation.JavaSourcePathAnalysisInputLocation;
import de.upb.soot.signatures.FieldSubSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.signatures.MethodSubSignature;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.util.Utils;
import de.upb.soot.views.View;
import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.junit.Assert;
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
    Project<JavaClassPathAnalysisInputLocation> p =
        new Project<>(new JavaClassPathAnalysisInputLocation(jarFile));

    // Get the view
    View view = p.createOnDemandView();

    // Create java class signature
    JavaClassType utilsClassSignature = p.getIdentifierFactory().getClassType("de.upb.soot.Utils");

    // Resolve signature to `SootClass`
    SootClass utilsClass =
        utilsClassSignature.resolve(view).orElseThrow(IllegalStateException::new);

    // Print all methods that are loaded on-demand
    // System.out.println("Methods of " + utilsClassSignature + " class:");
    // utilsClass.getMethods().stream().map(it -> " - " + it).forEach(System.out::println);

    // System.out.println();

    // Parse sub-signature for "optionalToStream" method
    MethodSubSignature optionalToStreamMethodSubSignature =
        DefaultIdentifierFactory.getInstance()
            .parseMethodSubSignature(
                "java.util.stream.Stream optionalToStream(java.util.Optional)");

    // Print sub-signature
    // System.out.println("Method to find: " + optionalToStreamMethodSubSignature);

    // Get method for sub-signature
    SootMethod foundMethod =
        utilsClass
            .getMethod(optionalToStreamMethodSubSignature)
            .orElseThrow(IllegalStateException::new);
    Assert.assertNotNull(foundMethod.getBody());

    // Print method
    // System.out.println("Found method:   " + foundMethod);
    // System.out.println();

    // Print method content
    // System.out.println("Method body:    ---Yay, InvokeDynamic is loading, now!---");
    // System.out.println(foundMethod.getBody());

    // System.out.println();
    // System.out.println("--- EXAMPLE 2: Using Builders ---");
    // System.out.println();

    // Parse sub-signature for "name" field
    FieldSubSignature nameFieldSubSignature =
        DefaultIdentifierFactory.getInstance().parseFieldSubSignature("java.lang.String name");

    // Create the class signature
    JavaClassType classSignature = view.getIdentifierFactory().getClassType("x.y.z.foo.Bar");

    // Build a soot class

    SootClass c =
        new SootClass(
            new EagerJavaClassSource(
                new JavaSourcePathAnalysisInputLocation(Collections.emptySet()),
                null,
                classSignature,
                null,
                null,
                null,
                Utils.immutableSet(
                    SootField.builder()
                        .withSignature(nameFieldSubSignature.toFullSignature(classSignature))
                        .withModifiers(Modifier.PUBLIC)
                        .build()),
                Utils.immutableSet(
                    SootMethod.builder()
                        .withSource(
                            new MethodSource() {
                              @Override
                              @Nullable
                              public Body resolveBody() {
                                return null;
                              }

                              @Override
                              @Nonnull
                              public MethodSignature getSignature() {
                                return DefaultIdentifierFactory.getInstance()
                                    .getMethodSignature(
                                        utilsClass, optionalToStreamMethodSubSignature);
                              }
                            })
                        .withSignature(
                            optionalToStreamMethodSubSignature.toFullSignature(classSignature))
                        .withModifiers(Modifier.PUBLIC)
                        .build()),
                null,
                EnumSet.of(Modifier.PUBLIC)),
            SourceType.Application);

    // Print some information
    // System.out.println("Field sub-signature: " + nameFieldSubSignature);
    // System.out.println("Class signature:     " + c);
    // System.out.println();
    // System.out.println("Field:         " + c.getField(nameFieldSubSignature));
    // System.out.println("Field by name: " + c.getField(nameFieldSubSignature.getName()));
    // System.out.println("Method:        " + c.getMethod(optionalToStreamMethodSubSignature));
  }
}
