package sootup.analysis.interprocedural.icfg;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/**
 * A statement that throws may do so before it has any effect. With exceptions enabled, the handlers
 * of a statement are therefore also successors of its normal predecessors, so the handler is
 * reached with the state before the statement.
 */
class ICFGExceptionalPredecessorEdgesTest {

  private static JavaView view;
  private static CallGraph cg;
  private static SootMethod method;

  @BeforeAll
  static void setup() {
    view =
        new JavaView(
            Collections.singletonList(
                new JavaClassPathAnalysisInputLocation("src/test/resources/icfg/binary")));
    MethodSignature main =
        view.getIdentifierFactory()
            .getMethodSignature(
                "ICFGExceptionalPredecessorExample",
                "main",
                "void",
                Collections.singletonList("java.lang.String[]"));
    cg = new ClassHierarchyAnalysisAlgorithm(view).initialize(Collections.singletonList(main));
    method =
        view
            .getClass(view.getIdentifierFactory().getClassType("ICFGExceptionalPredecessorExample"))
            .orElseThrow()
            .getMethods()
            .stream()
            .filter(m -> m.getName().equals("overwriteInTry"))
            .findFirst()
            .orElseThrow();
  }

  private static JimpleBasedInterproceduralCFG icfg(boolean enableExceptions) {
    return new JimpleBasedInterproceduralCFG(cg, view, enableExceptions, false);
  }

  /** x = arr[0], the statement in the try block that may throw before x is overwritten */
  private static Stmt arrayRead() {
    return method.getBody().getStmts().stream()
        .filter(
            s -> s instanceof JAssignStmt && ((JAssignStmt) s).getRightOp() instanceof JArrayRef)
        .findFirst()
        .orElseThrow();
  }

  private static Stmt handler() {
    return method.getBody().getStmts().stream()
        .filter(
            s ->
                s instanceof JIdentityStmt
                    && ((JIdentityStmt) s).getRightOp() instanceof JCaughtExceptionRef)
        .findFirst()
        .orElseThrow();
  }

  @Test
  void predecessorOfThrowingStmtReachesHandler() {
    JimpleBasedInterproceduralCFG icfg = icfg(true);
    Stmt arrayRead = arrayRead();
    Stmt handler = handler();
    assertTrue(icfg.getSuccsOf(arrayRead).contains(handler));
    for (Stmt pred : icfg.getPredsOf(arrayRead)) {
      assertTrue(icfg.getSuccsOf(pred).contains(handler), pred.toString());
      assertTrue(icfg.getPredsOf(handler).contains(pred), pred.toString());
    }
  }

  @Test
  void handlerReachableWithoutPassingThrowingStmt() {
    // the state before x = arr[0] reaches the handler: there is a path from the start of the
    // method to the handler that does not contain x = arr[0]
    JimpleBasedInterproceduralCFG icfg = icfg(true);
    Stmt arrayRead = arrayRead();
    Set<Stmt> visited = Collections.newSetFromMap(new IdentityHashMap<>());
    Deque<Stmt> worklist = new ArrayDeque<>(icfg.getStartPointsOf(method));
    while (!worklist.isEmpty()) {
      Stmt stmt = worklist.pop();
      if (stmt != arrayRead && visited.add(stmt)) {
        worklist.addAll(icfg.getSuccsOf(stmt));
      }
    }
    assertTrue(visited.contains(handler()));
  }

  @Test
  void noExceptionalEdgesWhenDisabled() {
    JimpleBasedInterproceduralCFG icfg = icfg(false);
    Stmt handler = handler();
    for (Stmt stmt : method.getBody().getStmts()) {
      assertFalse(icfg.getSuccsOf(stmt).contains(handler), stmt.toString());
    }
  }

  @Test
  void successorsAndPredecessorsAreSymmetric() {
    for (boolean enableExceptions : new boolean[] {true, false}) {
      JimpleBasedInterproceduralCFG icfg = icfg(enableExceptions);
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
