package sootup.tests.typehierarchy.methoddispatchtestcase;

import static junit.framework.TestCase.*;

import categories.Java8Test;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.MethodDispatchResolver;
import sootup.core.types.ClassType;
import sootup.tests.typehierarchy.MethodDispatchBase;

/** @author: Hasitha Rajapakse * */
@Category(Java8Test.class)
public class ConcreteDispatchTest extends MethodDispatchBase {
  @Test
  public void method() {
    ClassType sootClassTypeA = getClassType("A");
    ClassType sootClassTypeB = getClassType("B");

    MethodSignature sootMethod1 =
        identifierFactory.getMethodSignature(
            sootClassTypeA, "method", "void", Collections.emptyList());
    MethodSignature sootMethod2 =
        identifierFactory.getMethodSignature(
            sootClassTypeA, "method2", "void", Collections.emptyList());
    MethodSignature sootMethod3 =
        identifierFactory.getMethodSignature(
            sootClassTypeB, "method2", "void", Collections.emptyList());

    MethodSignature candidate1 =
        MethodDispatchResolver.resolveConcreteDispatch(customTestWatcher.getView(), sootMethod1);
    assertEquals(candidate1, sootMethod1);

    MethodSignature candidate2 =
        MethodDispatchResolver.resolveConcreteDispatch(customTestWatcher.getView(), sootMethod2);
    assertEquals(candidate2, sootMethod3);
  }
}
