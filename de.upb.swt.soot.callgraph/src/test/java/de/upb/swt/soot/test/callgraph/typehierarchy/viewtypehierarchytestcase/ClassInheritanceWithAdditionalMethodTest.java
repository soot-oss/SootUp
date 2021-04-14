package de.upb.swt.soot.test.callgraph.typehierarchy.viewtypehierarchytestcase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.callgraph.typehierarchy.TypeHierarchy;
import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSubSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.test.callgraph.typehierarchy.JavaTypeHierarchyTestBase;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Zun Wang */
@Category(Java8Test.class)
public class ClassInheritanceWithAdditionalMethodTest extends JavaTypeHierarchyTestBase {

  ViewTypeHierarchy typeHierarchy =
      (ViewTypeHierarchy) TypeHierarchy.fromView(customTestWatcher.getView());
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
    SootClass<?> sootClass =
        customTestWatcher
            .getView()
            .getClass(
                customTestWatcher
                    .getView()
                    .getIdentifierFactory()
                    .getClassType(customTestWatcher.getClassName()))
            .get();
    SootClass<?> superClass =
        customTestWatcher.getView().getClass(sootClass.getSuperclass().get()).get();

    Set<SootMethod> methodsSetOfSootClass = (Set<SootMethod>) sootClass.getMethods();
    Set<SootMethod> methodsSetOfSuperClass = (Set<SootMethod>) superClass.getMethods();

    Set<MethodSubSignature> methodSignaturesOfSootClass =
        methodsSetOfSootClass.stream()
            .map(sootMethod -> sootMethod.getSubSignature())
            .collect(Collectors.toSet());

    Set<MethodSubSignature> methodSignaturesOfSuperClass =
        methodsSetOfSuperClass.stream()
            .map(sootMethod -> sootMethod.getSubSignature())
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
