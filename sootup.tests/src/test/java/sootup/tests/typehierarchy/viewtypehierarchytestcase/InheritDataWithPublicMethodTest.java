package sootup.tests.typehierarchy.viewtypehierarchytestcase;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.util.Utils;
import sootup.tests.typehierarchy.JavaTypeHierarchyTestBase;

/**
 * @author Hasitha Rajapakse *
 */
public class InheritDataWithPublicMethodTest extends JavaTypeHierarchyTestBase {
  @Test
  public void method() {
    ViewTypeHierarchy typeHierarchy = (ViewTypeHierarchy) this.getView().getTypeHierarchy();
    ClassType sootClassType = getClassType(this.getClassName());

    assertEquals(
        getClassType("SuperClass"), typeHierarchy.superClassOf(sootClassType).orElse(null));
    assertTrue(typeHierarchy.isSubtype(getClassType("SuperClass"), sootClassType));

    SootClass sootClass =
        this.getView()
            .getClass(this.getView().getIdentifierFactory().getClassType(this.getClassName()))
            .orElse(null);
    assertNotNull(sootClass);
    SootMethod sootMethod =
        sootClass
            .getMethod(
                getView()
                    .getIdentifierFactory()
                    .getMethodSignature(sootClassType, "method", "void", Collections.emptyList())
                    .getSubSignature())
            .orElse(null);
    assertNotNull(sootMethod);
    Body body = sootMethod.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);
    List<String> expectedStmts =
        Stream.of(
                "this := @this: InheritDataWithPublicMethod",
                "l1 = specialinvoke this.<SuperClass: int getnum()>()",
                "return")
            .collect(Collectors.toList());

    assertEquals(expectedStmts, actualStmts);
  }
}
