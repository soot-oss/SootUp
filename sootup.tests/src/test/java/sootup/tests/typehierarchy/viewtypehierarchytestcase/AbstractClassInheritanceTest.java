package sootup.tests.typehierarchy.viewtypehierarchytestcase;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootClass;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.tests.typehierarchy.JavaTypeHierarchyTestBase;

/**
 * @author Hasitha Rajapakse *
 */
public class AbstractClassInheritanceTest extends JavaTypeHierarchyTestBase {
  @Test
  public void method() {
    SootClass sootClass =
        getView()
            .getClass(getView().getIdentifierFactory().getClassType(getClassName()))
            .orElse(null);
    assertNotNull(sootClass);
    assertTrue(sootClass.hasSuperclass());

    ClassType superClassType = sootClass.getSuperclass().orElse(null);
    assertNotNull(superClassType);
    SootClass superClass = getView().getClass(superClassType).orElse(null);
    assertNotNull(superClass);
    assertTrue(superClass.isAbstract());

    ViewTypeHierarchy typeHierarchy = (ViewTypeHierarchy) getView().getTypeHierarchy();
    assertEquals(
        getClassType("AbstractClass"),
        typeHierarchy.superClassOf(getClassType("AbstractClassInheritance")).get());

    Set<ClassType> subclassSet = new HashSet<>();
    subclassSet.add(getClassType("AbstractClassInheritance"));
    assertEquals(
        subclassSet,
        typeHierarchy.subclassesOf(getClassType("AbstractClass")).collect(Collectors.toSet()));

    assertTrue(
        typeHierarchy.isSubtype(
            getClassType("AbstractClass"), getClassType("AbstractClassInheritance")));
  }
}
