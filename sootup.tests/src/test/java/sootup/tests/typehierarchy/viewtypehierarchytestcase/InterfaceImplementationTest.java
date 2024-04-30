package sootup.tests.typehierarchy.viewtypehierarchytestcase;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.tests.typehierarchy.JavaTypeHierarchyTestBase;

/** @author: Hasitha Rajapakse * */
@Tag("Java8")
public class InterfaceImplementationTest extends JavaTypeHierarchyTestBase {
  @Test
  public void method() {
    ViewTypeHierarchy typeHierarchy = (ViewTypeHierarchy) this.getView().getTypeHierarchy();
    Set<ClassType> interfaceSet = new HashSet<>();
    interfaceSet.add(getClassType("InterfaceA"));
    assertEquals(
        interfaceSet,
        typeHierarchy.implementedInterfacesOf(getClassType("InterfaceImplementation")));
    Set<ClassType> implementerSet = new HashSet<>();
    implementerSet.add(getClassType("InterfaceImplementation"));
    assertEquals(typeHierarchy.implementersOf(getClassType("InterfaceA")), implementerSet);
    assertEquals(typeHierarchy.subtypesOf(getClassType("InterfaceA")), implementerSet);
  }
}
