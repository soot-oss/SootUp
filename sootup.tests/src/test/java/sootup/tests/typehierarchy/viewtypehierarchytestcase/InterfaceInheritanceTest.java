package sootup.tests.typehierarchy.viewtypehierarchytestcase;

import static org.junit.Assert.assertEquals;

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
public class InterfaceInheritanceTest extends JavaTypeHierarchyTestBase {
  @Test
  public void method() {
    ViewTypeHierarchy typeHierarchy =
        (ViewTypeHierarchy) customTestWatcher.getView().getTypeHierarchy();
    Set<ClassType> interfaceSet = new HashSet<>();
    interfaceSet.add(getClassType("InterfaceA"));
    interfaceSet.add(getClassType("InterfaceB"));
    assertEquals(
        typeHierarchy.implementedInterfacesOf(getClassType("InterfaceInheritance")), interfaceSet);
    Set<ClassType> implementerSet = new HashSet<>();
    implementerSet.add(getClassType("InterfaceInheritance"));
    implementerSet.add(getClassType("InterfaceB"));
    assertEquals(typeHierarchy.implementersOf(getClassType("InterfaceA")), implementerSet);

    assertEquals(typeHierarchy.subtypesOf(getClassType("InterfaceA")), implementerSet);
  }
}
