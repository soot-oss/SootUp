package de.upb.swt.soot.test.callgraph.typehierarchy.testcase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.callgraph.typehierarchy.TypeHierarchy;
import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.test.callgraph.typehierarchy.JavaTypeHierarchyBase;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author: Hasitha Rajapakse * */
@Category(Java8Test.class)
public class AbstractClassInheritanceTest extends JavaTypeHierarchyBase {
  @Test
  public void method() {
    SootClass sootClass =
        (SootClass)
            customTestWatcher
                .getView()
                .getClass(
                    customTestWatcher
                        .getView()
                        .getIdentifierFactory()
                        .getClassType(customTestWatcher.getClassName()))
                .get();
    assertTrue(sootClass.hasSuperclass());
    SootClass superClass =
        (SootClass) customTestWatcher.getView().getClass(sootClass.getSuperclass().get()).get();
    assertTrue(superClass.isAbstract());

    ViewTypeHierarchy typeHierarchy =
        (ViewTypeHierarchy) TypeHierarchy.fromView(customTestWatcher.getView());
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
