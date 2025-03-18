package sootup.tests.typehierarchy.viewtypehierarchytestcase;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
public class MultiInterfaceImplementationTest extends JavaTypeHierarchyTestBase {
  @Test
  public void method() {
    ViewTypeHierarchy typeHierarchy = (ViewTypeHierarchy) this.getView().getTypeHierarchy();
    Set<ClassType> interfaceSet = new HashSet<>();
    interfaceSet.add(getClassType("InterfaceA"));
    interfaceSet.add(getClassType("InterfaceB"));
    assertEquals(
        interfaceSet,
        typeHierarchy
            .implementedInterfacesOf(getClassType("MultiInterfaceImplementation"))
            .collect(Collectors.toSet()));
    Set<ClassType> implementerSet = new HashSet<>();
    implementerSet.add(getClassType("MultiInterfaceImplementation"));
    assertEquals(
        typeHierarchy.implementersOf(getClassType("InterfaceA")).collect(Collectors.toSet()),
        implementerSet);
    assertEquals(
        typeHierarchy.implementersOf(getClassType("InterfaceB")).collect(Collectors.toSet()),
        implementerSet);
    assertEquals(
        typeHierarchy.subtypesOf(getClassType("InterfaceB")).collect(Collectors.toSet()),
        implementerSet);
  }
}
