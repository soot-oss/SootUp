package sootup.tests.typehierarchy.viewtypehierarchytestcase;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.java.core.types.JavaClassType;
import sootup.tests.typehierarchy.JavaTypeHierarchyTestBase;

/**
 * @author: Hasitha Rajapakse *
 */
public class InheritanceTransitivityTest extends JavaTypeHierarchyTestBase {

  @Test
  public void method() {
    JavaClassType subClassA = getClassType("SubClassA");
    JavaClassType subClassB = getClassType("SubClassB");

    TypeHierarchy typeHierarchy = getView().getTypeHierarchy();
    Set<ClassType> subClassSet = new HashSet<>();
    subClassSet.add(subClassA);
    subClassSet.add(subClassB);

    JavaClassType inheritanceTransitivity = getClassType("InheritanceTransitivity");
    assertEquals(
        subClassSet,
        typeHierarchy.subclassesOf(inheritanceTransitivity).collect(Collectors.toSet()));
    assertEquals(subClassA, typeHierarchy.superClassOf(subClassB).get());
    assertEquals(inheritanceTransitivity, typeHierarchy.superClassOf(subClassA).get());

    assertEquals(
        subClassSet, typeHierarchy.subtypesOf(inheritanceTransitivity).collect(Collectors.toSet()));
  }
}
