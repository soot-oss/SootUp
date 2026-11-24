package sootup.java.bytecode.frontend;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import sootup.analysis.intraprocedural.LocalMustAliasAnalysis;
import sootup.analysis.intraprocedural.StrongLocalMustAliasAnalysis;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

public class LocalMustAliasAnalysisTest {

  // no interceptors, so that the copies of the source code stay in the body
  private final JavaView view =
      new JavaView(
          new JavaClassPathAnalysisInputLocation(
              "src/test/resources/mustalias", SourceType.Application, Collections.emptyList()));

  private Body body(String method, String params) {
    return view.getMethod(
            view.getIdentifierFactory()
                .parseMethodSignature(
                    "<MustAliasTarget: java.lang.Object " + method + "(" + params + ")>"))
        .get()
        .getBody();
  }

  /** The call to sink(..) and the locals passed to it */
  private static Stmt sinkCall(Body body) {
    return body.getStmts().stream()
        .filter(
            s ->
                s.isInvokableStmt()
                    && s.asInvokableStmt()
                        .getInvokeExpr()
                        .map(e -> e.getMethodSignature().getName().equals("sink"))
                        .orElse(false))
        .findFirst()
        .get();
  }

  private static Local arg(Stmt call, int i) {
    Value v = call.asInvokableStmt().getInvokeExpr().get().getArg(i);
    return (Local) v;
  }

  @Test
  public void copiesAliasAndAllocationsDoNot() {
    Body body = body("copies", "");
    Stmt call = sinkCall(body);
    LocalMustAliasAnalysis lmaa = new LocalMustAliasAnalysis(body.getControlFlowGraph(), false);
    Local b = arg(call, 0);
    Local c = arg(call, 1);
    assertTrue(lmaa.mustAlias(b, call, b, call));
    assertFalse(lmaa.mustAlias(b, call, c, call));
    Local a = body.getLocals().stream().filter(l -> l.getName().equals("a")).findFirst().get();
    assertTrue(lmaa.mustAlias(a, call, b, call));
  }

  @Test
  public void aliasesSurviveAMergeOfTheSameValue() {
    Body body = body("merge", "boolean");
    Stmt call = sinkCall(body);
    LocalMustAliasAnalysis lmaa = new LocalMustAliasAnalysis(body.getControlFlowGraph(), false);
    assertTrue(lmaa.mustAlias(arg(call, 0), call, arg(call, 1), call));
  }

  @Test
  public void strongAnalysisDiscardsNumbersFromLoops() {
    Body body = body("loop", "int");
    Stmt call = sinkCall(body);
    Local current = arg(call, 1);
    LocalMustAliasAnalysis strong =
        new StrongLocalMustAliasAnalysis(body.getControlFlowGraph(), false);
    assertFalse(strong.mustAlias(current, call, current, call));
    assertEquals("UNKNOWN", strong.instanceKeyString(current, call).orElse("UNKNOWN"));
    assertFalse(strong.mustAlias(arg(call, 0), call, current, call));
  }

  @Test
  public void plainAnalysisKeepsNumbersFromLoops() {
    Body body = body("loop", "int");
    Stmt call = sinkCall(body);
    Local current = arg(call, 1);
    LocalMustAliasAnalysis plain = new LocalMustAliasAnalysis(body.getControlFlowGraph(), false);
    assertTrue(plain.mustAlias(current, call, current, call));
    assertFalse(plain.instanceKeyString(current, call).orElse("UNKNOWN").equals("UNKNOWN"));
  }
}
