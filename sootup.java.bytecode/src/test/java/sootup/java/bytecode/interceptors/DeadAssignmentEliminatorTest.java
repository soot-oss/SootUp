package sootup.java.bytecode.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.PrimitiveType;
import sootup.interceptors.DeadAssignmentEliminator;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

@Tag("Java8")
public class DeadAssignmentEliminatorTest {

  /**
   *
   *
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
   * There used to be a bug that would result in an invalid statement graph because the whole block
   * containing `int a = 42;` gets deleted.
   */
  @Test
  public void conditionalToRemovedBlock() {
    StmtPositionInfo noPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();

    Local a = JavaJimple.newLocal("a", PrimitiveType.getInt());
    Set<Local> locals = Collections.singleton(a);

    BranchingStmt conditional =
        JavaJimple.newIfStmt(
            JavaJimple.newLtExpr(IntConstant.getInstance(10), IntConstant.getInstance(20)),
            noPositionInfo);
    Stmt ret = JavaJimple.newReturnVoidStmt(noPositionInfo);
    FallsThroughStmt intToA =
        JavaJimple.newAssignStmt(a, IntConstant.getInstance(42), noPositionInfo);

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("test", "ab.c", "void", Collections.emptyList()));

    builder.setLocals(locals);
    final MutableStmtGraph stmtGraph = builder.getStmtGraph();

    stmtGraph.setStartingStmt(conditional);
    stmtGraph.putEdge(conditional, JIfStmt.FALSE_BRANCH_IDX, intToA);
    stmtGraph.putEdge(conditional, JIfStmt.TRUE_BRANCH_IDX, ret);
    stmtGraph.putEdge(intToA, ret);

    Body beforeBody = builder.build();
    builder = Body.builder(beforeBody, Collections.emptySet());
    new DeadAssignmentEliminator().interceptBody(builder, new JavaView(Collections.emptyList()));
    Body afterBody = builder.build();

    assertEquals(
        beforeBody.getStmtGraph().getNodes().size() - 1,
        afterBody.getStmtGraph().getNodes().size());
  }

  @Test
  public void testRemoveDeadAssignment() {
    Body.BodyBuilder testBuilder = createBody(false);
    Body testBody = testBuilder.build();

    Body.BodyBuilder builder = Body.builder(testBody, Collections.emptySet());
    new DeadAssignmentEliminator().interceptBody(builder, new JavaView(Collections.emptyList()));
    Body processedBody = builder.build();

    StmtGraph<?> expectedGraph = testBody.getStmtGraph();
    StmtGraph<?> actualGraph = processedBody.getStmtGraph();

    assertEquals(expectedGraph.getNodes().size() - 1, actualGraph.getNodes().size());
  }

  @Test
  public void testNoModification() {
    Body.BodyBuilder testBuilder = createBody(true);
    Body testBody = testBuilder.build();
    new DeadAssignmentEliminator()
        .interceptBody(testBuilder, new JavaView(Collections.emptyList()));
    Body processedBody = testBuilder.build();
    StmtGraph<?> expectedGraph = testBody.getStmtGraph();
    StmtGraph<?> actualGraph = processedBody.getStmtGraph();

    assertEquals(expectedGraph.getNodes().size(), actualGraph.getNodes().size());
  }

  private static Body.BodyBuilder createBody(boolean essentialOption) {
    JavaJimple javaJimple = JavaJimple.getInstance();
    StmtPositionInfo noPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();

    JavaClassType objectType = JavaIdentifierFactory.getInstance().getClassType("java.lang.Object");

    Local a = JavaJimple.newLocal("a", objectType);
    Local b = JavaJimple.newLocal("b", objectType);
    Local c = JavaJimple.newLocal("c", PrimitiveType.getInt());

    FallsThroughStmt strToA =
        JavaJimple.newAssignStmt(a, javaJimple.newStringConstant("str"), noPositionInfo);
    Stmt ret = JavaJimple.newReturnStmt(a, noPositionInfo);

    Set<Local> locals = new LinkedHashSet<>(Arrays.asList(a, b, c));

    Body.BodyBuilder builder = Body.builder();
    final MutableStmtGraph stmtGraph = builder.getStmtGraph();
    stmtGraph.setStartingStmt(strToA);
    builder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("ab.c", "test", "void", Collections.emptyList()));

    if (essentialOption) {
      FallsThroughStmt newToB =
          JavaJimple.newAssignStmt(b, JavaJimple.newNewExpr(objectType), noPositionInfo);
      stmtGraph.putEdge(strToA, newToB);
      stmtGraph.putEdge(newToB, ret);
    } else {
      FallsThroughStmt intToC =
          JavaJimple.newAssignStmt(c, IntConstant.getInstance(42), noPositionInfo);
      stmtGraph.putEdge(strToA, intToC);
      stmtGraph.putEdge(intToC, ret);
    }
    builder.setLocals(locals);
    builder.setPosition(NoPositionInformation.getInstance());

    return builder;
  }
}
