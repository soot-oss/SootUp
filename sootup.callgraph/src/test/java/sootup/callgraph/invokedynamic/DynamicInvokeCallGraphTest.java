package sootup.callgraph.invokedynamic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphConfig;
import sootup.callgraph.config.CallGraphConfigBuilder;
import sootup.core.jimple.common.constant.MethodHandle;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.expr.JInterfaceInvokeExpr;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/** CHA/RTA with the shared {@link DynamicInvokeResolver}; sources in resources/.../source. */
public class DynamicInvokeCallGraphTest {

  private static final String DIR = "src/test/resources/callgraph/InvokeDynamic/";

  private final JavaView view =
      new JavaView(new JavaClassPathAnalysisInputLocation(DIR + "binary", SourceType.Application));

  private MethodSignature sig(String cls, String name, String ret, String... params) {
    return view.getIdentifierFactory()
        .getMethodSignature("indy." + cls, name, ret, Arrays.asList(params));
  }

  private MethodSignature main(String cls) {
    return sig(cls, "main", "void", "java.lang.String[]");
  }

  private CallGraph cg(MethodSignature entry, boolean rta, DynamicInvokeResolver resolver) {
    CallGraphConfigBuilder b = CallGraphConfig.builder().view(view).entryPoints(List.of(entry));
    if (resolver != null) {
      b.dynamicInvokeResolver(resolver);
    }
    return rta ? b.rta().build().computeCallGraph() : b.cha().build().computeCallGraph();
  }

  /**
   * {@code from -> to} exists by default and disappears with {@link DynamicInvokeResolver#none()}.
   */
  private void assertResolved(MethodSignature entry, MethodSignature from, MethodSignature to) {
    for (boolean rta : new boolean[] {false, true}) {
      assertTrue(cg(entry, rta, null).callTargetsFrom(from).contains(to), "default, rta=" + rta);
      assertFalse(
          cg(entry, rta, DynamicInvokeResolver.none()).containsMethod(to), "none, rta=" + rta);
    }
  }

  @Test
  public void capturingLambda() {
    MethodSignature body = sig("CapturingLambda", "lambda$main$0", "void", "indy.Payload");
    assertResolved(main("CapturingLambda"), main("CapturingLambda"), body);
    assertTrue(
        cg(main("CapturingLambda"), false, null)
            .callTargetsFrom(body)
            .contains(sig("CapturingLambda", "sink", "void", "java.lang.Object")));
  }

  @Test
  public void instanceLambda() {
    MethodSignature go = sig("InstanceLambda", "go", "void");
    assertResolved(main("InstanceLambda"), go, sig("InstanceLambda", "lambda$go$0", "void"));
  }

  @Test
  public void boundMethodRef() {
    assertResolved(main("BoundMethodRef"), main("BoundMethodRef"), sig("Worker", "work", "void"));
  }

  @Test
  public void constructorRef() {
    assertResolved(main("ConstructorRef"), main("ConstructorRef"), sig("Worker", "<init>", "void"));
  }

  @Test
  public void lambdaIsCalledFromCallSiteNotCreationSite() {
    MethodSignature entry = main("CapturingLambda");
    MethodSignature body = sig("CapturingLambda", "lambda$main$0", "void", "indy.Payload");
    for (boolean rta : new boolean[] {false, true}) {
      List<CallGraph.Call> calls =
          cg(entry, rta, null).callsFrom(entry).stream()
              .filter(c -> c.targetMethodSignature().equals(body))
              .toList();
      assertEquals(1, calls.size(), "rta=" + rta);
      assertTrue(
          calls.get(0).invokableStmt().getInvokeExpr().get() instanceof JInterfaceInvokeExpr,
          "rta=" + rta);
    }
  }

  @Test
  public void lambdaWithArguments() {
    MethodSignature body = sig("LambdaArgs", "lambda$main$0", "indy.Payload", "indy.Payload");
    assertResolved(main("LambdaArgs"), main("LambdaArgs"), body);
    assertTrue(
        cg(main("LambdaArgs"), true, null)
            .callTargetsFrom(body)
            .contains(sig("LambdaArgs", "id", "indy.Payload", "indy.Payload")));
  }

  @Test
  public void unboundMethodRef() {
    assertResolved(main("UnboundRef"), main("UnboundRef"), sig("Payload", "self", "indy.Payload"));
  }

  @Test
  public void constructorRefWithResult() {
    assertResolved(main("CtorRefResult"), main("CtorRefResult"), sig("Worker", "<init>", "void"));
  }

  @Test
  public void stringConcatenationCallsToString() {
    // "x" + p
    assertResolved(main("Concat"), main("Concat"), sig("Payload", "toString", "java.lang.String"));
  }

  @Test
  public void recordMethodsCallComponentMethods() {
    MethodSignature entry = main("Rec");
    assertResolved(
        entry,
        sig("Rec", "toString", "java.lang.String"),
        sig("Payload", "toString", "java.lang.String"));
    assertResolved(entry, sig("Rec", "hashCode", "int"), sig("Payload", "hashCode", "int"));
    assertResolved(
        entry,
        sig("Rec", "equals", "boolean", "java.lang.Object"),
        sig("Payload", "equals", "boolean", "java.lang.Object"));
  }

  @Test
  public void neverCalledLambdaIsUnreachableUnlessCreationSiteEdges() {
    MethodSignature entry = main("NeverCalled");
    MethodSignature body = sig("NeverCalled", "lambda$main$0", "void");
    DynamicInvokeResolver creationSite =
        DynamicInvokeResolver.bootstrapMethodHandles().withCreationSiteEdges();
    for (boolean rta : new boolean[] {false, true}) {
      assertFalse(cg(entry, rta, null).containsMethod(body), "rta=" + rta);
      assertTrue(cg(entry, rta, creationSite).callTargetsFrom(entry).contains(body), "rta=" + rta);
    }
  }

  @Test
  public void customResolverIsConsulted() {
    // a resolver that drops every target except constructors
    DynamicInvokeResolver ctorsOnly =
        expr ->
            DynamicInvokeResolver.bootstrapMethodHandles().resolve(expr).stream()
                .filter(t -> t.kind() == MethodHandle.Kind.REF_INVOKE_CONSTRUCTOR)
                .toList();
    assertTrue(
        cg(main("ConstructorRef"), false, ctorsOnly)
            .containsMethod(sig("Worker", "<init>", "void")));
    assertFalse(
        cg(main("BoundMethodRef"), false, ctorsOnly).containsMethod(sig("Worker", "work", "void")));
  }

  @Test
  public void targetsDescribeCaptures() {
    assertEquals(
        List.of(
            new DynamicInvokeTarget(
                sig("CapturingLambda", "lambda$main$0", "void", "indy.Payload"),
                MethodHandle.Kind.REF_INVOKE_STATIC,
                true)),
        targetsIn(main("CapturingLambda")));
    DynamicInvokeTarget captured = targetsIn(main("CapturingLambda")).get(0);
    assertEquals(0, captured.captureParameterIndex(0));

    DynamicInvokeTarget bound = targetsIn(main("BoundMethodRef")).get(0);
    assertEquals(MethodHandle.Kind.REF_INVOKE_VIRTUAL, bound.kind());
    assertEquals(DynamicInvokeTarget.RECEIVER, bound.captureParameterIndex(0));

    DynamicInvokeTarget instance = targetsIn(sig("InstanceLambda", "go", "void")).get(0);
    assertEquals(DynamicInvokeTarget.RECEIVER, instance.captureParameterIndex(0));
  }

  private List<DynamicInvokeTarget> targetsIn(MethodSignature method) {
    Optional<JDynamicInvokeExpr> indy =
        view.getMethod(method).get().getBody().getStmts().stream()
            .filter(s -> s.isInvokableStmt())
            .flatMap(s -> s.asInvokableStmt().getInvokeExpr().stream())
            .filter(e -> e instanceof JDynamicInvokeExpr)
            .map(e -> (JDynamicInvokeExpr) e)
            .findFirst();
    return DynamicInvokeResolver.bootstrapMethodHandles().resolve(indy.get());
  }
}
