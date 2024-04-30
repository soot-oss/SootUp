package sootup.tests.typehierarchy.viewtypehierarchytestcase;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.tests.typehierarchy.JavaTypeHierarchyTestBase;

/** @author: Hasitha Rajapakse * */
@Tag("Java8")
public class MethodOverridingTest extends JavaTypeHierarchyTestBase {
  @Test
  public void method() {
    ViewTypeHierarchy typeHierarchy = (ViewTypeHierarchy) this.getView().getTypeHierarchy();
    ClassType sootClassType = getClassType(this.getClassName());

    assertEquals(typeHierarchy.superClassOf(sootClassType), getClassType("SuperClass"));
    assertTrue(typeHierarchy.isSubtype(getClassType("SuperClass"), sootClassType));

    SootClass sootClass =
        this.getView()
            .getClass(this.getView().getIdentifierFactory().getClassType(this.getClassName()))
            .get();
    SootMethod sootMethod =
        sootClass
            .getMethod(
                getView()
                    .getIdentifierFactory()
                    .getMethodSignature(sootClassType, "method", "void", Collections.emptyList())
                    .getSubSignature())
            .get();
    Body body = sootMethod.getBody();
    assertNotNull(body);

    SootClass superClass = this.getView().getClass(sootClass.getSuperclass().get()).get();
    SootMethod superMethod =
        superClass
            .getMethod(
                getView()
                    .getIdentifierFactory()
                    .getMethodSignature(
                        superClass.getType(), "method", "void", Collections.emptyList())
                    .getSubSignature())
            .get();
    Body superBody = superMethod.getBody();
    assertNotNull(superBody);

    assertNotEquals(body, superBody);
  }
}
