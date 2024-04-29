package sootup.tests.typehierarchy.viewtypehierarchytestcase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.tests.typehierarchy.JavaTypeHierarchyTestBase;

/** @author: Hasitha Rajapakse * */
@Tag("Java8")
public class ClassInheritanceTest extends JavaTypeHierarchyTestBase {
  @Test
  public void method() {
    ViewTypeHierarchy typeHierarchy = (ViewTypeHierarchy) this.getView().getTypeHierarchy();
    assertEquals(
        typeHierarchy.superClassOf(getClassType("ClassInheritance")).get(), getClassType("SuperClass"));

    Set<ClassType> subclassSet = new HashSet<>();
    subclassSet.add(getClassType("ClassInheritance"));
    assertEquals(typeHierarchy.subclassesOf(getClassType("SuperClass")).collect(Collectors.toSet()), subclassSet);

    assertTrue(
        typeHierarchy.isSubtype(getClassType("SuperClass"), getClassType("ClassInheritance")));
  }
}
