package sootup.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.InMemoryJavaClassSource;
import sootup.java.core.JavaSootClass;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;
import sootup.java.core.views.MutableJavaView;

public class MutableViewOverBaseTest {

  private static final Path JAR = Paths.get("src/test/resources/java-miniapps/MiniApp.jar");

  private JavaView base;
  private AnalysisInputLocation location;
  private JavaClassType abstractDs;
  private JavaClassType department;
  private JavaClassType employee;

  @BeforeEach
  public void setup() {
    location = PathBasedAnalysisInputLocation.create(JAR, SourceType.Application);
    base = new JavaView(location);
    abstractDs = base.getIdentifierFactory().getClassType("ds.AbstractDataStrcture");
    department = base.getIdentifierFactory().getClassType("ds.Department");
    employee = base.getIdentifierFactory().getClassType("ds.Employee");
  }

  private Set<ClassType> types(MutableJavaView view) {
    return view.getClasses().map(SootClass::getType).collect(Collectors.toSet());
  }

  private Set<ClassType> subtypesOfAbstractDs(MutableJavaView view) {
    return view.getTypeHierarchy().subtypesOf(abstractDs).collect(Collectors.toSet());
  }

  private JavaSootClass newSubclass(String name, JavaClassType superClass) {
    return new JavaSootClass(
        InMemoryJavaClassSource.builder()
            .withAnalysisInputLocation(location)
            .withClassType(base.getIdentifierFactory().getClassType(name))
            .withSourcePath(JAR)
            .withSuperclass(superClass)
            .build(),
        SourceType.Application);
  }

  @Test
  public void startsWithTheSharedClassesOfTheBase() {
    MutableJavaView view = new MutableJavaView(base);

    Set<ClassType> baseTypes =
        base.getClasses().map(SootClass::getType).collect(Collectors.toSet());
    assertFalse(baseTypes.isEmpty());
    assertEquals(baseTypes, types(view));
    // the class objects are shared, not loaded again
    assertSame(base.getClass(employee).get(), view.getClass(employee).get());
    assertEquals(new HashSet<>(Arrays.asList(department, employee)), subtypesOfAbstractDs(view));
  }

  @Test
  public void changesStayInTheMutableView() {
    MutableJavaView view = new MutableJavaView(base);
    Set<ClassType> baseTypes =
        base.getClasses().map(SootClass::getType).collect(Collectors.toSet());

    JavaSootClass manager = newSubclass("ds.Manager", abstractDs);
    view.addClass(manager);
    view.removeClass(employee);

    assertEquals(
        new HashSet<>(Arrays.asList(department, manager.getType())), subtypesOfAbstractDs(view));
    assertFalse(view.getClass(employee).isPresent());
    assertTrue(view.getClass(manager.getType()).isPresent());

    // the base view is unchanged
    assertEquals(baseTypes, base.getClasses().map(SootClass::getType).collect(Collectors.toSet()));
    assertTrue(base.getClass(employee).isPresent());
    assertFalse(base.getClass(manager.getType()).isPresent());
    assertEquals(
        new HashSet<>(Arrays.asList(department, employee)),
        base.getTypeHierarchy().subtypesOf(abstractDs).collect(Collectors.toSet()));
  }

  @Test
  public void mutableViewsOnOneBaseAreIndependent() {
    MutableJavaView first = new MutableJavaView(base);
    MutableJavaView second = new MutableJavaView(base);

    JavaSootClass employeeClass = first.getClass(employee).get();
    JavaSootClass replaced = employeeClass.withMethods(Collections.emptySet());
    first.replaceClass(employeeClass, replaced);

    assertSame(replaced, first.getClass(employee).get());
    assertSame(employeeClass, second.getClass(employee).get());
    assertEquals(types(first), types(second));
  }
}
