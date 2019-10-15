package de.upb.swt.soot.test.java.bytecode.frontend;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.DefaultIdentifierFactory;
import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.frontend.EagerJavaClassSource;
import de.upb.swt.soot.core.frontend.MethodSource;
import de.upb.swt.soot.core.inputlocation.DefaultSourceTypeSpecifier;
import de.upb.swt.soot.core.inputlocation.EagerInputLocation;
import de.upb.swt.soot.core.model.*;
import de.upb.swt.soot.core.signatures.FieldSubSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.signatures.MethodSubSignature;
import de.upb.swt.soot.core.types.JavaClassType;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.bytecode.frontend.AsmJavaClassProvider;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import java.io.File;
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

    String jarFile = "../shared-test-resources/Soot-4.0-SNAPSHOT.jar";
    System.err.println(new File(jarFile).getAbsolutePath());
    assertTrue("File " + jarFile + " not found.", new File(jarFile).exists());

    // Create a project
    Project<JavaClassPathAnalysisInputLocation> p =
        new Project<>(
            new JavaClassPathAnalysisInputLocation(
                jarFile, DefaultSourceTypeSpecifier.getInstance(), new AsmJavaClassProvider()));

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
                new EagerInputLocation(DefaultSourceTypeSpecifier.getInstance()),
                null,
                classSignature,
                null,
                null,
                null,
                ImmutableUtils.immutableSet(
                    SootField.builder()
                        .withSignature(nameFieldSubSignature.toFullSignature(classSignature))
                        .withModifiers(Modifier.PUBLIC)
                        .build()),
                ImmutableUtils.immutableSet(
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
