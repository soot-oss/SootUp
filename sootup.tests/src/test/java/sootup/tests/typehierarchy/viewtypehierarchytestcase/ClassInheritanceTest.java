package sootup.tests.typehierarchy.viewtypehierarchytestcase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.tests.typehierarchy.JavaTypeHierarchyTestBase;

/** @author: Hasitha Rajapakse * */
@Category(Java8Test.class)
public class ClassInheritanceTest extends JavaTypeHierarchyTestBase {
  @Test
  public void method() {
    ViewTypeHierarchy typeHierarchy =
        (ViewTypeHierarchy) customTestWatcher.getView().getTypeHierarchy();
    assertEquals(
        typeHierarchy.superClassOf(getClassType("ClassInheritance")), getClassType("SuperClass"));

    Set<ClassType> subclassSet = new HashSet<>();
    subclassSet.add(getClassType("ClassInheritance"));
    assertEquals(typeHierarchy.subclassesOf(getClassType("SuperClass")), subclassSet);

    assertTrue(
        typeHierarchy.isSubtype(getClassType("SuperClass"), getClassType("ClassInheritance")));
  }
}
