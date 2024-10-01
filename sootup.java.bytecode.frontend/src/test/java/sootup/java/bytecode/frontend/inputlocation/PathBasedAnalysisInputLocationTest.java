package sootup.java.bytecode.frontend.inputlocation;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import categories.TestCategories;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.ClassModifier;
import sootup.core.model.FieldModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.signatures.FieldSubSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.core.*;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

/**
 * @author Manuel Benz created on 06.06.18
 * @author Kaustubh Kelkar updated on 16.04.2020
 */
@Tag(TestCategories.JAVA_8_CATEGORY)
public class PathBasedAnalysisInputLocationTest extends AnalysisInputLocationTest {

  @Test
  public void testSingleClass() {
    PathBasedAnalysisInputLocation pathBasedNamespace =
        new PathBasedAnalysisInputLocation.ClassFileBasedAnalysisInputLocation(
            cls, "", SourceType.Application);
    ArrayList<ClassType> sigs = new ArrayList<>();
    sigs.add(getIdentifierFactory().getClassType("Employee"));
    testClassReceival(pathBasedNamespace, sigs, 1);
  }

  @Test()
  public void testSingleClassDoesNotExist() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () ->
            PathBasedAnalysisInputLocation.create(
                Paths.get("NonExisting.class"), SourceType.Application));
  }

  @Test
  public void testSingleClassWPackageName() {
    AnalysisInputLocation pathBasedNamespace =
        new PathBasedAnalysisInputLocation.ClassFileBasedAnalysisInputLocation(
            Paths.get("../shared-test-resources/ClassWithPackageName.class"),
            "ClassesPackageName",
            SourceType.Application);
    ArrayList<ClassType> sigs = new ArrayList<>();
    sigs.add(getIdentifierFactory().getClassType("ClassesPackageName.ClassWithPackageName"));
    testClassReceival(pathBasedNamespace, sigs, 1);
  }

  @Test
  public void testJar() {
    PathBasedAnalysisInputLocation pathBasedNamespace =
        PathBasedAnalysisInputLocation.create(jar, SourceType.Application);
    ArrayList<ClassType> sigs = new ArrayList<>();
    sigs.add(getIdentifierFactory().getClassType("Employee", "ds"));
    sigs.add(getIdentifierFactory().getClassType("MiniApp"));
    testClassReceival(pathBasedNamespace, sigs, 6);
  }

  @Test
  public void testWar() {
    PathBasedAnalysisInputLocation pathBasedNamespace =
        PathBasedAnalysisInputLocation.create(war, SourceType.Application);
    final ClassType warClass1 = getIdentifierFactory().getClassType("SimpleWarRead");
    testClassReceival(pathBasedNamespace, Collections.singletonList(warClass1), 19);
  }

  @Test
  public void testClassInWar() {

    String warFile = "../shared-test-resources/java-warApp/dummyWarApp.war";

    assertTrue(new File(warFile).exists(), "File " + warFile + " not found.");

    // Get the view
    JavaView view = new JavaView(new JavaClassPathAnalysisInputLocation(warFile));

    assertEquals(19, view.getClasses().count());

    // Create java class signature
    ClassType utilsClassSignature = view.getIdentifierFactory().getClassType("Employee", "ds");

    // Resolve signature to `SootClass`
    JavaSootClass utilsClass = view.getClass(utilsClassSignature).get();

    // Parse sub-signature for "setEmpSalary" method
    MethodSubSignature optionalToStreamMethodSubSignature =
        JavaIdentifierFactory.getInstance().parseMethodSubSignature("void setEmpSalary(int)");

    // Get method for sub-signature
    JavaSootMethod foundMethod = utilsClass.getMethod(optionalToStreamMethodSubSignature).get();
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
    JavaClassType classSignature = view.getIdentifierFactory().getClassType("Employee", "ds");

    JavaSootField field =
        new JavaSootField(
            JavaIdentifierFactory.getInstance()
                .getFieldSignature(classSignature, nameFieldSubSignature),
            Collections.singleton(FieldModifier.PUBLIC),
            null,
            NoPositionInformation.getInstance());

    // Build a soot class
    JavaSootClass c =
        new JavaSootClass(
            new OverridingJavaClassSource(
                new EagerInputLocation(),
                null,
                classSignature,
                null,
                null,
                null,
                Collections.singleton(field),
                Collections.emptySet(),
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
    assertTrue(view.getClass(sig).isPresent(), sig + " is not found in rt.jar");
  }

  @Test
  public void testRuntimeJar() {
    AnalysisInputLocation pathBasedNamespace = new DefaultRuntimeAnalysisInputLocation();

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
    JavaView view = new JavaView(new DefaultRuntimeAnalysisInputLocation());

    Collection<SootClass> classes = new HashSet<>(); // Set to track the classes to check

    view.getClasses().filter(aClass -> !aClass.isLibraryClass()).forEach(classes::add);

    assertEquals(0, classes.size(), "User Defined class found, expected none");
  }
}
