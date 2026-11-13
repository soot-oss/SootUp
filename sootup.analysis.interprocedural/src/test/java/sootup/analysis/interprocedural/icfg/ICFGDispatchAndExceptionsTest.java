package sootup.analysis.interprocedural.icfg;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.JThrowStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/**
 * Checks that {@link JimpleBasedInterproceduralCFG} resolves callees through the call graph, honors
 * the enableExceptions flag, and reports start points and branch targets consistently with the
 * graph it exposes.
 */
class ICFGDispatchAndExceptionsTest {

  private static JavaView view;
  private static List<MethodSignature> entryPoints;

  @BeforeAll
  static void setup() {
    view =
        new JavaView(
            Collections.singletonList(
                new JavaClassPathAnalysisInputLocation("src/test/resources/icfg/binary")));
    entryPoints =
        Collections.singletonList(
            view.getIdentifierFactory()
                .getMethodSignature(
                    "ICFGDispatchExample",
                    "main",
                    "void",
                    Collections.singletonList("java.lang.String[]")));
  }

  private static SootMethod method(String className, String methodName) {
    return view
        .getClass(view.getIdentifierFactory().getClassType(className))
        .orElseThrow()
        .getMethods()
        .stream()
        .filter(m -> m.getName().equals(methodName))
        .findFirst()
        .orElseThrow();
  }

  private static Stmt firstCallIn(JimpleBasedInterproceduralCFG icfg, SootMethod m) {
    return m.getBody().getStmts().stream().filter(icfg::isCallStmt).findFirst().orElseThrow();
  }

  private static Stmt callTo(JimpleBasedInterproceduralCFG icfg, SootMethod m, String calleeName) {
    return m.getBody().getStmts().stream()
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

  private static Stmt handlerIn(SootMethod m) {
    return m.getBody().getStmts().stream()
        .filter(
            s ->
                s instanceof JIdentityStmt
                    && ((JIdentityStmt) s).getRightOp() instanceof JCaughtExceptionRef)
        .findFirst()
        .orElseThrow();
  }

  private static Set<String> declaringClasses(Collection<SootMethod> methods) {
    return methods.stream()
        .map(m -> m.getDeclaringClassType().getClassName())
        .collect(Collectors.toSet());
  }

  private static JimpleBasedInterproceduralCFG icfg(CallGraph cg, boolean enableExceptions) {
    return new JimpleBasedInterproceduralCFG(cg, view, enableExceptions, false);
  }

  private static CallGraph cha() {
    return new ClassHierarchyAnalysisAlgorithm(view).initialize(entryPoints);
  }

  private static CallGraph rta() {
    return new RapidTypeAnalysisAlgorithm(view).initialize(entryPoints);
  }

  @Test
  void virtualCallCalleesFollowTheCallGraph() {
    SootMethod caller = method("ICFGDispatchExample", "virtualCall");

    JimpleBasedInterproceduralCFG chaIcfg = icfg(cha(), false);
    Stmt call = callTo(chaIcfg, caller, "id");
    assertEquals(
        new HashSet<>(Arrays.asList("ICFGDispatchSubA", "ICFGDispatchSubB")),
        declaringClasses(chaIcfg.getCalleesOfCallAt(call)));

    JimpleBasedInterproceduralCFG rtaIcfg = icfg(rta(), false);
    call = callTo(rtaIcfg, caller, "id");
    assertEquals(
        Collections.singleton("ICFGDispatchSubA"),
        declaringClasses(rtaIcfg.getCalleesOfCallAt(call)));
  }

  @Test
  void interfaceCallCalleesFollowTheCallGraph() {
    JimpleBasedInterproceduralCFG icfg = icfg(rta(), false);
    Stmt call = callTo(icfg, method("ICFGDispatchExample", "interfaceCall"), "run");
    assertEquals(
        Collections.singleton("ICFGDispatchIfaceImpl"),
        declaringClasses(icfg.getCalleesOfCallAt(call)));
  }

  @Test
  void calleesOfNonCallStatementsAreEmpty() {
    JimpleBasedInterproceduralCFG icfg = icfg(cha(), false);
    for (Stmt stmt : method("ICFGDispatchExample", "branch").getBody().getStmts()) {
      if (!icfg.isCallStmt(stmt)) {
        assertTrue(icfg.getCalleesOfCallAt(stmt).isEmpty(), stmt.toString());
      }
    }
  }

  @Test
  void exceptionalEdgesOnlyWhenEnabled() {
    SootMethod tryCatch = method("ICFGDispatchExample", "tryCatch");
    Stmt handler = handlerIn(tryCatch);

    JimpleBasedInterproceduralCFG withExceptions = icfg(cha(), true);
    Stmt call = firstCallIn(withExceptions, tryCatch);
    assertTrue(withExceptions.getSuccsOf(call).contains(handler));
    assertTrue(withExceptions.getReturnSitesOfCallAt(call).contains(handler));
    assertTrue(withExceptions.getPredsOf(handler).contains(call));

    JimpleBasedInterproceduralCFG withoutExceptions = icfg(cha(), false);
    call = firstCallIn(withoutExceptions, tryCatch);
    assertFalse(withoutExceptions.getSuccsOf(call).contains(handler));
    assertFalse(withoutExceptions.getReturnSitesOfCallAt(call).contains(handler));
    assertFalse(withoutExceptions.getPredsOf(handler).contains(call));
  }

  @Test
  void startPointsAreTheHeadsOfTheExposedGraph() {
    SootMethod tryCatch = method("ICFGDispatchExample", "tryCatch");
    Stmt handler = handlerIn(tryCatch);
    Stmt first = tryCatch.getBody().getControlFlowGraph().getStartingStmt();

    // with exceptional edges the handler is reachable, so it is not a start point
    JimpleBasedInterproceduralCFG withExceptions = icfg(cha(), true);
    assertEquals(
        Collections.singletonList(first), toList(withExceptions.getStartPointsOf(tryCatch)));
    assertFalse(withExceptions.isStartPoint(handler));

    // without exceptional edges the handler has no predecessor and stays a head (as in Soot)
    JimpleBasedInterproceduralCFG withoutExceptions = icfg(cha(), false);
    assertTrue(withoutExceptions.getStartPointsOf(tryCatch).contains(first));
    assertTrue(withoutExceptions.getStartPointsOf(tryCatch).contains(handler));
  }

  @Test
  void throwStatementsRemainExitsWithAndWithoutExceptionalEdges() {
    // Soot's ExceptionalUnitGraph keeps a throw statement as a tail unless every exception it may
    // throw (including the implicit NullPointerException) is caught, so a throw caught locally by a
    // specific handler is still an exit statement.
    SootMethod m = method("ICFGDispatchExample", "throwCaughtLocally");
    Stmt throwStmt =
        m.getBody().getStmts().stream()
            .filter(s -> s instanceof JThrowStmt)
            .findFirst()
            .orElseThrow();
    for (boolean enableExceptions : new boolean[] {true, false}) {
      JimpleBasedInterproceduralCFG icfg = icfg(cha(), enableExceptions);
      assertTrue(icfg.isExitStmt(throwStmt));
      assertTrue(icfg.getEndPointsOf(m).contains(throwStmt));
    }
  }

  @Test
  void branchTargetExcludesFallThroughSuccessor() {
    SootMethod branch = method("ICFGDispatchExample", "branch");
    JimpleBasedInterproceduralCFG icfg = icfg(cha(), false);
    Stmt ifStmt =
        branch.getBody().getStmts().stream()
            .filter(s -> s instanceof JIfStmt)
            .findFirst()
            .orElseThrow();
    List<Stmt> succs = icfg.getSuccsOf(ifStmt);
    assertEquals(2, succs.size());

    Stmt fallThrough = succs.get(0);
    Stmt target = succs.get(1);
    assertTrue(icfg.isFallThroughSuccessor(ifStmt, fallThrough));
    assertFalse(icfg.isFallThroughSuccessor(ifStmt, target));
    assertFalse(icfg.isBranchTarget(ifStmt, fallThrough));
    assertTrue(icfg.isBranchTarget(ifStmt, target));
  }

  @Test
  void backwardsICFGMirrorsExceptionalEdges() {
    SootMethod tryCatch = method("ICFGDispatchExample", "tryCatch");
    Stmt handler = handlerIn(tryCatch);
    JimpleBasedInterproceduralCFG forward = icfg(cha(), true);
    BackwardsInterproceduralCFG backward = new BackwardsInterproceduralCFG(forward);
    Stmt call = firstCallIn(forward, tryCatch);

    assertTrue(backward.getPredsOf(call).contains(handler));
    assertTrue(backward.getSuccsOf(handler).contains(call));
    assertEquals(
        new HashSet<>(forward.getEndPointsOf(tryCatch)),
        new HashSet<>(backward.getStartPointsOf(tryCatch)));
  }

  private static List<Stmt> toList(Collection<Stmt> stmts) {
    return new ArrayList<>(stmts);
  }
}
