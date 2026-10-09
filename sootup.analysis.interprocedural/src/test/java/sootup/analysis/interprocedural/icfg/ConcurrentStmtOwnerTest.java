package sootup.analysis.interprocedural.icfg;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JNopStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/**
 * Data flow solvers query the ICFG from several threads, and clients register the statements of
 * bodies that become relevant during the analysis (e.g. callbacks into application code) with
 * {@link AbstractJimpleBasedICFG#setOwnerStatement(Stmt, Body)} at the same time.
 */
public class ConcurrentStmtOwnerTest {

  @Test
  public void statementsRegisteredConcurrentlyAreAllKnown() throws Exception {
    JavaView view =
        new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/icfg/binary"));
    SootMethod entry =
        view
            .getClass(view.getIdentifierFactory().getClassType("ICFGExample"))
            .get()
            .getMethods()
            .stream()
            .filter(m -> m.getName().equals("entryPoint"))
            .findFirst()
            .get();
    JimpleBasedInterproceduralCFG icfg =
        new JimpleBasedInterproceduralCFG(
            view, Collections.singletonList(entry.getSignature()), false, false);
    Body body = entry.getBody();

    int threads = 8;
    int perThread = 50_000;
    List<List<Stmt>> stmts = new ArrayList<>();
    for (int t = 0; t < threads; t++) {
      List<Stmt> chunk = new ArrayList<>(perThread);
      for (int i = 0; i < perThread; i++) {
        chunk.add(new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo()));
      }
      stmts.add(chunk);
    }

    ExecutorService executor = Executors.newFixedThreadPool(threads);
    try {
      List<Future<?>> futures = new ArrayList<>();
      for (List<Stmt> chunk : stmts) {
        futures.add(
            executor.submit(
                () -> {
                  for (Stmt stmt : chunk) {
                    icfg.setOwnerStatement(stmt, body);
                    icfg.getBodyOf(stmt);
                  }
                }));
      }
      for (Future<?> future : futures) {
        future.get();
      }
    } finally {
      executor.shutdown();
    }

    for (List<Stmt> chunk : stmts) {
      for (Stmt stmt : chunk) {
        assertSame(body, icfg.getBodyOf(stmt));
      }
    }
  }
}
