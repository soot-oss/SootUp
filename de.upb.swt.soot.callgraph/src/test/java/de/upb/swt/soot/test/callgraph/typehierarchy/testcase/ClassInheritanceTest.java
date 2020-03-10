package de.upb.swt.soot.test.callgraph.typehierarchy.testcase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.callgraph.typehierarchy.TypeHierarchy;
import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.test.callgraph.typehierarchy.JavaTypeHierarchyBase;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author: Hasitha Rajapakse * */
@Category(Java8Test.class)
public class ClassInheritanceTest extends JavaTypeHierarchyBase {
  @Test
  public void method() {
    ViewTypeHierarchy typeHierarchy =
        (ViewTypeHierarchy) TypeHierarchy.fromView(customTestWatcher.getView());
    assertEquals(
        typeHierarchy.superClassOf(getClassType("ClassInheritance")), getClassType("SuperClass"));

    Set<ClassType> subclassSet = new HashSet<>();
    subclassSet.add(getClassType("ClassInheritance"));
    assertEquals(typeHierarchy.subclassesOf(getClassType("SuperClass")), subclassSet);

    assertTrue(
        typeHierarchy.isSubtype(getClassType("SuperClass"), getClassType("ClassInheritance")));
  }
}
