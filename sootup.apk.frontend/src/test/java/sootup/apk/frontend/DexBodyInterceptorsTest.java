package sootup.apk.frontend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableBlockControlFlowGraph;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.UnknownType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

/** Tests the default body interceptors of the dex frontend. */
class DexBodyInterceptorsTest {

  private static final StmtPositionInfo POS = StmtPositionInfo.getNoStmtPositionInfo();
  private static final JavaIdentifierFactory FACTORY = new JavaIdentifierFactory();

  /**
   * Dex code reuses registers for values of different types. A register that holds a boolean and
   * later the null that is passed for an object stays a boolean where it holds the boolean: the
   * locals are split before the zeros that are used as objects are replaced with null.
   */
  @Test
  void registerThatHoldsABooleanAndLaterNull() {
    MethodSignature test = FACTORY.parseMethodSignature("<example.Test: boolean test()>");
    MethodSignature use =
        FACTORY.parseMethodSignature("<example.Test: void use(java.lang.Object)>");
    // r = test(); if r == 0 goto zero; nop; zero: r = 0; use(r); return;
    Local r = Jimple.newLocal("r", UnknownType.getInstance());
    FallsThroughStmt call = Jimple.newAssignStmt(r, Jimple.newStaticInvokeExpr(test), POS);
    JIfStmt check = Jimple.newIfStmt(Jimple.newEqExpr(r, IntConstant.getInstance(0)), POS);
    FallsThroughStmt nop = Jimple.newNopStmt(POS);
    FallsThroughStmt zero = Jimple.newAssignStmt(r, IntConstant.getInstance(0), POS);
    FallsThroughStmt useNull = Jimple.newInvokeStmt(Jimple.newStaticInvokeExpr(use, r), POS);
    Stmt ret = Jimple.newReturnVoidStmt(POS);
    var graph = new MutableBlockControlFlowGraph();
    graph.putEdge(call, check);
    graph.putEdge(check, JIfStmt.FALSE_BRANCH_IDX, nop);
    graph.putEdge(check, JIfStmt.TRUE_BRANCH_IDX, zero);
    graph.putEdge(nop, zero);
    graph.putEdge(zero, useNull);
    graph.putEdge(useNull, ret);
    graph.setStartingStmt(call);
    var builder =
        Body.builder(graph)
            .setLocals(new LinkedHashSet<>(List.of(r)))
            .setMethodSignature(
                FACTORY.getMethodSignature(
                    FACTORY.getClassType("example.Test"),
                    "run",
                    FACTORY.getType("void"),
                    List.of()));

    JavaView view = new JavaView(Collections.emptyList());
    for (BodyInterceptor interceptor : DexBodyInterceptors.Default.bodyInterceptors()) {
      interceptor.interceptBody(builder, view);
    }

    List<Stmt> stmts = builder.getStmts();
    JIfStmt transformedCheck =
        (JIfStmt) stmts.stream().filter(s -> s instanceof JIfStmt).findFirst().get();
    assertEquals(IntConstant.getInstance(0), transformedCheck.getCondition().getOp2());
    JAssignStmt transformedZero =
        (JAssignStmt)
            stmts.stream()
                .filter(
                    s ->
                        s instanceof JAssignStmt a
                            && !(a.getRightOp() instanceof AbstractInvokeExpr))
                .findFirst()
                .get();
    assertEquals(NullConstant.getInstance(), transformedZero.getRightOp());
  }
}
