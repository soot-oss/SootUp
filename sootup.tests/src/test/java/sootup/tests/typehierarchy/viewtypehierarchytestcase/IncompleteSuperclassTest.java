package sootup.tests.typehierarchy.viewtypehierarchytestcase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static sootup.core.util.ImmutableUtils.immutableList;

import categories.Java8Test;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.tests.typehierarchy.JavaTypeHierarchyTestBase;

/** @author Jonas Klauke * */
@Category(Java8Test.class)
public class IncompleteSuperclassTest extends JavaTypeHierarchyTestBase {
  @Test
  public void method() {
    ViewTypeHierarchy typeHierarchy =
        (ViewTypeHierarchy) customTestWatcher.getView().getTypeHierarchy();
    List<ClassType> superclasses =
        typeHierarchy.incompleteSuperClassesOf(getClassType("SubClassB"));
    ClassType object = getClassType("java.lang.Object");
    ImmutableList<ClassType> expectedSuperClasses =
        immutableList(getClassType("SubClassA"), object);
    assertEquals(expectedSuperClasses, superclasses);
    assertFalse(customTestWatcher.getView().getClass(object).isPresent());
  }
}
