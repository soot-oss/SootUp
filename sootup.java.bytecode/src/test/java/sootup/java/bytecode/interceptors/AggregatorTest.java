package sootup.java.bytecode.interceptors;

import static org.junit.Assert.assertEquals;

import categories.Java8Test;
import java.nio.file.Paths;
import java.util.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JAddExpr;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.util.ImmutableUtils;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

@Category(Java8Test.class)
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

    new Aggregator().interceptBody(testBuilder, new JavaView(Collections.emptyList()));
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
    new Aggregator().interceptBody(testBuilder, new JavaView(Collections.emptyList()));
    Body processedBody = testBuilder.build();
    List<Stmt> originalStmts = testBody.getStmts();
    List<Stmt> processedStmts = processedBody.getStmts();

    assertEquals(originalStmts.size(), processedStmts.size());
    for (int i = 0; i < processedStmts.size(); i++) {
      assertEquals(originalStmts.get(i).toString(), processedStmts.get(i).toString());
    }
  }

  @Test
  public void noAggregationWithUse() {
    Body.BodyBuilder builder = Body.builder();

    StmtPositionInfo noPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();

    JavaClassType fileType = JavaIdentifierFactory.getInstance().getClassType("File");

    Local a = JavaJimple.newLocal("a", fileType);
    Local b = JavaJimple.newLocal("b", fileType);

    FallsThroughStmt assignA =
        JavaJimple.newAssignStmt(a, JavaJimple.newNewExpr(fileType), noPositionInfo);
    // this use of `a` should prevent the aggregator from changing anything
    FallsThroughStmt useA =
        JavaJimple.newInvokeStmt(
            Jimple.newSpecialInvokeExpr(
                a,
                JavaIdentifierFactory.getInstance().parseMethodSignature("<File: void <init>()>")),
            noPositionInfo);
    FallsThroughStmt assignB = JavaJimple.newAssignStmt(b, a, noPositionInfo);
    Stmt ret = JavaJimple.newReturnVoidStmt(noPositionInfo);
    final MutableStmtGraph stmtGraph = builder.getStmtGraph();

    stmtGraph.setStartingStmt(assignA);
    stmtGraph.putEdge(assignA, useA);
    stmtGraph.putEdge(useA, assignB);
    stmtGraph.putEdge(assignB, ret);

    builder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("test", "ab.c", "void", Collections.emptyList()));

    new Aggregator().interceptBody(builder, new JavaView(Collections.emptyList()));

    // ensure that the assigner doesn't remove any statements
    assertEquals(4, builder.getStmts().size());
  }

  private static Body.BodyBuilder createBodyBuilder(boolean withAggregation) {
    StmtPositionInfo noPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();

    Local a = JavaJimple.newLocal("a", PrimitiveType.getInt());
    Local b = JavaJimple.newLocal("b", PrimitiveType.getInt());

    FallsThroughStmt intToA =
        JavaJimple.newAssignStmt(a, IntConstant.getInstance(7), noPositionInfo);
    FallsThroughStmt intToB;
    if (withAggregation) {
      intToB =
          JavaJimple.newAssignStmt(b, new JAddExpr(a, IntConstant.getInstance(4)), noPositionInfo);
    } else {
      intToB = JavaJimple.newAssignStmt(b, IntConstant.getInstance(42), noPositionInfo);
    }
    Stmt ret = JavaJimple.newReturnVoidStmt(noPositionInfo);

    Set<Local> locals = ImmutableUtils.immutableSet(a, b);

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("ab.c", "test", "void", Collections.emptyList()));
    final MutableStmtGraph stmtGraph = builder.getStmtGraph();
    stmtGraph.setStartingStmt(intToA);
    stmtGraph.putEdge(intToA, intToB);
    stmtGraph.putEdge(intToB, ret);

    builder.setLocals(locals);
    builder.setPosition(NoPositionInformation.getInstance());

    return builder;
  }

  @Test
  public void testResource_Misuse() {

    //     String classPath =
    // "../sootup.tests/src/test/resources/bugs/664_struce-compiled/org/apache";
    String classPath = "../sootup.tests/src/test/resources/interceptor/";
    AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(classPath);

    JavaView view = new JavaView(inputLocation);
    {
      final SootMethod sootMethod =
          view.getMethod(view.getIdentifierFactory().parseMethodSignature("<Misuse: void test()>"))
              .get();

      sootMethod.getBody();
    }
    {
      final SootMethod sootMethod =
          view.getMethod(view.getIdentifierFactory().parseMethodSignature("<Misuse: void test1()>"))
              .get();

      System.out.println(sootMethod.getBody());
    }
  }

  @Test
  public void testIssue739() {

    AnalysisInputLocation inputLocation =
        new PathBasedAnalysisInputLocation.ClassFileBasedAnalysisInputLocation(
            Paths.get("../shared-test-resources/bugfixes/Issue739_Aggregator.class"),
            "",
            SourceType.Application,
            Collections.singletonList(new Aggregator()));

    JavaView view = new JavaView(inputLocation);

    final ClassType classType = view.getIdentifierFactory().getClassType("Issue739_Aggregator");
    Assert.assertTrue(view.getClass(classType).isPresent());

    for (JavaSootMethod javaSootMethod :
        view.getClasses().stream().findFirst().get().getMethods()) {
      final Body body = javaSootMethod.getBody();
    }
  }
}
