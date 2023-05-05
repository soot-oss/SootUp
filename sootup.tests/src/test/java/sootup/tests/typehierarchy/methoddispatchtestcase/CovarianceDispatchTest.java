package sootup.tests.typehierarchy.methoddispatchtestcase;

import static org.junit.Assert.*;

import categories.Java8Test;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.MethodDispatchResolver;
import sootup.tests.typehierarchy.MethodDispatchBase;

/** @author : Jonas Klauke * */
@Category(Java8Test.class)
public class CovarianceDispatchTest extends MethodDispatchBase {
  @Test
  public void method() {
    // sourcecode frontend test
    Set<MethodSignature> sourcecodeCandidates =
        MethodDispatchResolver.resolveAllDispatches(
            customTestWatcher.getView(),
            identifierFactory.parseMethodSignature("Superclass#method(): java.lang.Object"));

    assertFalse(
        sourcecodeCandidates.contains(
            identifierFactory.parseMethodSignature("Subclass#method(): Subclass")));
    assertTrue(
        sourcecodeCandidates.contains(
            identifierFactory.parseMethodSignature("Subclass#method(): java.lang.Object")));

    // bytecode frontend test
    Set<MethodSignature> bytecodeCandidates =
        MethodDispatchResolver.resolveAllDispatches(
            customTestWatcher.getView(),
            identifierFactory.parseMethodSignature(
                "java.util.AbstractSet#clone(): java.lang.Object"));
    assertFalse(
        bytecodeCandidates.contains(
            identifierFactory.parseMethodSignature(
                "java.util.EnumSet#clone(): java.util.EnumSet")));
    assertTrue(
        bytecodeCandidates.contains(
            identifierFactory.parseMethodSignature("java.util.EnumSet#clone(): java.lang.Object")));
  }
}
