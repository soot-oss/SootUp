package sootup.tests.typehierarchy.viewtypehierarchytestcase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.tests.typehierarchy.JavaTypeHierarchyTestBase;

/** @author Zun Wang */
@Tag("Java8")
public class ClassInheritanceWithAdditionalMethodTest extends JavaTypeHierarchyTestBase {

  ViewTypeHierarchy typeHierarchy = (ViewTypeHierarchy) this.getView().getTypeHierarchy();
  /**
   * Test: {@link java.lang.Object} is superclass of "SuperClass" and
   * "ClassInheritanceWithAdditionalMethod"
   */
  @Test
  public void testSuperClassExtendsObject() {
    assertEquals(
        typeHierarchy.superClassOf(getClassType("SuperClass")), getClassType("java.lang.Object"));
  }

  /**
   * Test: "SuperClass" and "ClassInheritanceWithAdditionalMethod" are subclasses of {@link
   * java.lang.Object}
   */
  @Test
  public void testObjectIsSuperclassOfSuperClass() {
    Set<ClassType> subClassSet = new HashSet<ClassType>();
    subClassSet = typeHierarchy.subclassesOf(getClassType("java.lang.Object"));
    assertTrue(subClassSet.contains(getClassType("SuperClass")));
    assertTrue(subClassSet.contains(getClassType("ClassInheritanceWithAdditionalMethod")));
  }

  /** Test: "ClassInheritanceWithAdditionalMethod"'s superclass is "SuperClass" */
  @Test
  public void testClassInheritanceClassExtendsSuperClass() {
    assertEquals(
        typeHierarchy.superClassOf(getClassType("ClassInheritanceWithAdditionalMethod")),
        getClassType("SuperClass"));
  }

  /** Test: "ClassInheritanceWithAdditionalMethod" is subclass of "SuperClass" */
  @Test
  public void testSuperClassIsSuperclassOfClassInheritanceClass() {
    assertEquals(
        typeHierarchy.superClassOf(getClassType("ClassInheritanceWithAdditionalMethod")),
        getClassType("SuperClass"));
  }

  /** Test: "ClassInheritanceWithAdditionalMethod" has additional method. */
  @Test
  public void ClassInheritanceClassHasAdditionalMethod() {
    SootClass sootClass =
        this.getView()
            .getClass(this.getView().getIdentifierFactory().getClassType(this.getClassName()))
            .get();
    SootClass superClass = this.getView().getClass(sootClass.getSuperclass().get()).get();

    Set<SootMethod> methodsSetOfSootClass = (Set<SootMethod>) sootClass.getMethods();
    Set<SootMethod> methodsSetOfSuperClass = (Set<SootMethod>) superClass.getMethods();

    Set<MethodSubSignature> methodSignaturesOfSootClass =
        methodsSetOfSootClass.stream()
            .map(sootMethod -> sootMethod.getSignature().getSubSignature())
            .collect(Collectors.toSet());

    Set<MethodSubSignature> methodSignaturesOfSuperClass =
        methodsSetOfSuperClass.stream()
            .map(sootMethod -> sootMethod.getSignature().getSubSignature())
            .collect(Collectors.toSet());

    boolean hasAdditionalMethod = false;
    for (MethodSubSignature msSubClass : methodSignaturesOfSootClass) {
      if (!(methodSignaturesOfSuperClass.contains(msSubClass))) {
        hasAdditionalMethod = true;
        break;
      }
    }
    assertTrue(hasAdditionalMethod);
  }
}
