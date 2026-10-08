package sootup.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.ClassModifier;
import sootup.core.model.FullPosition;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.InMemoryJavaClassSource;
import sootup.java.core.JavaSootClass;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.MutableJavaView;

/**
 * Checks that the type hierarchy of a {@link MutableJavaView} reflects classes that are added,
 * removed or replaced after the hierarchy has been built.
 */
public class MutableViewTypeHierarchyTest {

  private static final Path JAR = Paths.get("src/test/resources/java-miniapps/MiniApp.jar");

  private MutableJavaView view;
  private AnalysisInputLocation location;
  private JavaClassType abstractDs;
  private JavaClassType department;
  private JavaClassType employee;

  @BeforeEach
  public void setup() {
    location = PathBasedAnalysisInputLocation.create(JAR, SourceType.Application);
    view = new MutableJavaView(location);
    abstractDs = view.getIdentifierFactory().getClassType("ds.AbstractDataStrcture");
    department = view.getIdentifierFactory().getClassType("ds.Department");
    employee = view.getIdentifierFactory().getClassType("ds.Employee");
  }

  private Set<ClassType> subtypesOfAbstractDs() {
    return view.getTypeHierarchy().subtypesOf(abstractDs).collect(Collectors.toSet());
  }

  private JavaSootClass newSubclass(String name, JavaClassType superClass) {
    return new InMemoryJavaClassSource(
            location,
            JAR,
            view.getIdentifierFactory().getClassType(name),
            superClass,
            Collections.emptySet(),
            null,
            Collections.emptySet(),
            Collections.emptySet(),
            new FullPosition(0, 0, 0, 0),
            EnumSet.noneOf(ClassModifier.class),
            Collections.emptySet(),
            Collections.emptySet(),
            Collections.emptySet())
        .buildClass(SourceType.Application);
  }

  @Test
  public void addedClassIsVisibleInAnExistingHierarchy() {
    assertEquals(new HashSet<>(Arrays.asList(department, employee)), subtypesOfAbstractDs());

    JavaSootClass manager = newSubclass("ds.Manager", abstractDs);
    view.addClass(manager);

    assertEquals(
        new HashSet<>(Arrays.asList(department, employee, manager.getType())),
        subtypesOfAbstractDs());
    assertTrue(view.getTypeHierarchy().isSubtype(abstractDs, manager.getType()));
  }

  @Test
  public void removedClassDisappearsFromAnExistingHierarchy() {
    assertEquals(new HashSet<>(Arrays.asList(department, employee)), subtypesOfAbstractDs());

    view.removeClass(employee);

    assertEquals(Collections.singleton(department), subtypesOfAbstractDs());
  }

  @Test
  public void replacedClassIsReturnedBySubtypeClassesOf() {
    TypeHierarchy hierarchy = view.getTypeHierarchy();
    // fills the cache of subtype classes
    assertEquals(2, hierarchy.subtypeClassesOf(abstractDs).count());

    JavaSootClass oldDepartment = view.getClass(department).get();
    JavaSootClass newDepartment = oldDepartment.withMethods(Collections.emptySet());
    view.replaceClass(oldDepartment, newDepartment);

    SootClass returned =
        view.getTypeHierarchy()
            .subtypeClassesOf(abstractDs)
            .filter(c -> c.getType().equals(department))
            .findFirst()
            .get();
    assertSame(newDepartment, returned);
  }

  @Test
  public void replacedClassWithNewSuperclassMovesInTheHierarchy() {
    assertTrue(view.getTypeHierarchy().isSubtype(abstractDs, employee));

    JavaSootClass oldEmployee = view.getClass(employee).get();
    view.replaceClass(oldEmployee, oldEmployee.withSuperclass(Optional.empty()));

    assertFalse(view.getTypeHierarchy().isSubtype(abstractDs, employee));
    assertEquals(Collections.singleton(department), subtypesOfAbstractDs());
  }
}
