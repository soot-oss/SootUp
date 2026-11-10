package sootup.examples.mutatingCFG;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.interceptors.BytecodeBodyInterceptors;
import sootup.java.bytecode.frontend.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

/**
 * The running example of the docs chapter "Modify a ControlFlowGraph": hardening {@code
 * Vault.withdraw}. The snippet markers are included into docs/mutable_controlflowgraph.md.
 */
public class MutatingControlFlowGraphTest {

  static final String BINARY = "src/test/resources/MutatingCFG/binary";

  static final StmtPositionInfo NO_POS = StmtPositionInfo.getNoStmtPositionInfo();

  /** the first invoke stmt calling a method named methodName */
  static JInvokeStmt invokeOf(ControlFlowGraph<?> graph, String methodName) {
    for (Stmt stmt : graph.getStmts()) {
      if (stmt instanceof JInvokeStmt
          && ((JInvokeStmt) stmt)
              .getInvokeExpr()
              .get()
              .getMethodSignature()
              .getName()
              .equals(methodName)) {
        return (JInvokeStmt) stmt;
      }
    }
    throw new AssertionError("no call of " + methodName);
  }

  // --8<-- [start:load-view]
  static JavaView loadView(List<BodyInterceptor> extraInterceptors) {
    List<BodyInterceptor> interceptors =
        new ArrayList<>(BytecodeBodyInterceptors.Default.getBodyInterceptors());
    interceptors.addAll(extraInterceptors);
    AnalysisInputLocation inputLocation =
        PathBasedAnalysisInputLocation.create(
            Paths.get(BINARY), SourceType.Application, interceptors);
    return new JavaView(inputLocation);
  }
  // --8<-- [end:load-view]


  @Test
  public void hardenWithdraw() {
    // --8<-- [start:load]
    JavaView view = loadView(Collections.emptyList());
    JavaIdentifierFactory factory = view.getIdentifierFactory();
    MethodSignature withdrawSig = factory.parseMethodSignature("<Vault: int withdraw(int)>");
    SootMethod withdraw = view.getMethod(withdrawSig).get();
    Body body = withdraw.getBody();
    // --8<-- [end:load]
    System.out.println("=== original\n" + body);

    // --8<-- [start:inspect]
    ControlFlowGraph<?> cfg = body.getControlFlowGraph();
    // the stmts in linearized (Jimple) order
    List<Stmt> stmts = cfg.getStmts();
    Stmt first = cfg.getStartingStmt();
    // stmt-level flow
    List<Stmt> next = cfg.successors(first);
    List<Stmt> previous = cfg.predecessors(next.get(0));
    // block-level view: maximal sequences of stmts without inner branching
    Collection<? extends BasicBlock<?>> blocks = cfg.getBlocks();
    BasicBlock<?> blockOfFirst = cfg.getBlockOf(first);
    // --8<-- [end:inspect]
    assertSame(first, stmts.get(0));
    assertEquals(Collections.singletonList(first), previous);
    assertEquals(1, blocks.size()); // straight-line code: a single block
    assertSame(first, blockOfFirst.getHead());

    // --8<-- [start:mutable-copy]
    Body.BodyBuilder builder = Body.builder(body, withdraw.getModifiers());
    MutableControlFlowGraph graph = builder.getControlFlowGraph();
    // --8<-- [end:mutable-copy]
    assertNotSame(cfg, graph);

    // --8<-- [start:insert]
    Local amount = body.getParameterLocal(0);
    MethodSignature auditLog = factory.parseMethodSignature("<Audit: void log(int)>");
    JInvokeStmt logCall =
        Jimple.newInvokeStmt(Jimple.newStaticInvokeExpr(auditLog, amount), NO_POS);
    // before the first "real" stmt, i.e. after this := @this and amount := @parameter0
    Stmt firstNonIdentity = body.getFirstNonIdentityStmt();
    graph.insertBefore(firstNonIdentity, logCall);
    // --8<-- [end:insert]
    assertEquals(Collections.singletonList(firstNonIdentity), graph.successors(logCall));

    // --8<-- [start:replace]
    JInvokeStmt legacyCheck = invokeOf(graph, "check");
    MethodSignature secureCheck = factory.parseMethodSignature("<Secure: void check(int)>");
    JInvokeStmt secureCall =
        Jimple.newInvokeStmt(
            Jimple.newStaticInvokeExpr(secureCheck, legacyCheck.getInvokeExpr().get().getArgs()),
            NO_POS);
    graph.replaceNode(legacyCheck, secureCall);
    // --8<-- [end:replace]
    assertFalse(graph.containsNode(legacyCheck));

    // --8<-- [start:remove]
    JInvokeStmt println = invokeOf(graph, "println");
    // the stmt that loads System.out into a local for the println call
    Stmt loadSystemOut = graph.predecessors(println).get(0);
    graph.removeNode(println); // keeps the flow: predecessors now reach its successor
    graph.removeNode(loadSystemOut);
    // the local that held System.out is unused now
    Set<Local> locals = new LinkedHashSet<>(builder.getLocals());
    locals.remove(((JAssignStmt) loadSystemOut).getLeftOp());
    builder.setLocals(locals);
    // --8<-- [end:remove]
    assertFalse(graph.containsNode(println));
    assertFalse(graph.containsNode(loadSystemOut));
    assertEquals(1, graph.getBlocks().size());

    // --8<-- [start:branch]
    // if amount <= 0 goto reject;  reject: return -1
    JIfStmt guard = Jimple.newIfStmt(Jimple.newLeExpr(amount, IntConstant.getInstance(0)), NO_POS);
    JReturnStmt reject = Jimple.newReturnStmt(IntConstant.getInstance(-1), NO_POS);

    Stmt afterLog = graph.successors(logCall).get(0);
    graph.removeEdge(logCall, afterLog); // cut the fall-through flow
    graph.putEdge(logCall, guard); // logCall falls through into the new guard
    // successor 0 of an if is its fall-through flow, successor 1 its branch target
    graph.putEdge(guard, JIfStmt.FALSE_BRANCH_IDX, afterLog);
    graph.putEdge(guard, JIfStmt.TRUE_BRANCH_IDX, reject);
    // --8<-- [end:branch]
    assertEquals(Arrays.asList(afterLog, reject), graph.successors(guard));
    assertEquals(3, graph.getBlocks().size());

    // --8<-- [start:trap]
    ClassType runtimeException = factory.getClassType("java.lang.RuntimeException");
    MethodSignature auditFailed =
        factory.parseMethodSignature("<Audit: void failed(java.lang.RuntimeException)>");

    // the handler:  e := @caughtexception; Audit.failed(e); return -1
    Local e = Jimple.newLocal("e", runtimeException);
    builder.addLocal(e);
    JIdentityStmt caught =
        Jimple.newIdentityStmt(e, new JCaughtExceptionRef(runtimeException), NO_POS);
    JInvokeStmt reportFailure =
        Jimple.newInvokeStmt(Jimple.newStaticInvokeExpr(auditFailed, e), NO_POS);
    JReturnStmt failed = Jimple.newReturnStmt(IntConstant.getInstance(-1), NO_POS);
    graph.addBlock(Arrays.asList(caught, reportFailure, failed));

    // every stmt between the guard and the return may throw into the handler
    Stmt debit = afterLog;
    while (!(debit instanceof JReturnStmt)) {
      Stmt following = graph.successors(debit).get(0);
      graph.addExceptionalEdge(debit, runtimeException, caught);
      debit = following;
    }
    // --8<-- [end:trap]
    assertEquals(
        Collections.singletonMap(runtimeException, caught), graph.exceptionalSuccessors(afterLog));
    assertTrue(graph.exceptionalSuccessors(guard).isEmpty());

    // --8<-- [start:build]
    Body hardened = builder.build();
    // --8<-- [end:build]
    System.out.println("=== hardened\n" + hardened);
    // the original body is untouched
    assertTrue(body.getControlFlowGraph().containsNode(legacyCheck));
    assertTrue(hardened.toString().contains("Secure: void check(int)"));
    assertFalse(hardened.toString().contains("java.io.PrintStream"));
  }

  @Test
  public void undoTheTrap() {
    JavaView view = loadView(Collections.singletonList(new VaultHardeningInterceptor()));
    Body hardened =
        view.getMethod(
                view.getIdentifierFactory().parseMethodSignature("<Vault: int withdraw(int)>"))
            .get()
            .getBody();
    Body.BodyBuilder builder = Body.builder(hardened, Collections.emptySet());
    MutableControlFlowGraph graph = builder.getControlFlowGraph();
    ClassType runtimeException =
        view.getIdentifierFactory().getClassType("java.lang.RuntimeException");
    Stmt handler =
        graph.getNodes().stream()
            .filter(s -> s instanceof JIdentityStmt)
            .filter(s -> ((JIdentityStmt) s).getRightOp() instanceof JCaughtExceptionRef)
            .findFirst()
            .get();

    // --8<-- [start:undo-trap]
    // drop the exceptional flow of one stmt ...
    Stmt someStmt = graph.exceptionalPredecessors(handler).get(0);
    graph.removeExceptionalEdge(someStmt, runtimeException);
    // ... or all exceptional flows of a stmt
    graph.clearExceptionalEdges(graph.exceptionalPredecessors(handler).get(0));
    // ... or every flow of that type into the handler
    graph.removeExceptionalFlowFromAllBlocks(runtimeException, handler);
    // --8<-- [end:undo-trap]
    assertTrue(graph.exceptionalPredecessors(handler).isEmpty());
    for (Stmt s : graph.getNodes()) {
      assertTrue(graph.exceptionalSuccessors(s).isEmpty());
    }
  }

  @Test
  public void hardenViaInterceptor() {
    // --8<-- [start:register-interceptor]
    JavaView view = loadView(Collections.singletonList(new VaultHardeningInterceptor()));
    // --8<-- [end:register-interceptor]
    Body body =
        view.getMethod(
                view.getIdentifierFactory().parseMethodSignature("<Vault: int withdraw(int)>"))
            .get()
            .getBody();
    System.out.println("=== via interceptor\n" + body);
    String jimple = body.toString();
    assertTrue(jimple.contains("<Audit: void log(int)>"));
    assertTrue(jimple.contains("<Secure: void check(int)>"));
    assertFalse(jimple.contains("<Legacy: void check(int)>"));
    assertFalse(jimple.contains("println"));
    assertFalse(jimple.contains("java.io.PrintStream"), "unused local left");
    assertTrue(jimple.contains("@caughtexception"));
    // other methods pass the interceptor unchanged
    Body ctor =
        view.getMethod(view.getIdentifierFactory().parseMethodSignature("<Vault: void <init>()>"))
            .get()
            .getBody();
    assertFalse(ctor.toString().contains("Audit"));
  }
}
