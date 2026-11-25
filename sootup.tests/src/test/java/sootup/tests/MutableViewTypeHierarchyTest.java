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
import sootup.core.typehierarchy.ViewTypeHierarchy;
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
    return newClass(name, superClass, Collections.emptySet(), EnumSet.noneOf(ClassModifier.class));
  }

  private JavaSootClass newClass(
      String name,
      JavaClassType superClass,
      Set<JavaClassType> interfaces,
      EnumSet<ClassModifier> modifiers) {
    return new InMemoryJavaClassSource(
            location,
            JAR,
            view.getIdentifierFactory().getClassType(name),
            superClass,
            interfaces,
            null,
            Collections.emptySet(),
            Collections.emptySet(),
            new FullPosition(0, 0, 0, 0),
            modifiers,
            Collections.emptySet(),
            Collections.emptySet(),
            Collections.emptySet())
        .buildClass(SourceType.Application);
  }

  /**
   * Checks that the hierarchy of the view, updated in place, answers like a hierarchy built from
   * scratch on the current classes of the view.
   */
  private void assertSameAsRebuilt(ClassType... types) {
    TypeHierarchy updated = view.getTypeHierarchy();
    TypeHierarchy rebuilt = new ViewTypeHierarchy(view);
    for (ClassType type : types) {
      assertEquals(
          rebuilt.subtypesOf(type).collect(Collectors.toSet()),
          updated.subtypesOf(type).collect(Collectors.toSet()),
          "subtypes of " + type);
      assertEquals(rebuilt.superClassOf(type), updated.superClassOf(type), "superclass of " + type);
    }
  }

  private ClassType object() {
    return view.getIdentifierFactory().getClassType("java.lang.Object");
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

    TypeHierarchy hierarchy = view.getTypeHierarchy();

    view.removeClass(employee);

    // updated in place, not discarded and rebuilt
    assertSame(hierarchy, view.getTypeHierarchy());
    assertEquals(Collections.singleton(department), subtypesOfAbstractDs());
    assertSameAsRebuilt(object(), abstractDs, department);
  }

  @Test
  public void everyChangeIncreasesTheModificationCount() {
    TypeHierarchy hierarchy = view.getTypeHierarchy();
    hierarchy.subtypesOf(abstractDs);

    long count = hierarchy.getModificationCount();
    view.addClass(newSubclass("ds.Manager", abstractDs));
    assertTrue(hierarchy.getModificationCount() > count, "add");

    count = hierarchy.getModificationCount();
    JavaSootClass oldDepartment = view.getClass(department).get();
    view.replaceClass(oldDepartment, oldDepartment.withMethods(Collections.emptySet()));
    assertTrue(hierarchy.getModificationCount() > count, "replace with the same supertypes");

    count = hierarchy.getModificationCount();
    JavaSootClass oldEmployee = view.getClass(employee).get();
    view.replaceClass(oldEmployee, oldEmployee.withSuperclass(Optional.empty()));
    assertTrue(hierarchy.getModificationCount() > count, "replace with other supertypes");

    count = hierarchy.getModificationCount();
    view.removeClass(department);
    assertTrue(hierarchy.getModificationCount() > count, "remove");
  }

  @Test
  public void cachedAnswersAreNotReturnedAfterARemoval() {
    TypeHierarchy hierarchy = view.getTypeHierarchy();
    // fill the caches of subtype classes and lowest common ancestors
    assertEquals(2, hierarchy.subtypeClassesOf(abstractDs).count());
    assertEquals(
        Collections.singleton(abstractDs),
        new HashSet<>(hierarchy.getLowestCommonAncestors(department, employee)));

    view.removeClass(employee);

    assertEquals(
        Collections.singletonList(department),
        hierarchy.subtypeClassesOf(abstractDs).map(SootClass::getType).toList());
    // an unknown type has only Object in common with any other type
    assertEquals(
        Collections.singleton(object()),
        new HashSet<>(hierarchy.getLowestCommonAncestors(department, employee)));
  }

  @Test
  public void removedSuperclassRemainsTheSupertypeOfItsSubclasses() {
    assertEquals(new HashSet<>(Arrays.asList(department, employee)), subtypesOfAbstractDs());

    view.removeClass(abstractDs);

    // like in a hierarchy built without it: its subclasses still name it as their superclass
    assertEquals(new HashSet<>(Arrays.asList(department, employee)), subtypesOfAbstractDs());
    assertEquals(Optional.of(abstractDs), view.getTypeHierarchy().superClassOf(department));
    assertFalse(view.getTypeHierarchy().subtypesOf(object()).anyMatch(abstractDs::equals));
    assertSameAsRebuilt(object(), abstractDs, department, employee);
  }

  @Test
  public void removedSuperclassLeavesWithItsLastSubclass() {
    view.getTypeHierarchy().subtypesOf(object());
    JavaSootClass base = newSubclass("ds.Base", (JavaClassType) object());
    JavaSootClass sub = newSubclass("ds.Sub", (JavaClassType) base.getType());
    view.addClass(base);
    view.addClass(sub);

    view.removeClass(base.getType());
    // still the superclass of ds.Sub
    assertTrue(view.getTypeHierarchy().contains(base.getType()));
    assertEquals(Optional.of(base.getType()), view.getTypeHierarchy().superClassOf(sub.getType()));

    view.removeClass(sub.getType());
    // no type references it anymore, so it is gone as in a hierarchy built without both
    assertFalse(view.getTypeHierarchy().contains(base.getType()));
    assertSameAsRebuilt(object(), abstractDs, department);
  }

  @Test
  public void removedClassCanBeAddedAgain() {
    JavaSootClass oldEmployee = view.getClass(employee).get();
    assertEquals(new HashSet<>(Arrays.asList(department, employee)), subtypesOfAbstractDs());

    view.removeClass(employee);
    view.addClass(oldEmployee);

    assertEquals(new HashSet<>(Arrays.asList(department, employee)), subtypesOfAbstractDs());
    assertSameAsRebuilt(object(), abstractDs, department, employee);
  }

  @Test
  public void anotherClassTakesTheIdOfARemovedClass() {
    assertEquals(new HashSet<>(Arrays.asList(department, employee)), subtypesOfAbstractDs());

    view.removeClass(employee);
    JavaSootClass manager = newSubclass("ds.Manager", abstractDs);
    view.addClass(manager);

    assertEquals(
        new HashSet<>(Arrays.asList(department, manager.getType())), subtypesOfAbstractDs());
    assertSameAsRebuilt(object(), abstractDs, department, manager.getType());
  }

  @Test
  public void removedImplementerAndInterfaceLeaveTheHierarchy() {
    view.getTypeHierarchy().subtypesOf(object());
    JavaSootClass named =
        newClass(
            "ds.Named",
            (JavaClassType) object(),
            Collections.emptySet(),
            EnumSet.of(ClassModifier.INTERFACE, ClassModifier.ABSTRACT));
    JavaSootClass namedDepartment =
        newClass(
            "ds.NamedDepartment",
            department,
            Collections.singleton((JavaClassType) named.getType()),
            EnumSet.noneOf(ClassModifier.class));
    view.addClass(named);
    view.addClass(namedDepartment);
    assertEquals(
        Collections.singleton(namedDepartment.getType()),
        view.getTypeHierarchy().implementersOf(named.getType()).collect(Collectors.toSet()));

    view.removeClass(namedDepartment.getType());

    assertEquals(
        Collections.emptySet(),
        view.getTypeHierarchy().implementersOf(named.getType()).collect(Collectors.toSet()));
    assertSameAsRebuilt(object(), abstractDs, department, named.getType());

    view.removeClass(named.getType());

    assertFalse(view.getTypeHierarchy().contains(named.getType()));
    assertSameAsRebuilt(object(), abstractDs, department);
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
