package sootup.java.bytecode.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import categories.TestCategories;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JAddExpr;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.PrimitiveType;
import sootup.core.util.ImmutableUtils;
import sootup.interceptors.ConstantPropagatorAndFolder;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.views.JavaView;

/** @author Marcus Nachtigall */
@Tag(TestCategories.JAVA_8_CATEGORY)
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
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("ab.c", "test", "void", Collections.emptyList()));

    final MutableStmtGraph stmtGraph = builder.getStmtGraph();
    stmtGraph.setStartingStmt(assignA);
    stmtGraph.putEdge(assignA, assignB);
    stmtGraph.putEdge(assignB, assignC);
    stmtGraph.putEdge(assignC, ret);

    builder.setLocals(locals);
    builder.setPosition(NoPositionInformation.getInstance());

    return builder;
  }
}
