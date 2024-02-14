package sootup.tests.typehierarchy.viewtypehierarchytestcase;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.util.Utils;
import sootup.tests.typehierarchy.JavaTypeHierarchyTestBase;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** @author Hasitha Rajapakse * */
@Tag("Java8")
public class InheritPublicDataTest extends JavaTypeHierarchyTestBase {
  @Test
  public void method() {
    ViewTypeHierarchy typeHierarchy =
        (ViewTypeHierarchy) customTestWatcher.getView().getTypeHierarchy();
    ClassType sootClassType = getClassType(customTestWatcher.getClassName());

    assertEquals(typeHierarchy.superClassOf(sootClassType), getClassType("SuperClass"));
    assertTrue(typeHierarchy.isSubtype(getClassType("SuperClass"), sootClassType));

    SootClass sootClass =
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
                    .getMethodSignature(sootClassType, "method", "void", Collections.emptyList())
                    .getSubSignature())
            .get();
    Body body = sootMethod.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);
    List<String> expectedStmts =
        Stream.of("r0 := @this: InheritPublicData", "i0 = r0.<SuperClass: int num>", "return")
            .collect(Collectors.toList());

    assertEquals(expectedStmts, actualStmts);
  }
}
