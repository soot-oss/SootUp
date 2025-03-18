package sootup.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.*;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.OverridingJavaClassSource;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.MutableJavaView;

/**
 * Comprises test that test the addition and removal of classes and methods to the mutable view. It
 * uses the MiniApp.jar for testing.
 */
public class MutableSootClientTest {
  static Path pathToJar = Paths.get("../shared-test-resources/java-miniapps/MiniApp.jar");
  static AnalysisInputLocation location;
  MutableJavaView mv;

  /** Load the jar file for analysis as input location. */
  @BeforeAll
  public static void setupProject() {
    location = PathBasedAnalysisInputLocation.create(pathToJar, SourceType.Application);
  }

  /** Create a new mutable view that the tests should be performed on. */
  @BeforeEach
  public void setupMutableView() {
    mv =
        new MutableJavaView(
            PathBasedAnalysisInputLocation.create(pathToJar, SourceType.Application));
  }

  /**
   * Remove a class from the mutable view and check whether the amount of classes in the view is
   * reduced by one.
   */
  @Test
  public void classRemovalTest() {
    long classesBeforeSize = mv.getClasses().count();
    ClassType classType = mv.getIdentifierFactory().getClassType("utils.Operations");
    mv.removeClass(classType);
    long classesAfterSize = mv.getClasses().count();

    assertEquals(classesBeforeSize, classesAfterSize + 1);
  }

  /**
   * Add a class to the mutable view and check whether the amount of classes in the view is
   * increased by one.
   */
  @Test
  public void classAdditionTest() {
    JavaClassType addedClassType = mv.getIdentifierFactory().getClassType("AddedClass");
    OverridingJavaClassSource newClass =
        new OverridingJavaClassSource(
            location,
            pathToJar,
            addedClassType,
            null,
            Collections.emptySet(),
            null,
            Collections.emptySet(),
            Collections.emptySet(),
            new FullPosition(0, 0, 0, 0),
            EnumSet.noneOf(ClassModifier.class),
            Collections.emptySet(),
            Collections.emptySet(),
            Collections.emptySet());

    long classesBeforeSize = mv.getClasses().count();
    mv.addClass(newClass.buildClass(SourceType.Application));
    long classesAfterSize = mv.getClasses().count();

    assertEquals(classesBeforeSize, classesAfterSize - 1);
  }

  /**
   * Remove a method from a class within the mutable view and check whether the class in the view
   * does not contain the specified method anymore.
   */
  @Test
  public void methodRemovalTest() {
    ClassType classType = mv.getIdentifierFactory().getClassType("utils.Operations");
    Optional<JavaSootClass> utilsClassOpt = mv.getClass(classType);
    assertTrue(utilsClassOpt.isPresent());

    SootClass utilsClass = utilsClassOpt.get();
    MethodSignature ms =
        mv.getIdentifierFactory()
            .parseMethodSignature("<utils.Operations: void removeDepartment(ds.Department)>");
    Optional<? extends SootMethod> removeDepartmentMethodOpt =
        utilsClass.getMethod(ms.getSubSignature());
    assertTrue(removeDepartmentMethodOpt.isPresent());

    SootMethod removeDepartmentMethod = removeDepartmentMethodOpt.get();
    assertTrue(utilsClass.getMethods().contains(removeDepartmentMethod));
    mv.removeMethod((JavaSootMethod) removeDepartmentMethod);

    // Need to get a new reference to the class, as the old one now points to a class that is no
    // longer in the view
    Optional<JavaSootClass> updatedUtilsClassOpt = mv.getClass(classType);
    assertTrue(updatedUtilsClassOpt.isPresent());
    SootClass updatedUtilsClass = updatedUtilsClassOpt.get();
    assertFalse(updatedUtilsClass.getMethods().contains(removeDepartmentMethod));
  }

  /**
   * Add a method to a class within the mutable view and check whether the class in the view does
   * contain the specified method afterwards.
   */
  @Test
  public void methodAdditionTest() {
    MethodSignature methodSignature =
        mv.getIdentifierFactory()
            .getMethodSignature("utils.Operations", "addedMethod", "void", Collections.emptyList());
    Body.BodyBuilder bodyBuilder = Body.builder();
    Body body = bodyBuilder.setMethodSignature(methodSignature).build();
    JavaSootMethod newMethod =
        new JavaSootMethod(
            new OverridingBodySource(methodSignature, body),
            methodSignature,
            EnumSet.of(MethodModifier.PUBLIC, MethodModifier.STATIC),
            Collections.emptyList(),
            Collections.emptyList(),
            NoPositionInformation.getInstance());

    ClassType classType = mv.getIdentifierFactory().getClassType("utils.Operations");
    Optional<JavaSootClass> utilsClassOpt = mv.getClass(classType);
    assertTrue(utilsClassOpt.isPresent());

    SootClass utilsClass = utilsClassOpt.get();
    assertFalse(utilsClass.getMethods().contains(newMethod));
    mv.addMethod(newMethod);

    // Need to get a new reference to the class, as the old one now points to a class that is no
    // longer in the view
    Optional<JavaSootClass> updatedUtilsClassOpt = mv.getClass(classType);
    assertTrue(updatedUtilsClassOpt.isPresent());
    SootClass updatedUtilsClass = updatedUtilsClassOpt.get();
    assertTrue(updatedUtilsClass.getMethods().contains(newMethod));
  }
}
