package sootup.java.bytecode.inputlocation;

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

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import javax.annotation.Nonnull;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.frontend.BodySource;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.model.*;
import sootup.core.signatures.FieldSubSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ClassType;
import sootup.core.util.ImmutableUtils;
import sootup.core.views.View;
import sootup.java.core.*;
import sootup.java.core.views.JavaView;

/**
 * @author Manuel Benz created on 06.06.18
 * @author Kaustubh Kelkar updated on 16.04.2020
 */
@Category(Java8Test.class)
public class PathBasedAnalysisInputLocationTest extends AnalysisInputLocationTest {

  @Test
  public void testSingleClass() {
    PathBasedAnalysisInputLocation pathBasedNamespace =
        PathBasedAnalysisInputLocation.create(cls, null);
    ArrayList<ClassType> sigs = new ArrayList<>();
    sigs.add(getIdentifierFactory().getClassType("Employee"));
    testClassReceival(pathBasedNamespace, sigs, 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSingleClassDoesNotExist() {
    PathBasedAnalysisInputLocation pathBasedNamespace =
        PathBasedAnalysisInputLocation.create(Paths.get("NonExisting.class"), null);
  }

  @Test
  public void testJar() {
    PathBasedAnalysisInputLocation pathBasedNamespace =
        PathBasedAnalysisInputLocation.create(jar, null);
    ArrayList<ClassType> sigs = new ArrayList<>();
    sigs.add(getIdentifierFactory().getClassType("Employee", "ds"));
    sigs.add(getIdentifierFactory().getClassType("MiniApp"));
    testClassReceival(pathBasedNamespace, sigs, 6);
  }

  @Test
  public void testWar() {
    PathBasedAnalysisInputLocation pathBasedNamespace =
        PathBasedAnalysisInputLocation.create(war, null);
    final ClassType warClass1 = getIdentifierFactory().getClassType("SimpleWarRead");
    testClassReceival(pathBasedNamespace, Collections.singletonList(warClass1), 19);
  }

  @Test
  public void testClassInWar() {

    String warFile = "../shared-test-resources/java-warApp/dummyWarApp.war";

    assertTrue("File " + warFile + " not found.", new File(warFile).exists());

    // Get the view
    JavaView view = new JavaView(new JavaClassPathAnalysisInputLocation(warFile));

    assertEquals(19, view.getClasses().size());

    // Create java class signature
    ClassType utilsClassSignature = view.getIdentifierFactory().getClassType("Employee", "ds");

    // Resolve signature to `SootClass`
    SootClass<JavaSootClassSource> utilsClass = view.getClass(utilsClassSignature).get();

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
        foundMethod.getParameterTypes().stream().anyMatch(type -> "int".equals(type.toString())));

    // Parse sub-signature for "empName" field
    FieldSubSignature nameFieldSubSignature =
        JavaIdentifierFactory.getInstance().parseFieldSubSignature("java.lang.String empName");

    // Create the class signature
    ClassType classSignature = view.getIdentifierFactory().getClassType("Employee", "ds");

    // Build a soot class
    SootClass<?> c =
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
                        .withModifiers(FieldModifier.PUBLIC)
                        .build()),
                ImmutableUtils.immutableSet(
                    SootMethod.builder()
                        .withSource(
                            new BodySource() {
                              @Nonnull
                              @Override
                              public Body resolveBody(@Nonnull Iterable<MethodModifier> modifiers) {
                                /* [ms] violating @Nonnull */
                                return null;
                              }

                              @Override
                              public Object resolveAnnotationsDefaultValue() {
                                return null;
                              }

                              @Override
                              @Nonnull
                              public MethodSignature getSignature() {
                                return JavaIdentifierFactory.getInstance()
                                    .getMethodSignature(
                                        utilsClass.getType(), optionalToStreamMethodSubSignature);
                              }
                            })
                        .withSignature(
                            JavaIdentifierFactory.getInstance()
                                .getMethodSignature(
                                    classSignature, optionalToStreamMethodSubSignature))
                        .withModifiers(MethodModifier.PUBLIC)
                        .build()),
                null,
                EnumSet.of(ClassModifier.PUBLIC),
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
    PathBasedAnalysisInputLocation pathBasedNamespace = new DefaultRTJarAnalysisInputLocation();

    JavaView v = new JavaView(pathBasedNamespace);

    // test some standard jre classes
    runtimeContains(v, "Object", "java.lang");
    runtimeContains(v, "List", "java.util");
    runtimeContains(v, "Map", "java.util");
    runtimeContains(v, "ArrayList", "java.util");
    runtimeContains(v, "HashMap", "java.util");
    runtimeContains(v, "Collection", "java.util");
    runtimeContains(v, "Comparator", "java.util");
  }

  /**
   * Test for JavaClassPathAnalysisInputLocation. Specifying jar file with source type as Library.
   * Expected - All input classes are of source type Library.
   */
  @Test
  public void testInputLocationLibraryMode() {
    JavaView view =
        new JavaView(
            new JavaClassPathAnalysisInputLocation(new DefaultRTJarAnalysisInputLocation());

    Collection<SootClass<JavaSootClassSource>> classes =
        new HashSet<>(); // Set to track the classes to check

    for (SootClass<JavaSootClassSource> aClass : view.getClasses()) {
      if (!aClass.isLibraryClass()) {
        classes.add(aClass);
      }
    }

    assertEquals("User Defined class found, expected none", 0, classes.size());
  }
}
