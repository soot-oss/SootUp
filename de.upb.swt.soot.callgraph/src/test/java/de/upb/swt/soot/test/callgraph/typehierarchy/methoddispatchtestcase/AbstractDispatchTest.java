package de.upb.swt.soot.test.callgraph.typehierarchy.methoddispatchtestcase;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.callgraph.typehierarchy.MethodDispatchResolver;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.test.callgraph.typehierarchy.MethodDispatchBase;
import java.util.Collections;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author: Hasitha Rajapakse * */
@Category(Java8Test.class)
public class AbstractDispatchTest extends MethodDispatchBase {
  @Test
  public void method() {

    ClassType sootClassTypeA = getClassType("A");
    ClassType sootClassTypeB = getClassType("B");
    ClassType sootClassTypeC = getClassType("C");
    ClassType sootClassTypeAbstract = getClassType("AbstractClass");

    MethodSignature sootMethodA =
        identifierFactory.getMethodSignature(
            sootClassTypeA, "method", "void", Collections.emptyList());
    MethodSignature sootMethodB =
        identifierFactory.getMethodSignature(
            sootClassTypeB, "method", "void", Collections.emptyList());
    MethodSignature sootMethodC =
        identifierFactory.getMethodSignature(
            sootClassTypeC, "method", "void", Collections.emptyList());

    Set<MethodSignature> candidatesAbstract =
        MethodDispatchResolver.resolveAbstractDispatch(
            customTestWatcher.getView(),
            identifierFactory.getMethodSignature(
                sootClassTypeAbstract, "method", "void", Collections.emptyList()));
    assertTrue(candidatesAbstract.contains(sootMethodA));
    assertTrue(candidatesAbstract.contains(sootMethodB));
    assertTrue(candidatesAbstract.contains(sootMethodC));

    Set<MethodSignature> candidatesSuper =
        MethodDispatchResolver.resolveAbstractDispatch(
            customTestWatcher.getView(),
            identifierFactory.getMethodSignature(
                sootClassTypeA, "method", "void", Collections.emptyList()));
    assertTrue(candidatesSuper.contains(sootMethodB));
    assertTrue(candidatesSuper.contains(sootMethodC));
  }
}
