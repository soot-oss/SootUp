package sootup.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableBlockControlFlowGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.PrimitiveType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;

class LocalLivenessAnalyserTest {

  private static final StmtPositionInfo NO_POSITION = StmtPositionInfo.getNoStmtPositionInfo();

  /**
   * Reproduces in-place set mutation bug:
   *
   * <p>Subject pattern: A loop with a condition `i < 10` and an increment `i = i + 1`. In a loop
   * with multiple statements, when propagating backwards across the loop-back edge, an empty `out`
   * set merged with `liveIn(loopHeader)` returned the exact `liveIn(loopHeader)` set reference.
   * When `increment` subsequently removed `def(i)`, it mutated `liveIn(loopHeader)` in-place,
   * incorrectly dropping `i` from the loopHeader's live-in set!
   */
  @Test
  void testCyclicControlFlowWithMergeDoesNotMutatePredecessorInPlace() {
    Local i = JavaJimple.newLocal("i", PrimitiveType.IntType.getInstance());
    Local j = JavaJimple.newLocal("j", PrimitiveType.IntType.getInstance());

    var initI = JavaJimple.newAssignStmt(i, IntConstant.getInstance(0), NO_POSITION);
    var loopHeader = new JIfStmt(JavaJimple.newLtExpr(i, IntConstant.getInstance(10)), NO_POSITION);
    var body =
        JavaJimple.newAssignStmt(
            j, JavaJimple.newAddExpr(i, IntConstant.getInstance(1)), NO_POSITION);
    var increment =
        JavaJimple.newAssignStmt(
            i, JavaJimple.newAddExpr(i, IntConstant.getInstance(1)), NO_POSITION);
    var loopBack = JavaJimple.newGotoStmt(NO_POSITION);
    var ret = JavaJimple.newReturnVoidStmt(NO_POSITION);

    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    graph.addBlock(Collections.singletonList(initI));
    graph.addBlock(Collections.singletonList(loopHeader));
    graph.addBlock(Collections.singletonList(body));
    graph.addBlock(Collections.singletonList(increment));
    graph.addBlock(Collections.singletonList(loopBack));
    graph.addBlock(Collections.singletonList(ret));

    graph.putEdge(initI, loopHeader);
    graph.putEdge(loopHeader, JIfStmt.TRUE_BRANCH_IDX, body);
    graph.putEdge(loopHeader, JIfStmt.FALSE_BRANCH_IDX, ret);
    graph.putEdge(body, increment);
    graph.putEdge(increment, loopBack);
    graph.putEdge(loopBack, 0, loopHeader);
    graph.setStartingStmt(initI);

    LocalLivenessAnalyser analyser = new LocalLivenessAnalyser(graph);
    assertTrue(analyser.getLiveLocalsBeforeStmt(loopHeader).contains(i));
  }

  @Test
  void propagatesLivenessAcrossExceptionalEdges() {
    JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
    var objectType = factory.getClassType("java.lang.Object");
    var throwableType = factory.getClassType("java.lang.Throwable");
    var sourceType = factory.getClassType("example.Source");
    Local value = JavaJimple.newLocal("value", objectType);
    Local caught = JavaJimple.newLocal("caught", throwableType);

    MethodSignature read =
        new MethodSignature(sourceType, "read", Collections.emptyList(), objectType);
    var throwingStmt =
        JavaJimple.newAssignStmt(value, Jimple.newStaticInvokeExpr(read), NO_POSITION);
    var normalReturn = JavaJimple.newReturnVoidStmt(NO_POSITION);
    var handler =
        JavaJimple.newIdentityStmt(caught, JavaJimple.newCaughtExceptionRef(), NO_POSITION);
    var exceptionalReturn = JavaJimple.newReturnStmt(value, NO_POSITION);

    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    graph.addBlock(Collections.singletonList(throwingStmt));
    graph.addBlock(Collections.singletonList(normalReturn));
    graph.addBlock(Collections.singletonList(handler));
    graph.addBlock(Collections.singletonList(exceptionalReturn));
    graph.addExceptionalEdge(throwingStmt, throwableType, handler);
    graph.putEdge(throwingStmt, normalReturn);
    graph.putEdge(handler, exceptionalReturn);
    graph.setStartingStmt(throwingStmt);

    LocalLivenessAnalyser analyser = new LocalLivenessAnalyser(graph);
    assertEquals(Collections.singleton(value), analyser.getLiveLocalsAfterStmt(throwingStmt));
    assertEquals(Collections.singleton(value), analyser.getLiveLocalsBeforeStmt(exceptionalReturn));
  }
}
