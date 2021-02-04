package de.upb.swt.soot.test.java.bytecode.inputlocation;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 06.06.2018 Manuel Benz
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.BodySource;
import de.upb.swt.soot.core.inputlocation.EagerInputLocation;
import de.upb.swt.soot.core.model.*;
import de.upb.swt.soot.core.signatures.FieldSubSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.signatures.MethodSubSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.swt.soot.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.OverridingJavaClassSource;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.views.JavaView;
import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Manuel Benz created on 06.06.18
 * @author Kaustubh Kelkar updated on 16.04.2020
 */
@Category(Java8Test.class)
public class PathBasedAnalysisInputLocationTest extends AnalysisInputLocationTest {

  @Test
  public void testJar() {
    PathBasedAnalysisInputLocation pathBasedNamespace =
        PathBasedAnalysisInputLocation.createForClassContainer(jar);

    final ClassType class1 = getIdentifierFactory().getClassType("Employee", "ds");
    final ClassType mainClass = getIdentifierFactory().getClassType("MiniApp");
    testClassReceival(pathBasedNamespace, class1, 4);
    testClassReceival(pathBasedNamespace, mainClass, 4);
  }

  @Test
  public void testWar() {
    PathBasedAnalysisInputLocation pathBasedNamespace =
        PathBasedAnalysisInputLocation.createForClassContainer(war);
    final ClassType warClass1 = getIdentifierFactory().getClassType("SimpleWarRead");
    testClassReceival(pathBasedNamespace, warClass1, 2);
  }

  @Test
  public void testClassInWar() {

    String warFile = "../shared-test-resources/java-warApp/dummyWarApp.war";

    assertTrue("File " + warFile + " not found.", new File(warFile).exists());

    // Create a project
    JavaProject p =
        JavaProject.builder(new JavaLanguage(8))
            .addClassPath(new JavaClassPathAnalysisInputLocation(warFile))
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
    assertNotNull(foundMethod.getBody());

    // Print method
    assertTrue("setEmpSalary".equalsIgnoreCase(foundMethod.getName()));
    assertEquals("void", foundMethod.getReturnType().toString());
    assertEquals(1, foundMethod.getParameterCount());
    assertTrue(
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
                            new BodySource() {
                              @Override
                              public Body resolveBody(@Nonnull Iterable<Modifier> modifiers) {
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
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()),
            SourceType.Application);

    assertEquals("java.lang.String", c.getField(nameFieldSubSignature).get().getType().toString());
    assertEquals("empName", c.getField(nameFieldSubSignature).get().getName());
  }

  void runtimeContains(View view, String classname, String packageName) {
    final ClassType sig = getIdentifierFactory().getClassType(classname, packageName);
    assertTrue(sig + " is not found in rt.jar", view.getClass(sig).isPresent());
  }

  @Test
  public void testRuntimeJar() {
    PathBasedAnalysisInputLocation pathBasedNamespace =
        PathBasedAnalysisInputLocation.createForClassContainer(
            Paths.get(System.getProperty("java.home") + "/lib/rt.jar"));

    final Collection<? extends AbstractClassSource> classSources =
        pathBasedNamespace.getClassSources(getIdentifierFactory());

    JavaView v =
        JavaProject.builder(new JavaLanguage(8))
            .addClassPath(pathBasedNamespace)
            .build()
            .createOnDemandView();
    // test some standard jre classes
    runtimeContains(v, "Object", "java.lang");
    runtimeContains(v, "List", "java.util");
    runtimeContains(v, "Map", "java.util");
    runtimeContains(v, "ArrayList", "java.util");
    runtimeContains(v, "HashMap", "java.util");
    runtimeContains(v, "Collection", "java.util");
    runtimeContains(v, "Comparator", "java.util");
  }
}
