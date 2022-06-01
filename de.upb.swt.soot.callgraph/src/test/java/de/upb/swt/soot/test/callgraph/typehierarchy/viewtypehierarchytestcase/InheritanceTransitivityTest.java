package de.upb.swt.soot.test.callgraph.typehierarchy.viewtypehierarchytestcase;

import static org.junit.Assert.assertEquals;

import categories.Java8Test;
import de.upb.swt.soot.core.typerhierachy.TypeHierarchy;
import de.upb.swt.soot.core.typerhierachy.ViewTypeHierarchy;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.test.callgraph.typehierarchy.JavaTypeHierarchyTestBase;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author: Hasitha Rajapakse * */
@Category(Java8Test.class)
public class InheritanceTransitivityTest extends JavaTypeHierarchyTestBase {

  @Test
  public void method() {
    ViewTypeHierarchy typeHierarchy =
        (ViewTypeHierarchy) TypeHierarchy.fromView(customTestWatcher.getView());
    Set<ClassType> subClassSet = new HashSet<>();
    subClassSet.add(getClassType("SubClassA"));
    subClassSet.add(getClassType("SubClassB"));
    assertEquals(typeHierarchy.subclassesOf(getClassType("InheritanceTransitivity")), subClassSet);
    assertEquals(typeHierarchy.superClassOf(getClassType("SubClassB")), getClassType("SubClassA"));
    assertEquals(
        typeHierarchy.superClassOf(getClassType("SubClassA")),
        getClassType("InheritanceTransitivity"));

    assertEquals(typeHierarchy.subtypesOf(getClassType("InheritanceTransitivity")), subClassSet);
  }
}
