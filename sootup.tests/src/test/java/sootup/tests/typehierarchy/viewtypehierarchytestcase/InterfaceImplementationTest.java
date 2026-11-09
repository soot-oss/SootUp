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
public class InterfaceImplementationTest extends JavaTypeHierarchyTestBase {
  @Test
  public void method() {
    JavaClassType interfaceA = getClassType("InterfaceA");
    JavaClassType interfaceImplementation = getClassType("InterfaceImplementation");

    TypeHierarchy typeHierarchy = getView().getTypeHierarchy();
    Set<ClassType> interfaceSet = new HashSet<>();
    interfaceSet.add(interfaceA);
    assertEquals(
        interfaceSet,
        typeHierarchy.implementedInterfacesOf(interfaceImplementation).collect(Collectors.toSet()));

    Set<ClassType> implementerSet = new HashSet<>();
    implementerSet.add(interfaceImplementation);
    assertEquals(
        implementerSet, typeHierarchy.implementersOf(interfaceA).collect(Collectors.toSet()));
    assertEquals(implementerSet, typeHierarchy.subtypesOf(interfaceA).collect(Collectors.toSet()));
  }
}
