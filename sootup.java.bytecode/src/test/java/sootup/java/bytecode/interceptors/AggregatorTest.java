package sootup.java.bytecode.interceptors;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JAddExpr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.PrimitiveType;
import sootup.core.util.ImmutableUtils;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;

public class AggregatorTest {

  /**
   * Tests the correct aggregation. Transforms from
   *
   * <p>a = 7; b = a + 4; return;
   *
   * <p>to
   *
   * <p>b = 7 + 4; return;
   */
  @Test
  public void testAggregation() {
    Body.BodyBuilder testBuilder = createBodyBuilder(true);
    Body testBody = testBuilder.build();
    List<Stmt> originalStmts = testBody.getStmts();

    new Aggregator().interceptBody(testBuilder, null);
    Body processedBody = testBuilder.build();
    List<Stmt> processedStmts = processedBody.getStmts();

    assertEquals(originalStmts.size() - 1, processedStmts.size());
    assertEquals("b = a + 4", originalStmts.get(1).toString());
    assertEquals("b = 7 + 4", processedStmts.get(0).toString());
    assertEquals(originalStmts.get(2), processedStmts.get(1));
  }

  /**
   * Tests the correct handling of a builder without any aggregation. Considers the following code,
   * but does not change anything:
   *
   * <p>a = 7; b = 42; return;
   */
  @Test
  public void testNoAggregation() {
    Body.BodyBuilder testBuilder = createBodyBuilder(false);
    Body testBody = testBuilder.build();
    new Aggregator().interceptBody(testBuilder, null);
    Body processedBody = testBuilder.build();
    List<Stmt> originalStmts = testBody.getStmts();
    List<Stmt> processedStmts = processedBody.getStmts();

    assertEquals(originalStmts.size(), processedStmts.size());
    for (int i = 0; i < processedStmts.size(); i++) {
      assertEquals(originalStmts.get(i).toString(), processedStmts.get(i).toString());
    }
  }

  private static Body.BodyBuilder createBodyBuilder(boolean withAggregation) {
    StmtPositionInfo noPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

    Local a = JavaJimple.newLocal("a", PrimitiveType.getInt());
    Local b = JavaJimple.newLocal("b", PrimitiveType.getInt());

    Stmt intToA = JavaJimple.newAssignStmt(a, IntConstant.getInstance(7), noPositionInfo);
    Stmt intToB;
    if (withAggregation) {
      intToB =
          JavaJimple.newAssignStmt(b, new JAddExpr(a, IntConstant.getInstance(4)), noPositionInfo);
    } else {
      intToB = JavaJimple.newAssignStmt(b, IntConstant.getInstance(42), noPositionInfo);
    }
    Stmt ret = JavaJimple.newReturnVoidStmt(noPositionInfo);

    Set<Local> locals = ImmutableUtils.immutableSet(a, b);

    Body.BodyBuilder builder = Body.builder();
    builder.setStartingStmt(intToA);
    builder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("test", "ab.c", "void", Collections.emptyList()));

    builder.addFlow(intToA, intToB);
    builder.addFlow(intToB, ret);
    builder.setLocals(locals);
    builder.setPosition(NoPositionInformation.getInstance());

    return builder;
  }
}
