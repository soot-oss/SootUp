package sootup.java.bytecode.frontend.interceptors;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.expr.AbstractConditionExpr;
import sootup.core.jimple.common.expr.Expr;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.VoidType;
import sootup.core.util.ImmutableUtils;
import sootup.core.util.Utils;
import sootup.core.views.View;
import sootup.interceptors.CopyPropagator;
import sootup.java.bytecode.frontend.inputlocation.ClassFileBasedAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

/**
 * @author Zun Wang
 */
public class CopyPropagatorTest {

  // Preparation
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();
  JavaClassType intType = factory.getClassType("int");
  JavaClassType refType = factory.getClassType("ref");
  JavaClassType classType = factory.getClassType("Test");
  MethodSignature methodSignature =
      new MethodSignature(classType, "test", Collections.emptyList(), VoidType.getInstance());
  IdentityRef identityRef = JavaJimple.newThisRef(classType);

  // build locals
  Local i1 = JavaJimple.newLocal("i1", intType);
  Local i2 = JavaJimple.newLocal("i2", intType);
  Local i3 = JavaJimple.newLocal("i3", intType);

  Local r0 = JavaJimple.newLocal("r0", refType);
  Local r1 = JavaJimple.newLocal("r1", refType);
  Local r2 = JavaJimple.newLocal("r2", refType);
  Local r3 = JavaJimple.newLocal("r3", refType);
  Local r4 = JavaJimple.newLocal("r4", refType);
  Local r5 = JavaJimple.newLocal("r5", refType);
  Local r6 = JavaJimple.newLocal("r6", refType);

  JCastExpr intCastExpr = JavaJimple.newCastExpr(IntConstant.getInstance(0), refType);

  // build Stmts
  // r0 := @this Test
  FallsThroughStmt startingStmt = JavaJimple.newIdentityStmt(r0, identityRef, noStmtPositionInfo);
  // r1 = new ref
  Expr expr = JavaJimple.newNewExpr(refType);
  FallsThroughStmt stmt1 = JavaJimple.newAssignStmt(r1, expr, noStmtPositionInfo);
  // r2 = r1
  FallsThroughStmt stmt2 = JavaJimple.newAssignStmt(r2, r1, noStmtPositionInfo);
  // r3 = r2
  FallsThroughStmt stmt3 = JavaJimple.newAssignStmt(r3, r2, noStmtPositionInfo);
  // r4 = r3
  FallsThroughStmt stmt4 = JavaJimple.newAssignStmt(r4, r3, noStmtPositionInfo);
  // return
  Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

  // r3 = r1;
  FallsThroughStmt estmt3 = JavaJimple.newAssignStmt(r3, r1, noStmtPositionInfo);
  // r4 = r1
  FallsThroughStmt estmt4 = JavaJimple.newAssignStmt(r4, r1, noStmtPositionInfo);

  // i1 = 5
  FallsThroughStmt stmt5 =
      JavaJimple.newAssignStmt(i1, IntConstant.getInstance(5), noStmtPositionInfo);
  // i2 = 0
  FallsThroughStmt stmt6 =
      JavaJimple.newAssignStmt(i2, IntConstant.getInstance(0), noStmtPositionInfo);
  // if i2 > i1 goto
  AbstractConditionExpr condition = JavaJimple.newGtExpr(i2, i1);
  BranchingStmt ifStmt7 = JavaJimple.newIfStmt(condition, noStmtPositionInfo);
  // i3 = i1 + 1
  Expr add1 = JavaJimple.newAddExpr(i1, IntConstant.getInstance(1));
  FallsThroughStmt stmt8 = JavaJimple.newAssignStmt(i3, add1, noStmtPositionInfo);
  // i2 = i2 + 1
  Expr add2 = JavaJimple.newAddExpr(i2, IntConstant.getInstance(1));
  FallsThroughStmt stmt9 = JavaJimple.newAssignStmt(i2, add2, noStmtPositionInfo);
  BranchingStmt gotoStmt = JavaJimple.newGotoStmt(noStmtPositionInfo);

  // if i2 > 5 goto
  AbstractConditionExpr econdition = JavaJimple.newGtExpr(i2, IntConstant.getInstance(5));
  BranchingStmt eifstmt7 = JavaJimple.newIfStmt(econdition, noStmtPositionInfo);
  // i3 = 5 + 1
  Expr eadd1 = JavaJimple.newAddExpr(IntConstant.getInstance(5), IntConstant.getInstance(1));
  FallsThroughStmt estmt8 = JavaJimple.newAssignStmt(i3, eadd1, noStmtPositionInfo);

  // r0 := @this Test; r1 = (ref) 0; r2 = (ref) 0L; r3 = (ref) 1; r4 = r1, r5 = r2
  // r1 = (ref) 0
  JCastExpr intCast = JavaJimple.newCastExpr(IntConstant.getInstance(0), refType);
  FallsThroughStmt stmt10 = JavaJimple.newAssignStmt(r1, intCast, noStmtPositionInfo);
  // r2 = (ref) 0L
  JCastExpr longCast = JavaJimple.newCastExpr(LongConstant.getInstance(0), refType);
  FallsThroughStmt stmt11 = JavaJimple.newAssignStmt(r2, longCast, noStmtPositionInfo);
  // r3 = (ref) 1
  JCastExpr intCast1 = JavaJimple.newCastExpr(IntConstant.getInstance(1), refType);
  FallsThroughStmt stmt12 = JavaJimple.newAssignStmt(r3, intCast1, noStmtPositionInfo);
  // r5 = r2
  FallsThroughStmt stmt13 = JavaJimple.newAssignStmt(r5, r2, noStmtPositionInfo);
  // r6 = r3
  FallsThroughStmt stmt14 = JavaJimple.newAssignStmt(r6, r3, noStmtPositionInfo);

  JAssignStmt eestmt4 =
      JavaJimple.newAssignStmt(r4, NullConstant.getInstance(), noStmtPositionInfo);
  JAssignStmt estmt13 =
      JavaJimple.newAssignStmt(r5, NullConstant.getInstance(), noStmtPositionInfo);

  public View setUp() {
    String baseDir = "../shared-test-resources/interceptors/";
    JavaClassPathAnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            baseDir, SourceType.Library, Collections.emptyList());
    final JavaView view = new JavaView(Arrays.asList(inputLocation));
    return view;
  }

  @Test
  public void testCopyPropagationWithRedefinition() {
    View view = setUp();
    final MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature(
                "CopyPropagatorTest", "tc1", "void", Collections.singletonList("int"));
    Body bodyBefore = view.getMethod(methodSignature).get().getBody();
    final Body.BodyBuilder builder = Body.builder(bodyBefore, Collections.emptySet());
    new CopyPropagator().interceptBody(builder, view);
    Body bodyAfter = builder.build();
    assertEquals(
        Stream.of(
                "CopyPropagatorTest this",
                "int l1",
                "unknown l2, l3, l4",
                "this := @this: CopyPropagatorTest",
                "l1 := @parameter0: int",
                "l3 = 0",
                "l2 = l1",
                "l1 = 10",
                "l3 = 20",
                // l2 should not be replaced with l1 as l1 gets redefined
                "l4 = l2 + 20",
                "return")
            .collect(Collectors.toList()),
        Utils.filterJimple(bodyAfter.toString()));
  }

  @Test
  public void testEqualStmt() {
    assertTrue(eestmt4.equivTo(eestmt4.withRValue(NullConstant.getInstance())));
  }

  /** Test the copy propagation's chain */
  @Test
  public void testChainBody() {

    Body body = createChainBody();
    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());
    CopyPropagator propagator = new CopyPropagator();
    propagator.interceptBody(builder, new JavaView(Collections.emptyList()));

    Body expectedBody = createExpectedChainBody();
    AssertUtils.assertStmtGraphEquiv(expectedBody, builder.build());
  }

  /** Test the copy propagation for loop */
  @Test
  public void testLoopBody() {

    Body.BodyBuilder builder = createLoopBody();

    CopyPropagator propagator = new CopyPropagator();
    propagator.interceptBody(builder, new JavaView(Collections.emptyList()));

    Body expectedBody = createExpectedLoopBody();
    AssertUtils.assertStmtGraphEquiv(expectedBody, builder.build());
  }

  /** Test the copy propagation for castExpr */
  @Test
  public void testCastExprBody() {

    Body body = createCastExprBody();
    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());
    CopyPropagator propagator = new CopyPropagator();
    propagator.interceptBody(builder, new JavaView(Collections.emptyList()));

    Body expectedBody = createExpectedCastExprBody();
    AssertUtils.assertStmtGraphEquiv(expectedBody, builder.build());
  }

  /** r0 := @this Test; r1 = new ref; r2 = r1; r3 = r2; r4 = r3; return */
  private Body createChainBody() {

    // build an instance of BodyBuilder
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(r0, r1, r2, r3, r4);

    builder.setLocals(locals);
    final MutableStmtGraph stmtGraph = builder.getStmtGraph();

    // build stmtsGraph for the builder
    stmtGraph.putEdge(startingStmt, stmt1);
    stmtGraph.putEdge(stmt1, stmt2);
    stmtGraph.putEdge(stmt2, stmt3);
    stmtGraph.putEdge(stmt3, stmt4);
    stmtGraph.putEdge(stmt4, ret);

    // set startingStmt
    stmtGraph.setStartingStmt(startingStmt);

    // set Position
    builder.setPosition(NoPositionInformation.getInstance());

    return builder.build();
  }

  /** r0 := @this Test; r1 = new ref; r2 = r1; r3 = r1; r4 = r1; return */
  private Body createExpectedChainBody() {

    // build an instance of BodyBuilder
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(r0, r1, r2, r3, r4);

    builder.setLocals(locals);
    final MutableStmtGraph stmtGraph = builder.getStmtGraph();

    // build stmtsGraph for the builder
    stmtGraph.putEdge(startingStmt, stmt1);
    stmtGraph.putEdge(stmt1, stmt2);
    stmtGraph.putEdge(stmt2, estmt3);
    stmtGraph.putEdge(estmt3, estmt4);
    stmtGraph.putEdge(estmt4, ret);

    // set startingStmt
    stmtGraph.setStartingStmt(startingStmt);

    // set Position
    builder.setPosition(NoPositionInformation.getInstance());

    return builder.build();
  }

  /**
   * l0 := @this Test; i1 = 5; i2 = 0; if i2 > i1 goto label2; i3 = i1 + 1; i2 = i2 + 1; goto
   * label1; return
   */
  private Body.BodyBuilder createLoopBody() {

    // build an instance of BodyBuilder
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(r0, i1, i2, i3);

    builder.setLocals(locals);
    final MutableStmtGraph stmtGraph = builder.getStmtGraph();

    // build stmtsGraph for the builder
    stmtGraph.putEdge(startingStmt, stmt5);
    stmtGraph.putEdge(stmt5, stmt6);
    stmtGraph.putEdge(stmt6, ifStmt7);
    stmtGraph.putEdge(ifStmt7, JIfStmt.FALSE_BRANCH_IDX, stmt8);
    stmtGraph.putEdge(stmt8, stmt9);
    stmtGraph.putEdge(stmt9, gotoStmt);
    stmtGraph.putEdge(gotoStmt, JGotoStmt.BRANCH_IDX, ifStmt7);
    stmtGraph.putEdge(ifStmt7, JIfStmt.TRUE_BRANCH_IDX, ret);

    // set startingStmt
    stmtGraph.setStartingStmt(startingStmt);

    return builder;
  }

  /**
   * l0 := @this Test; i1 = 5; i2 = 0; if i2 > 5 goto label2; i3 = 5 + 1; i2 = i2 + 1; goto label1;
   * return
   */
  private Body createExpectedLoopBody() {
    // build an instance of BodyBuilder
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(r0, i1, i2, i3);

    builder.setLocals(locals);
    final MutableStmtGraph stmtGraph = builder.getStmtGraph();
    // build stmtsGraph for the builder
    stmtGraph.putEdge(startingStmt, stmt5);
    stmtGraph.putEdge(stmt5, stmt6);
    stmtGraph.putEdge(stmt6, eifstmt7);
    stmtGraph.putEdge(eifstmt7, JIfStmt.FALSE_BRANCH_IDX, estmt8);
    stmtGraph.putEdge(estmt8, stmt9);
    stmtGraph.putEdge(stmt9, gotoStmt);
    stmtGraph.putEdge(gotoStmt, JGotoStmt.BRANCH_IDX, eifstmt7);
    stmtGraph.putEdge(eifstmt7, JIfStmt.TRUE_BRANCH_IDX, ret);

    // set startingStmt
    stmtGraph.setStartingStmt(startingStmt);

    // set Position
    builder.setPosition(NoPositionInformation.getInstance());

    return builder.build();
  }

  /**
   * r0 := @this Test; r1 = (ref) 0; r2 = (ref) 0L; r3 = (ref) (long) 1; r4 = r1, r5 = r2; r6 = r3;
   */
  private Body createCastExprBody() {

    // build an instance of BodyBuilder
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(r0, r1, r2, r3, r4, r5);

    builder.setLocals(locals);
    final MutableStmtGraph stmtGraph = builder.getStmtGraph();

    // build stmtsGraph for the builder
    stmtGraph.putEdge(startingStmt, stmt10);
    stmtGraph.putEdge(stmt10, stmt11);
    stmtGraph.putEdge(stmt11, stmt12);
    stmtGraph.putEdge(stmt12, estmt4);
    stmtGraph.putEdge(estmt4, stmt13);
    stmtGraph.putEdge(stmt13, stmt14);
    stmtGraph.putEdge(stmt14, ret);

    // set startingStmt
    stmtGraph.setStartingStmt(startingStmt);

    // set Position
    builder.setPosition(NoPositionInformation.getInstance());

    return builder.build();
  }

  /** r0 := @this Test; r1 = (ref) 0; r2 = (ref) 0l; r3 = (ref) 1; r4 = null, r5 = null; r6 = r3; */
  private Body createExpectedCastExprBody() {

    // build an instance of BodyBuilder
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(r0, r1, r2, r3, r4, r5);

    builder.setLocals(locals);
    final MutableStmtGraph stmtGraph = builder.getStmtGraph();

    // build stmtsGraph for the builder
    stmtGraph.putEdge(startingStmt, stmt10);
    stmtGraph.putEdge(stmt10, stmt11);
    stmtGraph.putEdge(stmt11, stmt12);
    stmtGraph.putEdge(stmt12, eestmt4);
    stmtGraph.putEdge(eestmt4, estmt13);
    stmtGraph.putEdge(estmt13, stmt14);
    stmtGraph.putEdge(stmt14, ret);

    // set startingStmt
    stmtGraph.setStartingStmt(startingStmt);

    // set Position
    builder.setPosition(NoPositionInformation.getInstance());

    return builder.build();
  }

  @Test
  void testBigInput() {
    AnalysisInputLocation inputLocation =
        new ClassFileBasedAnalysisInputLocation(
            Paths.get("../shared-test-resources/bugfixes/SlowCopyPropagator.class"),
            "",
            SourceType.Application,
            Collections.singletonList(new CopyPropagator()));

    JavaView view = new JavaView(inputLocation);
    final SootMethod sootMethod =
        view.getMethod(
                view.getIdentifierFactory()
                    .parseMethodSignature("<SlowCopyPropagator: void foo()>"))
            .get();

    Body body = sootMethod.getBody();
    assertFalse(body.toString().isEmpty());
  }
}
