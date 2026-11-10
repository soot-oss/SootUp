package sootup.examples.mutatingCFG;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Applies all hardening steps of the docs chapter "Modify a ControlFlowGraph" to {@code
 * Vault.withdraw} while the method is loaded. Every other body passes unchanged.
 */
// --8<-- [start:interceptor]
public class VaultHardeningInterceptor implements BodyInterceptor {

  private static final StmtPositionInfo NO_POS = StmtPositionInfo.getNoStmtPositionInfo();

  @Override
  public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {
    MethodSignature sig = builder.getMethodSignature();
    if (!sig.getDeclClassType().getFullyQualifiedName().equals("Vault")
        || !sig.getName().equals("withdraw")) {
      return;
    }
    MutableControlFlowGraph graph = builder.getControlFlowGraph();
    graph.beginBatch();
    try {
      harden(builder, graph, view);
    } finally {
      graph.endBatch();
    }
  }

  // --8<-- [end:interceptor]

  private static void harden(Body.BodyBuilder builder, MutableControlFlowGraph graph, View view) {
    Local amount = null;
    Stmt firstNonIdentity = null;
    for (Stmt s : graph.getStmts()) {
      if (s instanceof JIdentityStmt) {
        JIdentityStmt id = (JIdentityStmt) s;
        if (id.getRightOp() instanceof JParameterRef
            && ((JParameterRef) id.getRightOp()).getIndex() == 0) {
          amount = id.getLeftOp();
        }
      } else {
        firstNonIdentity = s;
        break;
      }
    }
    if (amount == null || firstNonIdentity == null) {
      return;
    }

    // log the call
    JInvokeStmt logCall =
        Jimple.newInvokeStmt(
            Jimple.newStaticInvokeExpr(sig(view, "<Audit: void log(int)>"), amount), NO_POS);
    graph.insertBefore(firstNonIdentity, logCall);

    // replace the legacy check
    JInvokeStmt legacy = MutatingControlFlowGraphTest.invokeOf(graph, "check");
    graph.replaceNode(
        legacy,
        Jimple.newInvokeStmt(
            Jimple.newStaticInvokeExpr(
                sig(view, "<Secure: void check(int)>"), legacy.getInvokeExpr().get().getArgs()),
            NO_POS));

    // remove the debug output
    JInvokeStmt println = MutatingControlFlowGraphTest.invokeOf(graph, "println");
    Stmt loadSystemOut = graph.predecessors(println).get(0);
    graph.removeNode(println);
    graph.removeNode(loadSystemOut);
    // the local that held System.out is unused now
    Set<Local> locals = new LinkedHashSet<>(builder.getLocals());
    locals.remove(((JAssignStmt) loadSystemOut).getLeftOp());
    builder.setLocals(locals);

    // guard clause
    JIfStmt guard = Jimple.newIfStmt(Jimple.newLeExpr(amount, IntConstant.getInstance(0)), NO_POS);
    JReturnStmt reject = Jimple.newReturnStmt(IntConstant.getInstance(-1), NO_POS);
    Stmt afterLog = graph.successors(logCall).get(0);
    graph.removeEdge(logCall, afterLog);
    graph.putEdge(logCall, guard);
    graph.setEdges(guard, afterLog, reject);

    // trap the debit
    ClassType runtimeException =
        view.getIdentifierFactory().getClassType("java.lang.RuntimeException");
    Local e = Jimple.newLocal("e", runtimeException);
    builder.addLocal(e);
    JIdentityStmt caught =
        Jimple.newIdentityStmt(e, new JCaughtExceptionRef(runtimeException), NO_POS);
    graph.addBlock(
        Arrays.asList(
            caught,
            Jimple.newInvokeStmt(
                Jimple.newStaticInvokeExpr(
                    sig(view, "<Audit: void failed(java.lang.RuntimeException)>"), e),
                NO_POS),
            Jimple.newReturnStmt(IntConstant.getInstance(-1), NO_POS)));
    for (Stmt s = afterLog; !(s instanceof JReturnStmt); ) {
      Stmt following = graph.successors(s).get(0);
      graph.addExceptionalEdge(s, runtimeException, caught);
      s = following;
    }
  }

  private static MethodSignature sig(View view, String signature) {
    return view.getIdentifierFactory().parseMethodSignature(signature);
  }
}
