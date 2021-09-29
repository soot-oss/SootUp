package de.upb.swt.soot.test.java.bytecode.interceptors;

import static org.junit.Assert.assertEquals;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.NoPositionInformation;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.expr.JAddExpr;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.bytecode.interceptors.Aggregator;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Ignore;
import org.junit.Test;

public class AggregatorTest {

  /**
   * Tests the correct aggregation. Transforms from
   *
   * <p>a = 7; i = 0; b = a + 4; return;
   *
   * <p>to
   *
   * <p>i = 0; b = 7 + 4; return;
   */
  @Test
  @Ignore("FIX ME")
  public void testAggregation() {
    Body.BodyBuilder testBuilder = createBody(true);
    Body testBody = testBuilder.build();
    new Aggregator().interceptBody(testBuilder);
    Body processedBody = testBuilder.build();
    List<Stmt> originalStmts = testBody.getStmts();
    List<Stmt> processedStmts = processedBody.getStmts();
    System.out.println("new");
    processedStmts.forEach(System.out::println);
    System.out.println("old");
    originalStmts.forEach(System.out::println);

    System.out.println(processedBody.getStmtGraph().getStartingStmt());

    assertEquals(originalStmts.size() - 1, processedStmts.size());
    assertEquals("b = a + 4", originalStmts.get(3).toString());
    assertEquals("b = 7 + 4", processedStmts.get(2).toString());
    assertEquals(originalStmts.get(4), processedStmts.get(3));
  }

  /**
   * Tests the correct handling of a builder without any aggregation. Considers the following code,
   * but does not change anything:
   *
   * <p>a = 7; b = 42; return;
   */
  @Test
  public void testNoAggregation() {
    Body.BodyBuilder testBuilder = createBody(false);
    Body testBody = testBuilder.build();
    new Aggregator().interceptBody(testBuilder);
    Body processedBody = testBuilder.build();
    List<Stmt> originalStmts = testBody.getStmts();
    List<Stmt> processedStmts = processedBody.getStmts();

    assertEquals(originalStmts.size(), processedStmts.size());
    for (int i = 0; i < processedStmts.size(); i++) {
      assertEquals(originalStmts.get(i).toString(), processedStmts.get(i).toString());
    }
  }

  private static Body.BodyBuilder createBody(boolean withAggregation) {
    StmtPositionInfo noPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

    Local i = JavaJimple.newLocal("i", PrimitiveType.getInt());
    Local a = JavaJimple.newLocal("a", PrimitiveType.getInt());
    Local b = JavaJimple.newLocal("b", PrimitiveType.getInt());

    Stmt intToI1 = JavaJimple.newAssignStmt(i, IntConstant.getInstance(1), noPositionInfo);
    Stmt intToI2 = JavaJimple.newAssignStmt(i, IntConstant.getInstance(2), noPositionInfo);
    Stmt intToI3 = JavaJimple.newAssignStmt(i, IntConstant.getInstance(3), noPositionInfo);
    Stmt intToA = JavaJimple.newAssignStmt(a, IntConstant.getInstance(7), noPositionInfo);
    Stmt intToB;
    if (withAggregation) {
      intToB =
          JavaJimple.newAssignStmt(b, new JAddExpr(a, IntConstant.getInstance(4)), noPositionInfo);
    } else {
      intToB = JavaJimple.newAssignStmt(b, IntConstant.getInstance(42), noPositionInfo);
    }
    Stmt ret = JavaJimple.newReturnVoidStmt(noPositionInfo);

    Set<Local> locals = ImmutableUtils.immutableSet(a, i, b);

    List<Trap> traps = new ArrayList<>();

    Body.BodyBuilder builder = Body.builder();
    builder.setStartingStmt(intToI1);
    builder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("test", "ab.c", "void", Collections.emptyList()));

    builder.addFlow(intToI1, intToA);
    builder.addFlow(intToA, intToI2);
    builder.addFlow(intToI2, intToB);
    builder.addFlow(intToB, intToI3);
    builder.addFlow(intToI3, ret);
    builder.setLocals(locals);
    builder.setTraps(traps);
    builder.setPosition(NoPositionInformation.getInstance());

    return builder;
  }
}
