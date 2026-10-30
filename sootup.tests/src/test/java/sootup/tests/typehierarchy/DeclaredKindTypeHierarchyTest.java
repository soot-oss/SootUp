package sootup.tests.typehierarchy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/**
 * A type may be referenced with the other kind than it is declared with, e.g. because classes on
 * the classpath were compiled against another version of it: {@code A implements X} and {@code B
 * extends X} were compiled against an interface {@code X}, and {@code C extends Y} against a class
 * {@code Y}, but {@code X} is declared as a class and {@code Y} as an interface. The declaration
 * decides the kind of the type; references that need the other kind are no subtype relations, as
 * the JVM would reject them. Sources in resources/typehierarchy/declaredkind/source.
 */
public class DeclaredKindTypeHierarchyTest {

  private static final String DIR = "src/test/resources/typehierarchy/declaredkind/";

  private static AnalysisInputLocation location(String dir) {
    return new JavaClassPathAnalysisInputLocation(
        DIR + dir, SourceType.Application, Collections.emptyList());
  }

  /** Views whose type hierarchies see the references before and after the declarations. */
  private static List<JavaView> views() {
    return Arrays.asList(
        new JavaView(Arrays.asList(location("referrers"), location("declarations"))),
        new JavaView(Arrays.asList(location("declarations"), location("referrers"))));
  }

  private static Set<ClassType> subtypes(TypeHierarchy hierarchy, ClassType type) {
    return hierarchy.subtypesOf(type).collect(Collectors.toSet());
  }

  @Test
  public void declarationDecidesTheKind() {
    for (JavaView view : views()) {
      TypeHierarchy hierarchy = view.getTypeHierarchy();
      assertFalse(hierarchy.isInterface(view.getIdentifierFactory().getClassType("X")));
      assertTrue(hierarchy.isInterface(view.getIdentifierFactory().getClassType("Y")));
    }
  }

  @Test
  public void referencesWithTheOtherKindAreNoSubtypeRelations() {
    for (JavaView view : views()) {
      TypeHierarchy hierarchy = view.getTypeHierarchy();
      ClassType x = view.getIdentifierFactory().getClassType("X");
      ClassType y = view.getIdentifierFactory().getClassType("Y");
      ClassType a = view.getIdentifierFactory().getClassType("A");
      ClassType b = view.getIdentifierFactory().getClassType("B");
      ClassType c = view.getIdentifierFactory().getClassType("C");

      assertEquals(Collections.emptySet(), subtypes(hierarchy, x));
      assertEquals(Collections.emptySet(), subtypes(hierarchy, y));
      assertFalse(hierarchy.isSubtype(x, a));
      assertFalse(hierarchy.isSubtype(x, b));
      assertFalse(hierarchy.isSubtype(y, c));
      assertEquals(0, hierarchy.implementedInterfacesOf(a).count());
      assertEquals(0, hierarchy.implementedInterfacesOf(b).count());
      // like a class without an extends clause
      assertEquals(
          Optional.of(view.getIdentifierFactory().getClassType("java.lang.Object")),
          hierarchy.superClassOf(c));
    }
  }

  @Test
  public void otherTypesAreUnaffected() {
    for (JavaView view : views()) {
      TypeHierarchy hierarchy = view.getTypeHierarchy();
      ClassType object = view.getIdentifierFactory().getClassType("java.lang.Object");
      ClassType x = view.getIdentifierFactory().getClassType("X");
      ClassType a = view.getIdentifierFactory().getClassType("A");
      ClassType c = view.getIdentifierFactory().getClassType("C");
      assertTrue(hierarchy.isSubtype(object, x));
      assertTrue(hierarchy.isSubtype(object, a));
      assertTrue(subtypes(hierarchy, object).containsAll(Arrays.asList(x, a, c)));
    }
  }
}
