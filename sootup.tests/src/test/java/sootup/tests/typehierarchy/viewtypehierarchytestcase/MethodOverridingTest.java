package sootup.tests.typehierarchy.viewtypehierarchytestcase;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.java.core.types.JavaClassType;
import sootup.tests.typehierarchy.JavaTypeHierarchyTestBase;

/**
 * @author: Hasitha Rajapakse *
 */
public class MethodOverridingTest extends JavaTypeHierarchyTestBase {
  @Test
  public void method() {
    JavaClassType superClass1 = getClassType("SuperClass");
    TypeHierarchy typeHierarchy = getView().getTypeHierarchy();
    ClassType sootClassType = getClassType(this.getClassName());

    assertEquals(superClass1, typeHierarchy.superClassOf(sootClassType).get());
    assertTrue(typeHierarchy.isSubtype(superClass1, sootClassType));

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

    SootClass superClass = getView().getClass(sootClass.getSuperclass().get()).get();
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
