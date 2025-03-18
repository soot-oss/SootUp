package sootup.tests.typehierarchy.viewtypehierarchytestcase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static sootup.core.util.ImmutableUtils.immutableList;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.tests.typehierarchy.JavaTypeHierarchyTestBase;

/**
 * @author Jonas Klauke *
 */
public class IncompleteSuperclassTest extends JavaTypeHierarchyTestBase {
  @Test
  public void method() {
    ViewTypeHierarchy typeHierarchy = (ViewTypeHierarchy) this.getView().getTypeHierarchy();
    List<ClassType> superclasses =
        typeHierarchy.superClassesOf(getClassType("SubClassB")).collect(Collectors.toList());
    ClassType object = getClassType("java.lang.Object");
    ImmutableList<ClassType> expectedSuperClasses =
        immutableList(getClassType("SubClassA"), object);
    assertEquals(expectedSuperClasses, superclasses);
    assertFalse(this.getView().getClass(object).isPresent());
  }
}
