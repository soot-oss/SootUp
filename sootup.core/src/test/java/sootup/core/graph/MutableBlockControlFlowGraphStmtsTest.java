package sootup.core.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JLeExpr;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.Body;
import sootup.core.signatures.PackageName;
import sootup.core.signatures.SignatureInterner;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;

/**
 * {@link MutableBlockControlFlowGraph#getStmts()} computes the order once until the graph changes
 */
public class MutableBlockControlFlowGraphStmtsTest {

  private static final StmtPositionInfo POS = StmtPositionInfo.getNoStmtPositionInfo();

  private final ClassType throwable =
      new ClassType() {
        @Override
        public String getFullyQualifiedName() {
          return getPackageName() + "." + getClassName();
        }

        @Override
        public String getClassName() {
          return "Throwable";
        }

        @Override
        public PackageName getPackageName() {
          return SignatureInterner.getPackageName("java.lang");
        }
      };

  /** Counts the walks of {@link #iterator()}, which compute the statement order */
  private static class CountingGraph extends MutableBlockControlFlowGraph {
    int walks = 0;

    @Override
    public Iterator<Stmt> iterator() {
      walks++;
      return super.iterator();
    }
  }

  private static void assertInIteratorOrder(MutableBlockControlFlowGraph graph) {
    List<Stmt> walked = new ArrayList<>();
    graph.iterator().forEachRemaining(walked::add);
    assertEquals(walked, graph.getStmts());
  }

  @Test
  void theOrderIsComputedOnceUntilTheGraphChanges() {
    JNopStmt a = new JNopStmt(POS);
    JNopStmt b = new JNopStmt(POS);
    JNopStmt c = new JNopStmt(POS);
    JReturnVoidStmt r = new JReturnVoidStmt(POS);
    CountingGraph graph = new CountingGraph();
    graph.setStartingStmt(a);
    graph.putEdge(a, b);
    graph.putEdge(b, r);
    graph.walks = 0;

    List<Stmt> first = graph.getStmts();
    List<Stmt> second = graph.getStmts();

    assertEquals(1, graph.walks);
    assertEquals(List.of(a, b, r), first);
    assertEquals(first, second);
    // each call returns a list of its own, as before
    assertNotSame(first, second);
    first.clear();
    assertEquals(List.of(a, b, r), graph.getStmts());

    graph.insertBefore(r, c);

    assertEquals(List.of(a, b, c, r), graph.getStmts());
    assertEquals(2, graph.walks);
  }

  @Test
  void theOrderFollowsEveryChange() {
    JNopStmt a = new JNopStmt(POS);
    BranchingStmt branch =
        Jimple.newIfStmt(new JLeExpr(IntConstant.getInstance(2), IntConstant.getInstance(3)), POS);
    JNopStmt b = new JNopStmt(POS);
    JNopStmt c = new JNopStmt(POS);
    JNopStmt c2 = new JNopStmt(POS);
    JReturnVoidStmt r = new JReturnVoidStmt(POS);
    JIdentityStmt handler =
        new JIdentityStmt(
            Jimple.newLocal("ex", throwable), new JCaughtExceptionRef(throwable), POS);
    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();

    graph.setStartingStmt(a);
    graph.putEdge(a, branch);
    graph.setEdges(branch, List.of(b, r));
    graph.putEdge(b, r);
    assertInIteratorOrder(graph);

    graph.insertBefore(b, c);
    assertInIteratorOrder(graph);

    graph.replaceNode(c, c2);
    assertInIteratorOrder(graph);

    graph.removeNode(c2);
    assertInIteratorOrder(graph);

    graph.addExceptionalEdge(b, throwable, handler);
    graph.putEdge(handler, r);
    assertInIteratorOrder(graph);

    graph.removeExceptionalEdge(b, throwable);
    assertInIteratorOrder(graph);

    graph.setEdges(branch, List.of(r, b));
    assertInIteratorOrder(graph);

    graph.removeEdge(b, r);
    assertInIteratorOrder(graph);
  }

  @Test
  void aBodyUsesTheOrderOfItsGraph() {
    JNopStmt a = new JNopStmt(POS);
    JReturnVoidStmt r = new JReturnVoidStmt(POS);
    CountingGraph graph = new CountingGraph();
    graph.setStartingStmt(a);
    graph.putEdge(a, r);
    Body body =
        Body.builder(graph)
            .setMethodSignature(
                SignatureInterner.getMethodSignature(
                    throwable,
                    SignatureInterner.getMethodSubSignature(
                        "m", VoidType.getInstance(), Collections.emptyList())))
            .build();
    graph.walks = 0;

    assertEquals(List.of(a, r), body.getStmts());
    assertEquals(List.of(a, r), body.getStmts());
    assertEquals(List.of(a, r), body.getControlFlowGraph().getStmts());

    assertEquals(1, graph.walks);
  }
}
