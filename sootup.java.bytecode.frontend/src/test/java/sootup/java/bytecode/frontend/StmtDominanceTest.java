package sootup.java.bytecode.frontend;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.graph.StmtDominance;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

public class StmtDominanceTest {

  private final JavaView view =
      new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/dominance"));

  private ControlFlowGraph<?> graph(String method) {
    return view.getMethod(
            view.getIdentifierFactory()
                .parseMethodSignature("<DominanceTarget: int " + method + "(int)>"))
        .get()
        .getBody()
        .getControlFlowGraph();
  }

  private static <T extends Stmt> List<T> all(ControlFlowGraph<?> g, Class<T> kind) {
    return g.getStmts().stream().filter(kind::isInstance).map(kind::cast).toList();
  }

  @Test
  public void diamondJoinsAtTheReturn() {
    ControlFlowGraph<?> g = graph("diamond");
    JIfStmt branch = all(g, JIfStmt.class).get(0);
    JReturnStmt ret = all(g, JReturnStmt.class).get(0);

    StmtDominance post = StmtDominance.postDominators(g, false);
    assertTrue(post.dominates(ret, branch));
    assertTrue(post.getImmediateDominator(branch).isPresent());

    StmtDominance dom = StmtDominance.dominators(g, false);
    assertTrue(dom.dominates(branch, ret));
    assertTrue(dom.dominates(g.getStartingStmt(), ret));
    assertFalse(dom.getImmediateDominator(g.getStartingStmt()).isPresent());
    assertEquals(
        g.getStartingStmt(), dom.getDominators(ret).get(dom.getDominators(ret).size() - 1));
  }

  @Test
  public void multipleExitsArePostDominatedByTheVirtualExitOnly() {
    ControlFlowGraph<?> g = graph("multipleExits");
    JIfStmt first = all(g, JIfStmt.class).get(0);
    StmtDominance post = StmtDominance.postDominators(g, false);
    // the branches lead to different exits: no statement post-dominates the first branch
    assertFalse(post.getImmediateDominator(first).isPresent());
    for (JReturnStmt ret : all(g, JReturnStmt.class)) {
      assertFalse(post.dominates(ret, first), ret.toString());
      assertFalse(post.getImmediateDominator(ret).isPresent(), ret.toString());
    }
  }

  @Test
  public void exceptionalEdgesConnectTheHandler() {
    ControlFlowGraph<?> g = graph("tryCatch");
    Stmt handler =
        g.getStmts().stream()
            .filter(
                s ->
                    s instanceof JIdentityStmt
                        && ((JIdentityStmt) s).getRightOp() instanceof JCaughtExceptionRef)
            .findFirst()
            .get();
    Stmt start = g.getStartingStmt();

    assertTrue(StmtDominance.dominators(g, true).dominates(start, handler));
    // without exceptional edges the handler is a head of its own
    assertFalse(StmtDominance.dominators(g, false).dominates(start, handler));
  }

  @Test
  public void statementsThatCannotReachAnExitHaveNoPostDominators() {
    ControlFlowGraph<?> g = graph("spin");
    JIfStmt branch = all(g, JIfStmt.class).get(0);
    JReturnStmt ret = all(g, JReturnStmt.class).get(0);
    JGotoStmt loop = all(g, JGotoStmt.class).get(0);

    StmtDominance post = StmtDominance.postDominators(g, false);
    // the infinite loop never reaches an exit
    assertTrue(post.getDominators(loop).isEmpty());
    assertFalse(post.getImmediateDominator(loop).isPresent());
    // the only exit of the branch is the return
    assertEquals(ret, post.getImmediateDominator(branch).get());
    // the loop is reached from the start as usual
    assertTrue(StmtDominance.dominators(g, false).dominates(branch, loop));
  }
}
