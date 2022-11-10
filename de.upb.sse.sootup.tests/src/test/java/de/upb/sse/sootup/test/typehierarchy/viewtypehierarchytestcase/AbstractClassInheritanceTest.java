package de.upb.sse.sootup.test.typehierarchy.viewtypehierarchytestcase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.sse.sootup.core.model.SootClass;
import de.upb.sse.sootup.core.typehierarchy.ViewTypeHierarchy;
import de.upb.sse.sootup.core.types.ClassType;
import de.upb.sse.sootup.test.typehierarchy.JavaTypeHierarchyTestBase;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Hasitha Rajapakse * */
@Category(Java8Test.class)
public class AbstractClassInheritanceTest extends JavaTypeHierarchyTestBase {
  @Test
  public void method() {
    SootClass<?> sootClass =
        customTestWatcher
            .getView()
            .getClass(
                customTestWatcher
                    .getView()
                    .getIdentifierFactory()
                    .getClassType(customTestWatcher.getClassName()))
            .get();
    assertTrue(sootClass.hasSuperclass());
    SootClass<?> superClass =
        customTestWatcher.getView().getClass(sootClass.getSuperclass().get()).get();
    assertTrue(superClass.isAbstract());

    ViewTypeHierarchy typeHierarchy =
        (ViewTypeHierarchy) customTestWatcher.getView().getTypeHierarchy();
    assertEquals(
        typeHierarchy.superClassOf(getClassType("AbstractClassInheritance")),
        getClassType("AbstractClass"));

    Set<ClassType> subclassSet = new HashSet<>();
    subclassSet.add(getClassType("AbstractClassInheritance"));
    assertEquals(typeHierarchy.subclassesOf(getClassType("AbstractClass")), subclassSet);

    assertTrue(
        typeHierarchy.isSubtype(
            getClassType("AbstractClass"), getClassType("AbstractClassInheritance")));
  }
}
