package de.upb.swt.soot.test.java.bytecode.frontend;

import categories.Java8Test;
import de.upb.swt.soot.core.frontend.MethodSource;
import de.upb.swt.soot.core.inputlocation.EagerInputLocation;
import de.upb.swt.soot.core.model.*;
import de.upb.swt.soot.core.signatures.FieldSubSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.signatures.MethodSubSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.swt.soot.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.OverridingJavaClassSource;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.views.JavaView;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.annotation.Nonnull;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Defines tests for module composition.
 *
 * @author Jan Martin Persch
 * @author Kaustubh Kelkar updated on 09.07.2020
 */
@Category(Java8Test.class)
public class ModuleCompositionTest {

  @Test
  public void apiExamples() {

    String warFile = "../shared-test-resources/java-warApp/dummyWarApp.war";

    Assert.assertTrue("File " + warFile + " not found.", new File(warFile).exists());
    Path jarPath;
    if (!new File(warFile).exists()) {
      jarPath = Paths.get("../shared-test-resources/java-miniapps/MiniApp.jar");
    } else {
      jarPath = Paths.get("../shared-test-resources/java-warApp/dummyWarApp.war");
    }
    PathBasedAnalysisInputLocation pathBasedAnalysisInputLocation =
        PathBasedAnalysisInputLocation.createForClassContainer(jarPath);

    List<Path> jarsFromPath =
        pathBasedAnalysisInputLocation
            .getJarsFromPath(); // TODO handle this part in JavaClassPathAnalysisInputLocation
    for (Path path : jarsFromPath) {
      if (path.getFileName().toString().contains("MiniApp")) {
        jarPath = path;
      }
    }

    // Create a project
    JavaProject p =
        JavaProject.builder(new JavaLanguage(8))
            .addClassPath(new JavaClassPathAnalysisInputLocation(jarPath.toString()))
            .build();

    // Get the view
    JavaView view = p.createOnDemandView();

    // Create java class signature
    ClassType utilsClassSignature = p.getIdentifierFactory().getClassType("Employee", "ds");

    // Resolve signature to `SootClass`
    SootClass utilsClass = view.getClass(utilsClassSignature).get();

    // Parse sub-signature for "setEmpSalary" method
    MethodSubSignature optionalToStreamMethodSubSignature =
        JavaIdentifierFactory.getInstance().parseMethodSubSignature("void setEmpSalary(int)");

    // Get method for sub-signature
    SootMethod foundMethod = utilsClass.getMethod(optionalToStreamMethodSubSignature).get();
    Assert.assertNotNull(foundMethod.getBody());

    // Print method
    Assert.assertTrue("setEmpSalary".equalsIgnoreCase(foundMethod.getName()));
    Assert.assertEquals("void", foundMethod.getReturnTypeSignature().toString());
    Assert.assertEquals(1, foundMethod.getParameterCount());
    Assert.assertTrue(
        foundMethod.getParameterTypes().stream()
            .anyMatch(
                type -> {
                  return "int".equals(type.toString());
                }));

    // Parse sub-signature for "empName" field
    FieldSubSignature nameFieldSubSignature =
        JavaIdentifierFactory.getInstance().parseFieldSubSignature("java.lang.String empName");

    // Create the class signature
    ClassType classSignature = view.getIdentifierFactory().getClassType("Employee", "ds");

    // Build a soot class
    SootClass c =
        new SootClass(
            new OverridingJavaClassSource(
                new EagerInputLocation(),
                null,
                classSignature,
                null,
                null,
                null,
                Collections.singleton(
                    SootField.builder()
                        .withSignature(
                            JavaIdentifierFactory.getInstance()
                                .getFieldSignature(classSignature, nameFieldSubSignature))
                        .withModifiers(Modifier.PUBLIC)
                        .build()),
                ImmutableUtils.immutableSet(
                    SootMethod.builder()
                        .withSource(
                            new MethodSource() {
                              @Override
                              public Body resolveBody() {
                                /* [ms] violating @Nonnull */
                                return null;
                              }

                              @Override
                              @Nonnull
                              public MethodSignature getSignature() {
                                return JavaIdentifierFactory.getInstance()
                                    .getMethodSignature(
                                        utilsClass, optionalToStreamMethodSubSignature);
                              }
                            })
                        .withSignature(
                            JavaIdentifierFactory.getInstance()
                                .getMethodSignature(
                                    classSignature, optionalToStreamMethodSubSignature))
                        .withModifiers(Modifier.PUBLIC)
                        .build()),
                null,
                EnumSet.of(Modifier.PUBLIC),
                Collections.emptyList()),
            SourceType.Application);

    Assert.assertEquals(
        "java.lang.String", c.getField(nameFieldSubSignature).get().getType().toString());
    Assert.assertEquals("empName", c.getField(nameFieldSubSignature).get().getName());
  }
  // }
}
