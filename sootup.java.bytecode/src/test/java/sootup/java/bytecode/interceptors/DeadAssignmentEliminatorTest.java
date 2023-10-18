package sootup.java.bytecode.interceptors;

import static org.junit.Assert.assertEquals;

import java.util.*;
import org.junit.Test;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.PrimitiveType;
import sootup.core.util.ImmutableUtils;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;

public class DeadAssignmentEliminatorTest {

  /**
   * <pre>
   *     void test() {
   *       if (10 < 20) {
   *         int a = 42;
   *       }
   *       return;
   *     }
   * </pre>
   *
   * gets simplified to
   *
   * <pre>
   *     void test() {
   *       if (10 < 20) {
   *       }
   *       return;
   *     }
   * </pre>
   *
   * There used to be a bug that would result in an invalid statement graph because the whole block containing
   * `int a = 42;` gets deleted.
   */
  @Test
  public void conditionalToRemovedBlock() {
    StmtPositionInfo noPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

    Local a = JavaJimple.newLocal("a", PrimitiveType.getInt());
    Set<Local> locals = ImmutableUtils.immutableSet(a);

    Stmt conditional = JavaJimple.newIfStmt(JavaJimple.newLtExpr(IntConstant.getInstance(10), IntConstant.getInstance(20)), noPositionInfo);
    Stmt ret = JavaJimple.newReturnVoidStmt(noPositionInfo);
    Stmt intToA = JavaJimple.newAssignStmt(a, IntConstant.getInstance(42), noPositionInfo);

    Body.BodyBuilder builder = Body.builder();
    builder.setStartingStmt(conditional);
    builder.setMethodSignature(
            JavaIdentifierFactory.getInstance()
                    .getMethodSignature("test", "ab.c", "void", Collections.emptyList()));

    builder.setLocals(locals);
    builder.setPosition(NoPositionInformation.getInstance());

    builder.addFlow(conditional, intToA);
    builder.addFlow(conditional, ret);
    builder.addFlow(intToA, ret);

    Body beforeBody = builder.build();
    new DeadAssignmentEliminator().interceptBody(builder, null);
    Body afterBody = builder.build();

    assertEquals(beforeBody.getStmtGraph().getNodes().size() - 1, afterBody.getStmtGraph().getNodes().size());
  }

  @Test
  public void testRemoveDeadAssignment() {
    Body.BodyBuilder testBuilder = createBody(false);
    Body testBody = testBuilder.build();
    new DeadAssignmentEliminator().interceptBody(testBuilder, null);
    Body processedBody = testBuilder.build();
    StmtGraph<?> expectedGraph = testBody.getStmtGraph();
    StmtGraph<?> actualGraph = processedBody.getStmtGraph();

    assertEquals(expectedGraph.getNodes().size() - 1, actualGraph.getNodes().size());
  }

  @Test
  public void testNoModification() {
    Body.BodyBuilder testBuilder = createBody(true);
    Body testBody = testBuilder.build();
    new DeadAssignmentEliminator().interceptBody(testBuilder, null);
    Body processedBody = testBuilder.build();
    StmtGraph<?> expectedGraph = testBody.getStmtGraph();
    StmtGraph<?> actualGraph = processedBody.getStmtGraph();

    assertEquals(expectedGraph.getNodes().size(), actualGraph.getNodes().size());
  }

  private static Body.BodyBuilder createBody(boolean essentialOption) {
    JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
    JavaJimple javaJimple = JavaJimple.getInstance();
    StmtPositionInfo noPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

    JavaClassType objectType = factory.getClassType("java.lang.Object");

    Local a = JavaJimple.newLocal("a", objectType);
    Local b = JavaJimple.newLocal("b", objectType);
    Local c = JavaJimple.newLocal("c", PrimitiveType.getInt());

    Stmt strToA = JavaJimple.newAssignStmt(a, javaJimple.newStringConstant("str"), noPositionInfo);
    Stmt ret = JavaJimple.newReturnStmt(a, noPositionInfo);

    Set<Local> locals = ImmutableUtils.immutableSet(a, b, c);

    Body.BodyBuilder builder = Body.builder();
    builder.setStartingStmt(strToA);
    builder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("test", "ab.c", "void", Collections.emptyList()));

    if (essentialOption) {
      Stmt newToB = JavaJimple.newAssignStmt(b, JavaJimple.newNewExpr(objectType), noPositionInfo);
      builder.addFlow(strToA, newToB);
      builder.addFlow(newToB, ret);
    } else {
      Stmt intToC = JavaJimple.newAssignStmt(c, IntConstant.getInstance(42), noPositionInfo);
      builder.addFlow(strToA, intToC);
      builder.addFlow(intToC, ret);
    }
    builder.setLocals(locals);
    builder.setPosition(NoPositionInformation.getInstance());

    return builder;
  }
}
