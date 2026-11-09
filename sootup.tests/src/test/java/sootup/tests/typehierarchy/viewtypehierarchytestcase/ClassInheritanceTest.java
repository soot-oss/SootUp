package sootup.tests.typehierarchy.viewtypehierarchytestcase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.tests.typehierarchy.JavaTypeHierarchyTestBase;

/**
 * @author: Hasitha Rajapakse *
 */
public class ClassInheritanceTest extends JavaTypeHierarchyTestBase {
  @Test
  public void method() {
    ViewTypeHierarchy typeHierarchy = (ViewTypeHierarchy) this.getView().getTypeHierarchy();
    assertEquals(
        getClassType("SuperClass"),
        typeHierarchy.superClassOf(getClassType("ClassInheritance")).get());

    Set<ClassType> subclassSet = new HashSet<>();
    subclassSet.add(getClassType("ClassInheritance"));
    assertEquals(
        subclassSet,
        typeHierarchy.subclassesOf(getClassType("SuperClass")).collect(Collectors.toSet()));

    assertTrue(
        typeHierarchy.isSubtype(getClassType("SuperClass"), getClassType("ClassInheritance")));
  }
}
