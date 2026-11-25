package sootup.analysis.interprocedural.icfg;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JrtFileSystemAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/**
 * With call exceptions analyzed, a call only reaches the handlers that can catch an exception its
 * callees may throw, like with Soot's interprocedural throw analysis.
 */
class ICFGCallExceptionsTest {

  private static JavaView view;
  private static CallGraph cg;

  @BeforeAll
  static void setup() {
    view =
        new JavaView(
            Arrays.asList(
                new JavaClassPathAnalysisInputLocation("src/test/resources/icfg/binary"),
                new JrtFileSystemAnalysisInputLocation()));
    MethodSignature main =
        view.getIdentifierFactory()
            .getMethodSignature(
                "ICFGCallExceptionsExample",
                "main",
                "void",
                Collections.singletonList("java.lang.String[]"));
    cg = new ClassHierarchyAnalysisAlgorithm(view).initialize(Collections.singletonList(main));
  }

  private static JimpleBasedInterproceduralCFG icfg(boolean analyzeCallExceptions) {
    JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG(cg, view, true, false);
    icfg.setAnalyzeCallExceptions(analyzeCallExceptions);
    return icfg;
  }

  private static SootMethod method(String name) {
    return view
        .getClass(view.getIdentifierFactory().getClassType("ICFGCallExceptionsExample"))
        .orElseThrow()
        .getMethods()
        .stream()
        .filter(m -> m.getName().equals(name))
        .findFirst()
        .orElseThrow();
  }

  /** the call in the try block of the given method */
  private static Stmt call(SootMethod method) {
    return method.getBody().getStmts().stream()
        .filter(s -> s.isInvokableStmt() && s.asInvokableStmt().getInvokeExpr().isPresent())
        .findFirst()
        .orElseThrow();
  }

  private static Stmt handler(SootMethod method) {
    return method.getBody().getStmts().stream()
        .filter(
            s ->
                s instanceof JIdentityStmt
                    && ((JIdentityStmt) s).getRightOp() instanceof JCaughtExceptionRef)
        .findFirst()
        .orElseThrow();
  }

  private static boolean reachesHandler(JimpleBasedInterproceduralCFG icfg, String name) {
    SootMethod method = method(name);
    Stmt call = call(method);
    Stmt handler = handler(method);
    boolean succ = icfg.getSuccsOf(call).contains(handler);
    boolean pred = icfg.getPredsOf(handler).contains(call);
    assertEquals(succ, pred, "successors and predecessors disagree in " + name);
    boolean returnSite = icfg.getReturnSitesOfCallAt(call).contains(handler);
    assertEquals(succ, returnSite, "successors and return sites disagree in " + name);
    return succ;
  }

  @Test
  void callReachesHandlersOfWhatItsCalleesThrow() {
    JimpleBasedInterproceduralCFG icfg = icfg(true);
    assertTrue(reachesHandler(icfg, "callsThrowsIse"));
    assertTrue(reachesHandler(icfg, "callsNested"));
    assertTrue(reachesHandler(icfg, "callsRecursive"));
    assertTrue(reachesHandler(icfg, "callsReadsArray"));
    assertTrue(reachesHandler(icfg, "callsNativeWithThrows"));
  }

  @Test
  void callReachesHandlersOfWhatTheCallItselfThrows() {
    // the callee throws nothing, but the call throws a NullPointerException on a null receiver
    assertTrue(reachesHandler(icfg(true), "callsInstanceOnNull"));
  }

  @Test
  void callDoesNotReachHandlersOfWhatNoCalleeThrows() {
    JimpleBasedInterproceduralCFG icfg = icfg(true);
    assertFalse(reachesHandler(icfg, "callsNeverThrows"));
    assertFalse(reachesHandler(icfg, "callsCatchesItself"));
    assertFalse(reachesHandler(icfg, "callsNativeWithoutThrows"));
  }

  @Test
  void unreachableHandlerIsNoStartPoint() {
    JimpleBasedInterproceduralCFG icfg = icfg(true);
    SootMethod method = method("callsNeverThrows");
    assertFalse(icfg.getStartPointsOf(method).contains(handler(method)));
    assertTrue(icfg.getPredsOf(handler(method)).isEmpty());
  }

  @Test
  void callReachesAllHandlersByDefault() {
    JimpleBasedInterproceduralCFG icfg = icfg(false);
    for (String name :
        Arrays.asList(
            "callsNeverThrows",
            "callsThrowsIse",
            "callsCatchesItself",
            "callsReadsArray",
            "callsNativeWithoutThrows")) {
      assertTrue(reachesHandler(icfg, name), name);
    }
  }

  @Test
  void successorsAndPredecessorsAreSymmetric() {
    JimpleBasedInterproceduralCFG icfg = icfg(true);
    for (SootMethod method :
        view.getClass(view.getIdentifierFactory().getClassType("ICFGCallExceptionsExample"))
            .orElseThrow()
            .getMethods()) {
      if (!method.hasBody() || !cg.containsMethod(method.getSignature())) {
        continue;
      }
      for (Stmt stmt : method.getBody().getStmts()) {
        for (Stmt succ : icfg.getSuccsOf(stmt)) {
          assertTrue(icfg.getPredsOf(succ).contains(stmt), stmt + " -> " + succ);
        }
        for (Stmt pred : icfg.getPredsOf(stmt)) {
          assertTrue(icfg.getSuccsOf(pred).contains(stmt), pred + " -> " + stmt);
        }
      }
    }
  }
}
