package sootup.apk.frontend.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableBlockControlFlowGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.Type;
import sootup.core.types.UnknownType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

/** Tests that {@link DexNullTransformer} replaces the zeros that dex code uses as null. */
class DexNullTransformerTest {

  private static final StmtPositionInfo POS = StmtPositionInfo.getNoStmtPositionInfo();
  private static final JavaIdentifierFactory FACTORY = new JavaIdentifierFactory();
  private static final MethodSignature USE_OBJECT =
      FACTORY.parseMethodSignature("<example.Test: void use(java.lang.Object)>");
  private static final MethodSignature USE_OBJECT_AND_INT =
      FACTORY.parseMethodSignature("<example.Test: void use(java.lang.Object,int)>");

  private static Body.BodyBuilder builder(String returnType, List<Local> locals, Stmt... stmts) {
    var graph = new MutableBlockControlFlowGraph();
    for (int i = 0; i + 1 < stmts.length; i++) {
      graph.putEdge((FallsThroughStmt) stmts[i], stmts[i + 1]);
    }
    if (stmts.length == 1) {
      graph.addNode(stmts[0]);
    }
    graph.setStartingStmt(stmts[0]);
    Type ret = FACTORY.getType(returnType);
    return Body.builder(graph)
        .setLocals(new LinkedHashSet<>(locals))
        .setMethodSignature(
            FACTORY.getMethodSignature(
                FACTORY.getClassType("example.Test"), "test", ret, List.of()));
  }

  private static void transform(Body.BodyBuilder builder) {
    new DexNullTransformer().interceptBody(builder, new JavaView(Collections.emptyList()));
  }

  @Test
  void zeroInALocalThatIsUsedAsObject() {
    // l = 0; use(l); return;
    Local l = Jimple.newLocal("l", UnknownType.getInstance());
    var builder =
        builder(
            "void",
            List.of(l),
            Jimple.newAssignStmt(l, IntConstant.getInstance(0), POS),
            Jimple.newInvokeStmt(Jimple.newStaticInvokeExpr(USE_OBJECT, l), POS),
            Jimple.newReturnVoidStmt(POS));

    transform(builder);

    assertEquals(
        NullConstant.getInstance(), ((JAssignStmt) builder.getStmts().get(0)).getRightOp());
  }

  @Test
  void zeroComparisonOfALocalThatIsUsedAsObject() {
    // l = 0; use(l); if l == 0 goto ret; nop; ret: return;
    Local l = Jimple.newLocal("l", UnknownType.getInstance());
    FallsThroughStmt def = Jimple.newAssignStmt(l, IntConstant.getInstance(0), POS);
    FallsThroughStmt use = Jimple.newInvokeStmt(Jimple.newStaticInvokeExpr(USE_OBJECT, l), POS);
    JIfStmt check = Jimple.newIfStmt(Jimple.newEqExpr(l, IntConstant.getInstance(0)), POS);
    FallsThroughStmt nop = Jimple.newNopStmt(POS);
    Stmt ret = Jimple.newReturnVoidStmt(POS);
    var graph = new MutableBlockControlFlowGraph();
    graph.putEdge(def, use);
    graph.putEdge(use, check);
    graph.putEdge(check, JIfStmt.FALSE_BRANCH_IDX, nop);
    graph.putEdge(check, JIfStmt.TRUE_BRANCH_IDX, ret);
    graph.putEdge(nop, ret);
    graph.setStartingStmt(def);
    var builder =
        Body.builder(graph)
            .setLocals(new LinkedHashSet<>(List.of(l)))
            .setMethodSignature(
                FACTORY.getMethodSignature(
                    FACTORY.getClassType("example.Test"),
                    "test",
                    FACTORY.getType("void"),
                    List.of()));

    transform(builder);

    JIfStmt transformed =
        (JIfStmt) builder.getStmts().stream().filter(s -> s instanceof JIfStmt).findFirst().get();
    assertEquals(NullConstant.getInstance(), transformed.getCondition().getOp2());
  }

  @Test
  void inlinedZeroArgumentForAnObjectParameter() {
    // use(0, 0); return;  -- the int argument stays 0
    var builder =
        builder(
            "void",
            List.of(),
            Jimple.newInvokeStmt(
                Jimple.newStaticInvokeExpr(
                    USE_OBJECT_AND_INT,
                    List.of(IntConstant.getInstance(0), IntConstant.getInstance(0))),
                POS),
            Jimple.newReturnVoidStmt(POS));

    transform(builder);

    var args = ((JInvokeStmt) builder.getStmts().get(0)).getInvokeExpr().get().getArgs();
    assertEquals(List.of(NullConstant.getInstance(), IntConstant.getInstance(0)), args);
  }

  @Test
  void returnedZeroOfAnObjectMethod() {
    // return 0;  -- in a method that returns an object
    var builder =
        builder(
            "java.lang.Object", List.of(), Jimple.newReturnStmt(IntConstant.getInstance(0), POS));

    transform(builder);

    assertEquals(NullConstant.getInstance(), ((JReturnStmt) builder.getStmts().get(0)).getOp());
  }
}
