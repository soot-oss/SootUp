package de.upb.swt.soot.test.java.sourcecode.typehierarchy.testcase;

import categories.Java8Test;
import de.upb.swt.soot.callgraph.typehierarchy.TypeHierarchy;
import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.test.java.sourcecode.typehierarchy.JavaTypeHierarchyBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/** @author: Hasitha Rajapakse * */

@Category(Java8Test.class)
public class MultiInterfaceImplementationTest extends JavaTypeHierarchyBase {
  @Test
  public void method() {
    ViewTypeHierarchy typeHierarchy =
        (ViewTypeHierarchy) TypeHierarchy.fromView(customTestWatcher.getView());
    Set<ClassType> interfaceSet = new HashSet<>();
    interfaceSet.add(getClassType("InterfaceA"));
    interfaceSet.add(getClassType("InterfaceB"));
    assertEquals(
        typeHierarchy.implementedInterfacesOf(getClassType(customTestWatcher.getClassName())),
        interfaceSet);
  }
}
