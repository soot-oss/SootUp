package sootup.java.bytecode.frontend;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.util.*;
import org.junit.jupiter.api.Test;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.ClassFileBasedAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

/**
 * Test that exception handlers that share code do not produce duplicate @caughtexception references
 * on the same execution path. This verifies the fix for the issue where inline exception handler
 * merging could produce a second @caughtexception identity statement reachable from a trap handler
 * via normal control flow.
 */
public class DiagSoot1577Test {

  @Test
  public void testNoDuplicateCaughtExceptionOnPath() {
    AnalysisInputLocation inputLocation =
        new ClassFileBasedAnalysisInputLocation(
            Paths.get("src/test/resources/soot-1577/g.class"),
            "cn.com.chinatelecom.account.api.c",
            SourceType.Application);

    JavaView view = new JavaView(inputLocation);

    MethodSignature sig =
        JavaIdentifierFactory.getInstance()
            .getMethodSignature(
                "cn.com.chinatelecom.account.api.c.g",
                "h",
                "int",
                Arrays.asList("android.content.Context"));
    Body body = view.getMethod(sig).get().getBody();
    ControlFlowGraph<?> cfg = body.getControlFlowGraph();

    // Verify: no @caughtexception identity statement should have a successor path
    // that reaches another @caughtexception identity statement via normal control flow.
    for (Stmt stmt : body.getStmts()) {
      if (isCaughtExceptionStmt(stmt)) {
        // BFS from this caught exception stmt to check if another caught exception stmt
        // is reachable through normal (non-exceptional) control flow
        Set<Stmt> visited = new HashSet<>();
        Queue<Stmt> queue = new LinkedList<>();
        for (Stmt succ : cfg.successors(stmt)) {
          queue.add(succ);
        }
        while (!queue.isEmpty()) {
          Stmt current = queue.poll();
          if (!visited.add(current)) {
            continue;
          }
          assertFalse(
              isCaughtExceptionStmt(current),
              "Found duplicate @caughtexception on execution path: "
                  + stmt
                  + " -> ... -> "
                  + current);
          for (Stmt succ : cfg.successors(current)) {
            queue.add(succ);
          }
        }
      }
    }
  }

  private static boolean isCaughtExceptionStmt(Stmt stmt) {
    return stmt instanceof JIdentityStmt
        && ((JIdentityStmt) stmt).getRightOp() instanceof JCaughtExceptionRef;
  }
}
