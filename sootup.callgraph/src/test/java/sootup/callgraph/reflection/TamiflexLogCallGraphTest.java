package sootup.callgraph.reflection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphConfig;
import sootup.callgraph.config.CallGraphConfigBuilder;
import sootup.core.jimple.common.constant.ClassConstant;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/**
 * Call graphs built from real TamiFlex logs: {@code ReflectionTamiflex/refl.log} was recorded by
 * running each {@code tfx.*} program under the TamiFlex Play-Out agent (JDK 21 port, see
 * ReflectionTamiflex/README.md), JDK-internal entries included.
 */
public class TamiflexLogCallGraphTest {

  private static final String DIR = "src/test/resources/callgraph/ReflectionTamiflex/";

  private final JavaView view =
      new JavaView(
          Arrays.asList(
              new DefaultRuntimeAnalysisInputLocation(),
              new JavaClassPathAnalysisInputLocation(DIR + "binary", SourceType.Application)));

  private final TamiflexReflectionModel model = new TamiflexReflectionModel(view, DIR + "refl.log");

  private MethodSignature sig(String cls, String name, String ret, String... params) {
    return view.getIdentifierFactory()
        .getMethodSignature("tfx." + cls, name, ret, Arrays.asList(params));
  }

  private MethodSignature main(String cls) {
    return sig(cls, "main", "void", "java.lang.String[]");
  }

  private CallGraph cg(MethodSignature entry, boolean rta, ReflectionModel reflectionModel) {
    CallGraphConfigBuilder b =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(entry))
            .reflectionModel(reflectionModel);
    return rta ? b.rta().build().computeCallGraph() : b.cha().build().computeCallGraph();
  }

  /** {@code from -> to} reachable from {@code entry} only with the log-based model. */
  private void assertOnlyWithModel(
      MethodSignature entry, MethodSignature from, MethodSignature to, boolean rta) {
    assertFalse(
        cg(entry, rta, ReflectionModel.none()).callTargetsFrom(from).contains(to),
        from + " -> " + to + " without model");
    assertTrue(
        cg(entry, rta, model).callTargetsFrom(from).contains(to),
        from + " -> " + to + " missing with model");
  }

  private void assertOnlyWithModel(MethodSignature entry, MethodSignature to, boolean rta) {
    assertOnlyWithModel(entry, entry, to, rta);
  }

  @Test
  public void invokeOnSubclassUsesLoggedRuntimeTarget() {
    // getMethod on Base, receiver is a Sub - TamiFlex logs the dispatched Sub.foo
    MethodSignature subFoo = sig("Sub", "foo", "java.lang.Object", "java.lang.Object");
    assertOnlyWithModel(main("InvokeOnSubclass"), subFoo, false);
    assertOnlyWithModel(main("InvokeOnSubclass"), subFoo, true);
  }

  @Test
  public void privateMethodInvoke() {
    MethodSignature secret = sig("Base", "secret", "java.lang.Object");
    assertOnlyWithModel(main("PrivateInvoke"), secret, false);
    assertOnlyWithModel(main("PrivateInvoke"), secret, true);
  }

  @Test
  public void staticInvokeWithTwoArgs() {
    MethodSignature sbar =
        sig("Base", "sbar", "java.lang.Object", "java.lang.Object", "java.lang.Object");
    assertOnlyWithModel(main("StaticTwoArgs"), sbar, false);
    assertOnlyWithModel(main("StaticTwoArgs"), sbar, true);
  }

  @Test
  public void oneSiteManyTargetsInHelperMethod() {
    MethodSignature entry = main("PluginLoader");
    MethodSignature load = sig("PluginLoader", "load", "tfx.Plugin", "java.lang.String");
    for (String plugin : new String[] {"PluginA", "PluginB"}) {
      assertOnlyWithModel(entry, load, sig(plugin, "<init>", "void"), false);
      // RTA: plugins count as instantiated only through the reflective allocation
      assertOnlyWithModel(entry, entry, sig(plugin, "start", "void"), true);
    }
  }

  @Test
  public void legacyClassNewInstance() {
    assertOnlyWithModel(main("LegacyNewInstance"), sig("PluginA", "<init>", "void"), false);
    assertOnlyWithModel(main("LegacyNewInstance"), sig("PluginA", "start", "void"), true);
  }

  @Test
  public void constructorWithArgs() {
    MethodSignature init = sig("Base", "<init>", "void", "java.lang.Object", "java.lang.Object");
    assertOnlyWithModel(main("CtorWithArgs"), init, false);
    assertOnlyWithModel(main("CtorWithArgs"), init, true);
  }

  @Test
  public void overloadsAreDisambiguatedByLine() {
    // log names only "tfx.Overloaded.run"; line numbers pick the right overload
    MethodSignature entry = main("Overloaded");
    MethodSignature run1 = sig("Overloaded", "run", "void", "tfx.Base");
    MethodSignature run2 = sig("Overloaded", "run", "void", "tfx.Base", "java.lang.Object");
    MethodSignature foo = sig("Base", "foo", "java.lang.Object", "java.lang.Object");
    MethodSignature sbar =
        sig("Base", "sbar", "java.lang.Object", "java.lang.Object", "java.lang.Object");
    assertOnlyWithModel(entry, run1, foo, false);
    assertOnlyWithModel(entry, run2, sbar, false);
    CallGraph cg = cg(entry, false, model);
    assertFalse(cg.callTargetsFrom(run1).contains(sbar));
    assertFalse(cg.callTargetsFrom(run2).contains(foo));
  }

  private Body rewritten(String cls) {
    SootMethod m = view.getMethod(main(cls)).get();
    Body body = model.resolve(m, m.getBody());
    assertNotSame(m.getBody(), body);
    return body;
  }

  @Test
  public void fieldAccessesBecomeFieldRefs() {
    Body body = rewritten("Fields");
    assertTrue(
        body.getStmts().stream()
            .anyMatch(
                s -> s instanceof JAssignStmt a && a.getLeftOp() instanceof JInstanceFieldRef));
    assertTrue(
        body.getStmts().stream()
            .anyMatch(
                s -> s instanceof JAssignStmt a && a.getRightOp() instanceof JStaticFieldRef));
    // no call edges involved - graph still builds
    assertTrue(cg(main("Fields"), false, model).containsMethod(main("Fields")));
  }

  @Test
  public void arrayNewInstanceBecomesNewArray() {
    Body body = rewritten("ArrayCreate");
    assertTrue(
        body.getStmts().stream()
            .anyMatch(
                s ->
                    s instanceof JAssignStmt a
                        && a.getRightOp() instanceof JNewArrayExpr n
                        && n.getBaseType().toString().equals("tfx.Base")));
  }

  @Test
  public void resolveIsMemoized() {
    SootMethod m = view.getMethod(main("PrivateInvoke")).get();
    assertTrue(model.resolve(m, m.getBody()) == model.resolve(m, m.getBody()));
  }

  // --- batch 2: features covered by the TamiFlex JDK 21 port (tamiflex PR #15) ---

  @Test
  public void declaredMethodsLoopResolvesEachInvokedMethod() {
    MethodSignature entry = main("DeclaredMethodsLoop");
    for (boolean rta : new boolean[] {false, true}) {
      assertOnlyWithModel(entry, sig("Handlers", "onA", "void"), rta);
      assertOnlyWithModel(entry, sig("Handlers", "onB", "void"), rta);
      // helper() was returned by getDeclaredMethods but never invoked
      assertFalse(cg(entry, rta, model).containsMethod(sig("Handlers", "helper", "void")));
    }
  }

  @Test
  public void inheritedMethodFoundViaSubclass() {
    MethodSignature inherited = sig("Parent", "inherited", "java.lang.Object");
    assertOnlyWithModel(main("InheritedGetMethod"), inherited, false);
    assertOnlyWithModel(main("InheritedGetMethod"), inherited, true);
  }

  @Test
  public void interfaceDefaultMethod() {
    MethodSignature greet = sig("Greeter", "greet", "java.lang.String");
    assertOnlyWithModel(main("DefaultMethodInvoke"), greet, false);
    assertOnlyWithModel(main("DefaultMethodInvoke"), greet, true);
  }

  @Test
  public void proxyHandlerForwardsToRealTarget() {
    // invoke site lives in an instance method reached only through a JDK dynamic proxy
    MethodSignature handler =
        sig(
            "ForwardingHandler",
            "invoke",
            "java.lang.Object",
            "java.lang.Object",
            "java.lang.reflect.Method",
            "java.lang.Object[]");
    MethodSignature work = sig("RealWorker", "work", "java.lang.Object", "java.lang.Object");
    assertOnlyWithModel(handler, work, false);
  }

  @Test
  public void varargsTarget() {
    MethodSignature count = sig("VarargsTarget", "count", "int", "java.lang.String[]");
    assertOnlyWithModel(main("VarargsInvoke"), count, false);
    assertOnlyWithModel(main("VarargsInvoke"), count, true);
  }

  @Test
  public void hiddenLambdaClassTargetIsSkipped() {
    // logged target is a normalized hidden class (tfx.LambdaReflect$$Lambda$HASHED$...), absent
    // from the view: no rewrite, no bogus edge
    SootMethod m = view.getMethod(main("LambdaReflect")).get();
    assertSame(m.getBody(), model.resolve(m, m.getBody()));
    assertTrue(
        cg(main("LambdaReflect"), false, model).callTargetsFrom(main("LambdaReflect")).stream()
            .noneMatch(t -> t.getDeclClassType().getFullyQualifiedName().contains("$$Lambda")));
  }

  @Test
  public void nestedClassBinaryName() {
    MethodSignature entry = main("NestedNewInstance");
    assertOnlyWithModel(entry, sig("Outer$Inner", "<init>", "void"), false);
    assertOnlyWithModel(entry, sig("Outer$Inner", "start", "void"), true);
  }

  @Test
  public void chainedReflectiveCalls() {
    // RTA: RealWorker instantiated reflectively, then called reflectively in the same method
    MethodSignature entry = main("ChainedReflection");
    assertOnlyWithModel(entry, sig("RealWorker", "<init>", "void"), true);
    assertOnlyWithModel(
        entry, sig("RealWorker", "work", "java.lang.Object", "java.lang.Object"), true);
  }

  @Test
  public void threeArgForNameBecomesClassConstant() {
    Body body = rewritten("ForName3");
    assertTrue(
        body.getStmts().stream()
            .anyMatch(
                s ->
                    s instanceof JAssignStmt a
                        && a.getRightOp() instanceof ClassConstant c
                        && c.getValue().equals("Ltfx/PluginB;")));
    assertOnlyWithModel(main("ForName3"), sig("PluginB", "<init>", "void"), false);
  }
}
