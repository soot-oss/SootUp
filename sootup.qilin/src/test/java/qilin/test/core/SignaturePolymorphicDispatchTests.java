package qilin.test.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import qilin.core.VirtualCalls;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

/**
 * Qilin dispatch of signature-polymorphic calls (JVMS 2.9.3): call-site descriptor, e.g. {@code
 * invokeExact(String,int)int}, resolves to declared {@code invokeExact(Object[])Object}.
 *
 * <p>Unit level on purpose: end-to-end needs points-to for {@code MethodHandles.lookup()} results,
 * i.e. the whole {@code java.lang.invoke} closure.
 */
public class SignaturePolymorphicDispatchTests {

  private static final JavaIdentifierFactory factory = new JavaIdentifierFactory();
  private static final ClassType MH = factory.getClassType("java.lang.invoke.MethodHandle");
  private static JavaView view;

  @BeforeAll
  static void setUp() {
    view = new JavaView(new DefaultRuntimeAnalysisInputLocation());
  }

  private static MethodSignature declaredInvokeExact() {
    return factory.getMethodSignature(
        MH, "invokeExact", "java.lang.Object", Collections.singletonList("java.lang.Object[]"));
  }

  @Test
  public void virtualDispatchFromRuntimeReceiverType() {
    MethodSubSignature callSite =
        factory.getMethodSubSignature(
            "invokeExact",
            factory.getType("int"),
            Arrays.asList(factory.getType("java.lang.String"), factory.getType("int")));
    // runtime receiver: JDK-internal MethodHandle subclass
    ClassType receiver = factory.getClassType("java.lang.invoke.DirectMethodHandle");
    VirtualCalls vc = new VirtualCalls(view);
    SootMethod target = vc.resolveNonSpecial(receiver, callSite);
    assertNotNull(target);
    assertEquals(declaredInvokeExact(), target.getSignature());
    // second lookup served from vtbl cache
    assertSame(target, vc.resolveNonSpecial(receiver, callSite));
  }

  @Test
  public void nonPolymorphicMissStaysMiss() {
    MethodSubSignature missing =
        factory.getMethodSubSignature(
            "noSuchMethod", factory.getType("int"), Collections.emptyList());
    assertNull(
        new VirtualCalls(view)
            .resolveNonSpecial(
                factory.getClassType("java.lang.invoke.DirectMethodHandle"), missing));
  }
}
