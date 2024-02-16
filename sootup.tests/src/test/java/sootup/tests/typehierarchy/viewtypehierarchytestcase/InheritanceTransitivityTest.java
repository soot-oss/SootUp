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
public class InheritanceTransitivityTest extends JavaTypeHierarchyTestBase {

  @Test
  public void method() {
    ViewTypeHierarchy typeHierarchy = (ViewTypeHierarchy) this.getView().getTypeHierarchy();
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
