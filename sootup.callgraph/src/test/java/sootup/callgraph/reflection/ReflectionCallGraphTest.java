package sootup.callgraph.reflection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphConfig;
import sootup.callgraph.config.CallGraphConfigBuilder;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/** Reflective calls resolved from a TamiFlex log become plain CHA/RTA edges. */
public class ReflectionCallGraphTest {

  private static final String DIR = "src/test/resources/callgraph/Reflection/";

  private final JavaView view =
      new JavaView(
          Arrays.asList(
              new DefaultRuntimeAnalysisInputLocation(),
              new JavaClassPathAnalysisInputLocation(DIR + "binary", SourceType.Application)));

  private MethodSignature main(String cls) {
    return view.getIdentifierFactory()
        .getMethodSignature(
            "refl." + cls, "main", "void", Collections.singletonList("java.lang.String[]"));
  }

  private MethodSignature sig(String cls, String name, String ret, String... params) {
    return view.getIdentifierFactory()
        .getMethodSignature("refl." + cls, name, ret, Arrays.asList(params));
  }

  private CallGraph cg(MethodSignature entry, boolean rta, boolean withReflection) {
    CallGraphConfigBuilder b = CallGraphConfig.builder().view(view).entryPoints(List.of(entry));
    if (withReflection) {
      b.reflectionModel(new TamiflexReflectionModel(view, DIR + "refl.log"));
    }
    return rta ? b.rta().build().computeCallGraph() : b.cha().build().computeCallGraph();
  }

  /** Edge {@code from -> to} exists only with the reflection model. */
  private void assertResolvedOnlyWithModel(MethodSignature from, MethodSignature to, boolean rta) {
    assertFalse(cg(from, rta, false).callTargetsFrom(from).contains(to), "edge without model");
    assertTrue(cg(from, rta, true).callTargetsFrom(from).contains(to), "no edge with model");
  }

  @Test
  public void methodInvoke() {
    MethodSignature id = sig("Target", "id", "java.lang.Object", "java.lang.Object");
    assertResolvedOnlyWithModel(main("MethodInvoke"), id, false);
    assertResolvedOnlyWithModel(main("MethodInvoke"), id, true);
  }

  @Test
  public void methodInvokeStatic() {
    MethodSignature sid = sig("Target", "sid", "java.lang.Object", "java.lang.Object");
    assertResolvedOnlyWithModel(main("MethodInvokeStatic"), sid, false);
    assertResolvedOnlyWithModel(main("MethodInvokeStatic"), sid, true);
  }

  @Test
  public void classNewInstance() {
    MethodSignature init = sig("Impl", "<init>", "void");
    assertResolvedOnlyWithModel(main("ClassNewInstance"), init, false);
    assertResolvedOnlyWithModel(main("ClassNewInstance"), init, true);
    // RTA: Impl counts as instantiated only via reflection
    assertResolvedOnlyWithModel(main("ClassNewInstance"), sig("Impl", "serve", "void"), true);
  }

  @Test
  public void constructorNewInstance() {
    MethodSignature init = sig("Impl", "<init>", "void", "java.lang.Object");
    assertResolvedOnlyWithModel(main("ConstructorNewInstance"), init, false);
    assertResolvedOnlyWithModel(main("ConstructorNewInstance"), init, true);
    assertResolvedOnlyWithModel(main("ConstructorNewInstance"), sig("Impl", "serve", "void"), true);
  }

  @Test
  public void noneLeavesBodyUntouched() {
    var m = view.getMethod(main("MethodInvoke")).get();
    assertTrue(ReflectionModel.none().resolve(m, m.getBody()) == m.getBody());
    assertTrue(ReflectionModel.none().isNone());
  }
}
