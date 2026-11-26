package sootup.analysis.interprocedural.icfg;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/**
 * Call targets without a body (here a native method) are callees of their call sites only if the
 * ICFG is asked to include them, like Soot's includePhantomCallees.
 */
class ICFGBodylessCalleesTest {

  private static JavaView view;
  private static CallGraph cg;
  private static SootMethod main;

  @BeforeAll
  static void setup() {
    view =
        new JavaView(
            Collections.singletonList(
                new JavaClassPathAnalysisInputLocation("src/test/resources/icfg/binary")));
    MethodSignature mainSig =
        view.getIdentifierFactory()
            .getMethodSignature(
                "ICFGBodylessCalleeExample",
                "main",
                "void",
                Collections.singletonList("java.lang.String[]"));
    cg = new ClassHierarchyAnalysisAlgorithm(view).initialize(Collections.singletonList(mainSig));
    main = view.getMethod(mainSig).orElseThrow();
  }

  private static Stmt callTo(JimpleBasedInterproceduralCFG icfg, String calleeName) {
    return main.getBody().getStmts().stream()
        .filter(icfg::isCallStmt)
        .filter(
            s ->
                s.asInvokableStmt()
                    .getInvokeExpr()
                    .get()
                    .getMethodSignature()
                    .getName()
                    .equals(calleeName))
        .findFirst()
        .orElseThrow();
  }

  private static Set<String> names(Collection<SootMethod> methods) {
    return methods.stream().map(SootMethod::getName).collect(Collectors.toSet());
  }

  @Test
  void bodylessCalleesAreExcludedByDefault() {
    JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG(cg, view, false, false);
    assertTrue(icfg.getCalleesOfCallAt(callTo(icfg, "nativeCall")).isEmpty());
    assertEquals(Set.of("withBody"), names(icfg.getCalleesOfCallAt(callTo(icfg, "withBody"))));
  }

  @Test
  void bodylessCalleesAreIncludedOnRequest() {
    JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG(cg, view, false, false);
    icfg.setIncludeBodylessCallees(true);
    assertEquals(Set.of("nativeCall"), names(icfg.getCalleesOfCallAt(callTo(icfg, "nativeCall"))));
    assertEquals(Set.of("withBody"), names(icfg.getCalleesOfCallAt(callTo(icfg, "withBody"))));
  }

  @Test
  void settingTheOptionAfterAQueryTakesEffect() {
    JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG(cg, view, false, false);
    Stmt call = callTo(icfg, "nativeCall");
    assertTrue(icfg.getCalleesOfCallAt(call).isEmpty());
    icfg.setIncludeBodylessCallees(true);
    assertEquals(Set.of("nativeCall"), names(icfg.getCalleesOfCallAt(call)));
  }
}
