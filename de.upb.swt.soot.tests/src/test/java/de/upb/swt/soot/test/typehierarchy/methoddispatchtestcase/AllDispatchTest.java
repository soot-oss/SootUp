package de.upb.swt.soot.test.typehierarchy.methoddispatchtestcase;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.typehierarchy.MethodDispatchResolver;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.test.typehierarchy.MethodDispatchBase;
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
    assertTrue(candidatesAbstract.contains(sootMethodAbstract));
  }
}

