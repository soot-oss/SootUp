package sootup.apk.frontend.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.apk.frontend.DexBodyInterceptors;
import sootup.core.graph.MutableBlockControlFlowGraph;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.UnknownType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

class DexSharedConstantSplitterTest {

  private static final StmtPositionInfo POS = StmtPositionInfo.getNoStmtPositionInfo();
  private static final JavaIdentifierFactory FACTORY = new JavaIdentifierFactory();
  private static final MethodSignature USE_INT =
      FACTORY.parseMethodSignature("<example.Test: void useInt(int)>");
  private static final MethodSignature USE_OBJECT =
      FACTORY.parseMethodSignature("<example.Test: void useObject(java.lang.Object)>");
  private static final MethodSignature USE_INTS =
      FACTORY.parseMethodSignature("<example.Test: void useInts(int,int)>");

  private static Body.BodyBuilder builder(MutableBlockControlFlowGraph graph, List<Local> locals) {
    return Body.builder(graph)
        .setLocals(new LinkedHashSet<>(locals))
        .setMethodSignature(
            FACTORY.getMethodSignature(
                FACTORY.getClassType("example.Test"), "test", FACTORY.getType("void"), List.of()));
  }

  private static Body.BodyBuilder linear(List<Local> locals, Stmt... stmts) {
    var graph = new MutableBlockControlFlowGraph();
    for (int i = 0; i + 1 < stmts.length; i++) {
      graph.putEdge((FallsThroughStmt) stmts[i], stmts[i + 1]);
    }
    graph.setStartingStmt(stmts[0]);
    return builder(graph, locals);
  }

  private static void run(Body.BodyBuilder builder, List<BodyInterceptor> interceptors) {
    JavaView view = new JavaView(Collections.emptyList());
    interceptors.forEach(interceptor -> interceptor.interceptBody(builder, view));
  }

  private static List<String> stmts(Body.BodyBuilder builder) {
    return builder.getStmts().stream().map(Stmt::toString).collect(Collectors.toList());
  }

  private static Stmt invoke(MethodSignature method, Local... args) {
    return Jimple.newInvokeStmt(Jimple.newStaticInvokeExpr(method, List.of(args)), POS);
  }

  @Test
  void zeroUsedAsIntAndAsObjectGetsOneConstantEach() {
    // l = 0; useInt(l); useObject(l); return;
    Local l = Jimple.newLocal("l", UnknownType.getInstance());
    var builder =
        linear(
            List.of(l),
            Jimple.newAssignStmt(l, IntConstant.getInstance(0), POS),
            invoke(USE_INT, l),
            invoke(USE_OBJECT, l),
            Jimple.newReturnVoidStmt(POS));

    run(builder, DexBodyInterceptors.Transformers.bodyInterceptors());

    assertEquals(
        List.of(
            "l = 0",
            "staticinvoke <example.Test: void useInt(int)>(l)",
            "l#c1 = null",
            "staticinvoke <example.Test: void useObject(java.lang.Object)>(l#c1)",
            "return"),
        stmts(builder));
  }

  @Test
  void statementThatUsesTwoSharedConstants() {
    // a = 0; b = 1; useInts(a, b); useInts(b, a); return;
    Local a = Jimple.newLocal("a", UnknownType.getInstance());
    Local b = Jimple.newLocal("b", UnknownType.getInstance());
    var builder =
        linear(
            List.of(a, b),
            Jimple.newAssignStmt(a, IntConstant.getInstance(0), POS),
            Jimple.newAssignStmt(b, IntConstant.getInstance(1), POS),
            invoke(USE_INTS, a, b),
            invoke(USE_INTS, b, a),
            Jimple.newReturnVoidStmt(POS));

    run(builder, List.of(new DexSharedConstantSplitter()));

    assertEquals(
        List.of(
            "a = 0",
            "b = 1",
            "staticinvoke <example.Test: void useInts(int,int)>(a, b)",
            "a#c1 = 0",
            "b#c1 = 1",
            "staticinvoke <example.Test: void useInts(int,int)>(b#c1, a#c1)",
            "return"),
        stmts(builder));
    assertEquals(4, builder.getLocals().size());
  }

  @Test
  void jumpsToAUseReachItsCopy() {
    // l = 0; c = 1; if c == 0 goto target; useInt(l); target: useObject(l); return;
    Local l = Jimple.newLocal("l", UnknownType.getInstance());
    Local c = Jimple.newLocal("c", UnknownType.getInstance());
    FallsThroughStmt defL = Jimple.newAssignStmt(l, IntConstant.getInstance(0), POS);
    FallsThroughStmt defC = Jimple.newAssignStmt(c, IntConstant.getInstance(1), POS);
    JIfStmt check = Jimple.newIfStmt(Jimple.newEqExpr(c, IntConstant.getInstance(0)), POS);
    FallsThroughStmt useInt = (FallsThroughStmt) invoke(USE_INT, l);
    FallsThroughStmt target = (FallsThroughStmt) invoke(USE_OBJECT, l);
    Stmt ret = Jimple.newReturnVoidStmt(POS);
    var graph = new MutableBlockControlFlowGraph();
    graph.putEdge(defL, defC);
    graph.putEdge(defC, check);
    graph.putEdge(check, JIfStmt.FALSE_BRANCH_IDX, useInt);
    graph.putEdge(check, JIfStmt.TRUE_BRANCH_IDX, target);
    graph.putEdge(useInt, target);
    graph.putEdge(target, ret);
    graph.setStartingStmt(defL);
    var builder = builder(graph, List.of(l, c));

    run(builder, List.of(new DexSharedConstantSplitter()));

    Stmt branch = builder.getStmts().stream().filter(s -> s instanceof JIfStmt).findFirst().get();
    Stmt jumpTarget = builder.getControlFlowGraph().getBranchTargetsOf((JIfStmt) branch).get(0);
    assertEquals("l#c1 = 0", jumpTarget.toString());
    assertEquals(
        "staticinvoke <example.Test: void useObject(java.lang.Object)>(l#c1)",
        builder.getControlFlowGraph().successors(jumpTarget).get(0).toString());
  }

  @Test
  void onlyConstantDefinitionsAreCopied() {
    // l = 0; useInt(l); m = l; useInt(m); useObject(m); return;  -- m is no constant
    Local l = Jimple.newLocal("l", UnknownType.getInstance());
    Local m = Jimple.newLocal("m", UnknownType.getInstance());
    var builder =
        linear(
            List.of(l, m),
            Jimple.newAssignStmt(l, IntConstant.getInstance(0), POS),
            invoke(USE_INT, l),
            Jimple.newAssignStmt(m, l, POS),
            invoke(USE_INT, m),
            invoke(USE_OBJECT, m),
            Jimple.newReturnVoidStmt(POS));
    List<String> before = stmts(builder);

    run(builder, List.of(new DexSharedConstantSplitter()));

    // l has two using statements (useInt and m = l), so the copy goes to the second
    assertEquals(
        List.of(
            before.get(0),
            before.get(1),
            "l#c1 = 0",
            "m = l#c1",
            before.get(3),
            before.get(4),
            before.get(5)),
        stmts(builder));
    assertEquals(3, builder.getLocals().size());
  }
}
