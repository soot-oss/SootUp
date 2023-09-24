package sootup.tests;

import static org.junit.Assert.*;

import categories.Java8Test;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.*;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.*;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.MutableJavaView;

/**
 * Comprises test that test the addition and removal of classes and methods to the mutable view. It
 * uses the MiniApp.jar for testing.
 */
@Category(Java8Test.class)
public class MutableSootClientTest {
  static Path pathToJar = Paths.get("../shared-test-resources/java-miniapps/MiniApp.jar");
  static AnalysisInputLocation<JavaSootClass> location;
  static JavaProject p;
  MutableJavaView mv;

  /** Load the jar file for analysis as input location. */
  @BeforeClass
  public static void setupProject() {
    location = PathBasedAnalysisInputLocation.create(pathToJar, SourceType.Application);
    p = JavaProject.builder(new JavaLanguage(8)).addInputLocation(location).build();
  }

  /** Create a new mutable view that the tests should be performed on. */
  @Before
  public void setupMutableView() {
    mv = p.createMutableView();
  }

  /**
   * Remove a class from the mutable view and check whether the amount of classes in the view is
   * reduced by one.
   */
  @Test
  public void classRemovalTest() {
    int classesBeforeSize = mv.getClasses().size();
    ClassType classType = p.getIdentifierFactory().getClassType("utils.Operations");
    mv.removeClass(classType);
    int classesAfterSize = mv.getClasses().size();

    assertTrue(classesBeforeSize == classesAfterSize + 1);
  }

  /**
   * Add a class to the mutable view and check whether the amount of classes in the view is
   * increased by one.
   */
  @Test
  public void classAdditionTest() {
    ClassType addedClassType = p.getIdentifierFactory().getClassType("AddedClass");
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

    int classesBeforeSize = mv.getClasses().size();
    mv.addClass(newClass.buildClass(SourceType.Application));
    int classesAfterSize = mv.getClasses().size();

    assertTrue(classesBeforeSize == classesAfterSize - 1);
  }

  /**
   * Remove a method from a class within the mutable view and check whether the class in the view
   * does not contain the specified method anymore.
   */
  @Test
  public void methodRemovalTest() {
    ClassType classType = p.getIdentifierFactory().getClassType("utils.Operations");
    Optional<JavaSootClass> utilsClassOpt = mv.getClass(classType);
    assertTrue(utilsClassOpt.isPresent());

    SootClass<JavaSootClassSource> utilsClass = utilsClassOpt.get();
    MethodSignature ms =
        p.getIdentifierFactory()
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
    SootClass<JavaSootClassSource> updatedUtilsClass = updatedUtilsClassOpt.get();
    assertFalse(updatedUtilsClass.getMethods().contains(removeDepartmentMethod));
  }

  /**
   * Add a method to a class within the mutable view and check whether the class in the view does
   * contain the specified method afterwards.
   */
  @Test
  public void methodAdditionTest() {
    MethodSignature methodSignature =
        p.getIdentifierFactory()
            .getMethodSignature("addedMethod", "utils.Operations", "void", Collections.emptyList());
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

    ClassType classType = p.getIdentifierFactory().getClassType("utils.Operations");
    Optional<JavaSootClass> utilsClassOpt = mv.getClass(classType);
    assertTrue(utilsClassOpt.isPresent());

    SootClass<JavaSootClassSource> utilsClass = utilsClassOpt.get();
    assertFalse(utilsClass.getMethods().contains(newMethod));
    mv.addMethod(newMethod);

    // Need to get a new reference to the class, as the old one now points to a class that is no
    // longer in the view
    Optional<JavaSootClass> updatedUtilsClassOpt = mv.getClass(classType);
    assertTrue(updatedUtilsClassOpt.isPresent());
    SootClass<JavaSootClassSource> updatedUtilsClass = updatedUtilsClassOpt.get();
    assertTrue(updatedUtilsClass.getMethods().contains(newMethod));
  }
}
