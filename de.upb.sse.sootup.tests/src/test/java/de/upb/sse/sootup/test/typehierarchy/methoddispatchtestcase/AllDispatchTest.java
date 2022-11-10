package de.upb.sse.sootup.test.typehierarchy.methoddispatchtestcase;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.sse.sootup.core.signatures.MethodSignature;
import de.upb.sse.sootup.core.typehierarchy.MethodDispatchResolver;
import de.upb.sse.sootup.core.types.ClassType;
import de.upb.sse.sootup.test.typehierarchy.MethodDispatchBase;
import java.util.Collections;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author : Jonas Klauke * */
@Category(Java8Test.class)
public class AllDispatchTest extends MethodDispatchBase {
  @Test
  public void method() {

    ClassType sootClassTypeA = getClassType("A");
    ClassType sootClassTypeB = getClassType("B");
    ClassType sootClassTypeC = getClassType("C");
    ClassType sootClassTypeAbstract = getClassType("AbstractClass");

    MethodSignature sootMethodB =
        identifierFactory.getMethodSignature(
            sootClassTypeB, "method", "void", Collections.emptyList());
    MethodSignature sootMethodC =
        identifierFactory.getMethodSignature(
            sootClassTypeC, "method", "void", Collections.emptyList());
    MethodSignature sootMethodAbstract =
        identifierFactory.getMethodSignature(
            sootClassTypeAbstract, "method", "void", Collections.emptyList());

    Set<MethodSignature> candidatesAbstract =
        MethodDispatchResolver.resolveAllDispatches(
            customTestWatcher.getView(),
            identifierFactory.getMethodSignature(
                sootClassTypeA, "method", "void", Collections.emptyList()));

    assertTrue(candidatesAbstract.contains(sootMethodB));
    assertTrue(candidatesAbstract.contains(sootMethodC));
    assertFalse(candidatesAbstract.contains(sootMethodAbstract));
  }
}
