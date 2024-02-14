package sootup.tests.typehierarchy.viewtypehierarchytestcase;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.tests.typehierarchy.JavaTypeHierarchyTestBase;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** @author: Hasitha Rajapakse * */
@Tag("Java8")
public class InterfaceImplementationTest extends JavaTypeHierarchyTestBase {
  @Test
  public void method() {
    ViewTypeHierarchy typeHierarchy =
        (ViewTypeHierarchy) customTestWatcher.getView().getTypeHierarchy();
    Set<ClassType> interfaceSet = new HashSet<>();
    interfaceSet.add(getClassType("InterfaceA"));
    assertEquals(
        typeHierarchy.implementedInterfacesOf(getClassType("InterfaceImplementation")),
        interfaceSet);
    Set<ClassType> implementerSet = new HashSet<>();
    implementerSet.add(getClassType("InterfaceImplementation"));
    assertEquals(typeHierarchy.implementersOf(getClassType("InterfaceA")), implementerSet);
    assertEquals(typeHierarchy.subtypesOf(getClassType("InterfaceA")), implementerSet);
  }
}
