package sootup.tests.typehierarchy.viewtypehierarchytestcase;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootClass;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.tests.typehierarchy.JavaTypeHierarchyTestBase;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** @author Hasitha Rajapakse * */
@Tag("Java8")
public class AbstractClassInheritanceTest extends JavaTypeHierarchyTestBase {
  @Test
  public void method() {
    SootClass sootClass =
        customTestWatcher
            .getView()
            .getClass(identifierFactory.getClassType(customTestWatcher.getClassName()))
            .orElse(null);
    assertNotNull(sootClass);
    assertTrue(sootClass.hasSuperclass());

    ClassType superClassType = sootClass.getSuperclass().orElse(null);
    assertNotNull(superClassType);
    SootClass superClass = customTestWatcher.getView().getClass(superClassType).orElse(null);
    assertNotNull(superClass);
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
