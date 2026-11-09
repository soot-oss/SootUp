package sootup.tests.typehierarchy.viewtypehierarchytestcase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ClassType;
import sootup.java.core.JavaSootClass;
import sootup.tests.typehierarchy.JavaTypeHierarchyTestBase;

/**
 * @author Zun Wang
 */
public class ClassInheritanceWithAdditionalMethodTest extends JavaTypeHierarchyTestBase {

  /**
   * Test: {@link java.lang.Object} is superclass of "SuperClass" and
   * "ClassInheritanceWithAdditionalMethod"
   */
  @Test
  public void testSuperClassExtendsObject() {
    assertEquals(
        getClassType("java.lang.Object"),
        getView().getTypeHierarchy().superClassOf(getClassType("SuperClass")).get());
  }

  /**
   * Test: "SuperClass" and "ClassInheritanceWithAdditionalMethod" are subclasses of {@link
   * java.lang.Object}
   */
  @Test
  public void testObjectIsSuperclassOfSuperClass() {
    Set<ClassType> subClassSet =
        getView()
            .getTypeHierarchy()
            .subclassesOf(getClassType("java.lang.Object"))
            .collect(Collectors.toSet());
    assertTrue(subClassSet.contains(getClassType("SuperClass")));
    assertTrue(subClassSet.contains(getClassType("ClassInheritanceWithAdditionalMethod")));
  }

  /** Test: "ClassInheritanceWithAdditionalMethod"'s superclass is "SuperClass" */
  @Test
  public void testClassInheritanceClassExtendsSuperClass() {
    assertEquals(
        getClassType("SuperClass"),
        getView()
            .getTypeHierarchy()
            .superClassOf(getClassType("ClassInheritanceWithAdditionalMethod"))
            .get());
  }

  /** Test: "ClassInheritanceWithAdditionalMethod" is subclass of "SuperClass" */
  @Test
  public void testSuperClassIsSuperclassOfClassInheritanceClass() {
    assertEquals(
        getClassType("SuperClass"),
        getView()
            .getTypeHierarchy()
            .superClassOf(getClassType("ClassInheritanceWithAdditionalMethod"))
            .get());
  }

  /** Test: "ClassInheritanceWithAdditionalMethod" has additional method. */
  @Test
  public void ClassInheritanceClassHasAdditionalMethod() {
    JavaSootClass sootClass =
        getView().getClass(getView().getIdentifierFactory().getClassType(getClassName())).get();
    JavaSootClass superClass = getView().getClass(sootClass.getSuperclass().get()).get();

    Set<MethodSubSignature> methodSignaturesOfSootClass =
        sootClass.getMethods().stream()
            .map(sootMethod -> sootMethod.getSignature().getSubSignature())
            .collect(Collectors.toSet());

    Set<MethodSubSignature> methodSignaturesOfSuperClass =
        superClass.getMethods().stream()
            .map(sootMethod -> sootMethod.getSignature().getSubSignature())
            .collect(Collectors.toSet());

    assertTrue(
        methodSignaturesOfSootClass.stream()
            .anyMatch(msSubClass -> !(methodSignaturesOfSuperClass.contains(msSubClass))));
  }
}
