package sootup.java.bytecode.frontend.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JAddExpr;
import sootup.core.jimple.common.expr.JEqExpr;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.PrimitiveType;
import sootup.core.util.ImmutableUtils;
import sootup.interceptors.ConstantPropagatorAndFolder;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.views.JavaView;

/**
 * @author Marcus Nachtigall
 */
public class ConstantPropagatorAndFolderTest {

  /**
   * Tests the correct folding and propagation. Transforms from
   *
   * <p>a = 3; b = 4; c = a + b; return c;
   *
   * <p>to
   *
   * <p>a = 3; b = 4; c = 7; return 7;
   */
  @Test
  public void testModification() {
    Body.BodyBuilder testBuilder = createBody(true);
    Body testBody = testBuilder.build();

    testBuilder = Body.builder(testBody, testBuilder.getModifiers());
    new ConstantPropagatorAndFolder()
        .interceptBody(testBuilder, new JavaView(Collections.emptyList()));
    Body processedBody = testBuilder.build();
    List<Stmt> originalStmts = testBody.getStmts();
    List<Stmt> processedStmts = processedBody.getStmts();

    assertEquals(originalStmts.size(), processedStmts.size());
    assertEquals(originalStmts.get(0).toString(), processedStmts.get(0).toString());
    assertEquals(originalStmts.get(1).toString(), processedStmts.get(1).toString());
    assertEquals("c = 3 + 4", originalStmts.get(2).toString());
    assertEquals("c = 7", processedStmts.get(2).toString());
    assertEquals("return c", originalStmts.get(3).toString());
    assertEquals("return 7", processedStmts.get(3).toString());
  }

  /**
   * Tests the correct handling of a builder without any propagation or folding. Considers the
   * following code, but does not change anything:
   *
   * <p>a = 3; b = 4; a = 2; return c;
   */
  @Test
  public void testNoModification() {
    Body.BodyBuilder testBuilder = createBody(false);
    Body testBody = testBuilder.build();
    new ConstantPropagatorAndFolder()
        .interceptBody(testBuilder, new JavaView(Collections.emptyList()));
    Body processedBody = testBuilder.build();
    List<Stmt> originalStmts = testBody.getStmts();
    List<Stmt> processedStmts = processedBody.getStmts();

    assertEquals(originalStmts.size(), processedStmts.size());
    for (int i = 0; i < processedStmts.size(); i++) {
      assertEquals(originalStmts.get(i).toString(), processedStmts.get(i).toString());
    }
  }

  /**
   * A constant is only propagated into a return if it is the only definition that reaches the
   * return. In
   *
   * <p>c = 3 + 4; if a == 0 goto l; c = a; l: return c;
   *
   * <p>the constant 7 is not returned if a != 0.
   */
  @Test
  public void testNoPropagationOfConstantRedefinedOnOtherPath() {
    StmtPositionInfo noPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();
    Local a = JavaJimple.newLocal("a", PrimitiveType.getInt());
    Local c = JavaJimple.newLocal("c", PrimitiveType.getInt());

    FallsThroughStmt assignA =
        JavaJimple.newIdentityStmt(
            a, JavaJimple.newParameterRef(PrimitiveType.getInt(), 0), noPositionInfo);
    FallsThroughStmt assignConstant =
        JavaJimple.newAssignStmt(
            c,
            new JAddExpr(IntConstant.getInstance(3), IntConstant.getInstance(4)),
            noPositionInfo);
    BranchingStmt ifStmt =
        JavaJimple.newIfStmt(new JEqExpr(a, IntConstant.getInstance(0)), noPositionInfo);
    FallsThroughStmt assignLocal = JavaJimple.newAssignStmt(c, a, noPositionInfo);
    Stmt ret = JavaJimple.newReturnStmt(c, noPositionInfo);

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(
        new JavaIdentifierFactory()
            .getMethodSignature("ab.c", "test", "int", Collections.singletonList("int")));
    MutableControlFlowGraph graph = builder.getControlFlowGraph();
    graph.setStartingStmt(assignA);
    graph.putEdge(assignA, assignConstant);
    graph.putEdge(assignConstant, ifStmt);
    graph.putEdge(ifStmt, JIfStmt.FALSE_BRANCH_IDX, assignLocal);
    graph.putEdge(ifStmt, JIfStmt.TRUE_BRANCH_IDX, ret);
    graph.putEdge(assignLocal, ret);
    builder.setLocals(ImmutableUtils.immutableSet(a, c));
    builder.setPosition(NoPositionInformation.getInstance());

    new ConstantPropagatorAndFolder().interceptBody(builder, new JavaView(Collections.emptyList()));
    List<String> stmts =
        builder.build().getStmts().stream().map(Object::toString).collect(Collectors.toList());

    assertEquals("c = 7", stmts.get(1));
    assertEquals("return c", stmts.get(stmts.size() - 1));
  }

  private static Body.BodyBuilder createBody(boolean constantFolding) {
    StmtPositionInfo noPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();

    Local a = JavaJimple.newLocal("a", PrimitiveType.getInt());
    Local b = JavaJimple.newLocal("b", PrimitiveType.getInt());
    Local c = JavaJimple.newLocal("c", PrimitiveType.getInt());

    Set<Local> locals = ImmutableUtils.immutableSet(a, b, c);

    FallsThroughStmt assignA =
        JavaJimple.newAssignStmt(a, IntConstant.getInstance(3), noPositionInfo);
    FallsThroughStmt assignB =
        JavaJimple.newAssignStmt(b, IntConstant.getInstance(4), noPositionInfo);
    FallsThroughStmt assignC;
    if (constantFolding) {
      assignC =
          JavaJimple.newAssignStmt(
              c,
              new JAddExpr(IntConstant.getInstance(3), IntConstant.getInstance(4)),
              noPositionInfo);
    } else {
      assignC = JavaJimple.newAssignStmt(a, IntConstant.getInstance(2), noPositionInfo);
    }
    Stmt ret = JavaJimple.newReturnStmt(c, noPositionInfo);

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(
        new JavaIdentifierFactory()
            .getMethodSignature("ab.c", "test", "int", Collections.emptyList()));

    final MutableControlFlowGraph controlFlowGraph = builder.getControlFlowGraph();
    controlFlowGraph.setStartingStmt(assignA);
    controlFlowGraph.putEdge(assignA, assignB);
    controlFlowGraph.putEdge(assignB, assignC);
    controlFlowGraph.putEdge(assignC, ret);

    builder.setLocals(locals);
    builder.setPosition(NoPositionInformation.getInstance());

    return builder;
  }
}
