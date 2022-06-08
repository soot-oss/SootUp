package de.upb.swt.soot.test.typehierarchy.viewtypehierarchytestcase;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.typerhierachy.TypeHierarchy;
import de.upb.swt.soot.core.typerhierachy.ViewTypeHierarchy;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.test.typehierarchy.JavaTypeHierarchyTestBase;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author: Hasitha Rajapakse * */
@Category(Java8Test.class)
public class MethodOverridingTest extends JavaTypeHierarchyTestBase {
  @Test
  public void method() {
    ViewTypeHierarchy typeHierarchy =
        (ViewTypeHierarchy) TypeHierarchy.fromView(customTestWatcher.getView());
    ClassType sootClassType = getClassType(customTestWatcher.getClassName());

    assertEquals(typeHierarchy.superClassOf(sootClassType), getClassType("SuperClass"));
    assertTrue(typeHierarchy.isSubtype(getClassType("SuperClass"), sootClassType));

    SootClass<?> sootClass =
        customTestWatcher
            .getView()
            .getClass(
                customTestWatcher
                    .getView()
                    .getIdentifierFactory()
                    .getClassType(customTestWatcher.getClassName()))
            .get();
    SootMethod sootMethod =
        sootClass
            .getMethod(
                identifierFactory
                    .getMethodSignature("method", sootClassType, "void", Collections.emptyList())
                    .getSubSignature())
            .get();
    Body body = sootMethod.getBody();
    assertNotNull(body);

    SootClass<?> superClass =
        customTestWatcher.getView().getClass(sootClass.getSuperclass().get()).get();
    SootMethod superMethod =
        superClass
            .getMethod(
                identifierFactory
                    .getMethodSignature(
                        "method", superClass.getType(), "void", Collections.emptyList())
                    .getSubSignature())
            .get();
    Body superBody = superMethod.getBody();
    assertNotNull(superBody);

    assertNotEquals(body, superBody);
  }
}
