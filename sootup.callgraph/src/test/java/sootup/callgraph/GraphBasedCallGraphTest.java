package sootup.callgraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.graph4j.DirectedPseudograph;
import org.graph4j.GraphBuilder;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph.Call;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.JavaIdentifierFactory;

public class GraphBasedCallGraphTest {

  private final JavaIdentifierFactory factory = new JavaIdentifierFactory();
  private final MethodSignature caller = factory.parseMethodSignature("<A: void caller()>");
  private final MethodSignature callee = factory.parseMethodSignature("<B: void callee()>");

  private static InvokableStmt invokeOf(MethodSignature target) {
    return new JInvokeStmt(
        Jimple.newStaticInvokeExpr(target), StmtPositionInfo.getNoStmtPositionInfo());
  }

  @SuppressWarnings("unchecked")
  private static DirectedPseudograph<MethodSignature, Call> spiedGraph() {
    return spy(
        (DirectedPseudograph<MethodSignature, Call>)
            GraphBuilder.empty().buildDirectedPseudograph());
  }

  /** Adding a call looks up the vertex of each of its methods once */
  @Test
  public void addCallLooksUpEachMethodOnce() {
    DirectedPseudograph<MethodSignature, Call> graph = spiedGraph();
    GraphBasedCallGraph cg = new GraphBasedCallGraph(graph, List.of(caller));

    cg.addCall(caller, callee, invokeOf(callee));

    verify(graph, times(1)).findVertex(caller);
    verify(graph, times(1)).findVertex(callee);
    assertTrue(cg.containsMethod(caller));
    assertTrue(cg.containsMethod(callee));
    assertEquals(1, cg.callCount());
  }

  /**
   * Checking for a call does not use graph4j's lookup by edge label, which makes every later added
   * edge update a label-to-edge map
   */
  @Test
  public void containsCallDoesNotLookUpEdgesByLabel() {
    DirectedPseudograph<MethodSignature, Call> graph = spiedGraph();
    GraphBasedCallGraph cg = new GraphBasedCallGraph(graph, List.of(caller));
    InvokableStmt stmt = invokeOf(callee);
    cg.addCall(caller, callee, stmt);

    assertTrue(cg.containsCall(caller, callee, stmt));
    assertTrue(cg.containsCall(new Call(caller, callee, stmt)));
    verify(graph, never()).findEdge(any());
  }

  /** A call is identified by its source, its target and its statement */
  @Test
  public void containsCallMatchesSourceTargetAndStatement() {
    GraphBasedCallGraph cg = new GraphBasedCallGraph(List.of(caller));
    InvokableStmt stmt = invokeOf(callee);
    cg.addCall(caller, callee, stmt);

    assertTrue(cg.containsCall(caller, callee, stmt));
    assertFalse(cg.containsCall(caller, callee, invokeOf(callee)));
    assertFalse(cg.containsCall(callee, caller, stmt));
    MethodSignature unknown = factory.parseMethodSignature("<C: void unknown()>");
    assertFalse(cg.containsCall(caller, unknown, stmt));
  }

  /** A copy contains the calls of the original, and calls added to it stay in the copy */
  @Test
  public void copyContainsTheCallsOfTheOriginal() {
    GraphBasedCallGraph cg = new GraphBasedCallGraph(List.of(caller));
    InvokableStmt stmt = invokeOf(callee);
    cg.addCall(caller, callee, stmt);

    MutableCallGraph copy = cg.copy();
    assertTrue(copy.containsCall(caller, callee, stmt));

    InvokableStmt second = invokeOf(callee);
    copy.addCall(caller, callee, second);
    assertTrue(copy.containsCall(caller, callee, second));
    assertFalse(cg.containsCall(caller, callee, second));
  }
}
