package sootup.java.bytecode.interceptors;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import java.util.Collections;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.expr.AbstractConditionExpr;
import sootup.core.jimple.common.expr.Expr;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.VoidType;
import sootup.core.util.ImmutableUtils;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;

/** @author Zun Wang */
@Category(Java8Test.class)
public class CopyPropagatorTest {

  // Preparation
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();
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
  Stmt startingStmt = JavaJimple.newIdentityStmt(r0, identityRef, noStmtPositionInfo);
  // r1 = new ref
  Expr expr = JavaJimple.newNewExpr(refType);
  Stmt stmt1 = JavaJimple.newAssignStmt(r1, expr, noStmtPositionInfo);
  // r2 = r1
  Stmt stmt2 = JavaJimple.newAssignStmt(r2, r1, noStmtPositionInfo);
  // r3 = r2
  Stmt stmt3 = JavaJimple.newAssignStmt(r3, r2, noStmtPositionInfo);
  // r4 = r3
  Stmt stmt4 = JavaJimple.newAssignStmt(r4, r3, noStmtPositionInfo);
  // return
  Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

  // r3 = r1;
  Stmt estmt3 = JavaJimple.newAssignStmt(r3, r1, noStmtPositionInfo);
  // r4 = r1
  Stmt estmt4 = JavaJimple.newAssignStmt(r4, r1, noStmtPositionInfo);

  // i1 = 5
  Stmt stmt5 = JavaJimple.newAssignStmt(i1, IntConstant.getInstance(5), noStmtPositionInfo);
  // i2 = 0
  Stmt stmt6 = JavaJimple.newAssignStmt(i2, IntConstant.getInstance(0), noStmtPositionInfo);
  // if i2 > i1 goto
  AbstractConditionExpr condition = JavaJimple.newGtExpr(i2, i1);
  Stmt stmt7 = JavaJimple.newIfStmt(condition, noStmtPositionInfo);
  // i3 = i1 + 1
  Expr add1 = JavaJimple.newAddExpr(i1, IntConstant.getInstance(1));
  Stmt stmt8 = JavaJimple.newAssignStmt(i3, add1, noStmtPositionInfo);
  // i2 = i2 + 1
  Expr add2 = JavaJimple.newAddExpr(i2, IntConstant.getInstance(1));
  Stmt stmt9 = JavaJimple.newAssignStmt(i2, add2, noStmtPositionInfo);
  Stmt gotoStmt = JavaJimple.newGotoStmt(noStmtPositionInfo);

  // if i2 > 5 goto
  AbstractConditionExpr econdition = JavaJimple.newGtExpr(i2, IntConstant.getInstance(5));
  Stmt estmt7 = JavaJimple.newIfStmt(econdition, noStmtPositionInfo);
  // i3 = 5 + 1
  Expr eadd1 = JavaJimple.newAddExpr(IntConstant.getInstance(5), IntConstant.getInstance(1));
  Stmt estmt8 = JavaJimple.newAssignStmt(i3, eadd1, noStmtPositionInfo);

  // r0 := @this Test; r1 = (ref) 0; r2 = (ref) 0L; r3 = (ref) 1; r4 = r1, r5 = r2
  // r1 = (ref) 0
  JCastExpr intCast = JavaJimple.newCastExpr(IntConstant.getInstance(0), refType);
  Stmt stmt10 = JavaJimple.newAssignStmt(r1, intCast, noStmtPositionInfo);
  // r2 = (ref) 0L
  JCastExpr longCast = JavaJimple.newCastExpr(LongConstant.getInstance(0), refType);
  Stmt stmt11 = JavaJimple.newAssignStmt(r2, longCast, noStmtPositionInfo);
  // r3 = (ref) 1
  JCastExpr intCast1 = JavaJimple.newCastExpr(IntConstant.getInstance(1), refType);
  Stmt stmt12 = JavaJimple.newAssignStmt(r3, intCast1, noStmtPositionInfo);
  // r5 = r2
  Stmt stmt13 = JavaJimple.newAssignStmt(r5, r2, noStmtPositionInfo);
  // r6 = r3
  Stmt stmt14 = JavaJimple.newAssignStmt(r6, r3, noStmtPositionInfo);

  JAssignStmt<Local, Constant> eestmt4 =
      JavaJimple.newAssignStmt(r4, NullConstant.getInstance(), noStmtPositionInfo);
  JAssignStmt<Local, Constant> estmt13 =
      JavaJimple.newAssignStmt(r5, NullConstant.getInstance(), noStmtPositionInfo);

  @Test
  public void testEqualStmt() {
    assertTrue(eestmt4.equivTo(eestmt4.withRValue(NullConstant.getInstance())));
  }

  @Test
  /** Test the copy propagation's chain */
  public void testChainBody() {

    Body body = createChainBody();
    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());
    CopyPropagator propagator = new CopyPropagator();
    propagator.interceptBody(builder, null);

    Body expectedBody = createExpectedChainBody();
    AssertUtils.assertStmtGraphEquiv(expectedBody, builder.build());
  }

  @Test
  /** Test the copy propagation for loop */
  public void testLoopBody() {

    Body.BodyBuilder builder = createLoopBody();

    CopyPropagator propagator = new CopyPropagator();
    propagator.interceptBody(builder, null);

    Body expectedBody = createExpectedLoopBody();
    AssertUtils.assertStmtGraphEquiv(expectedBody, builder.build());
  }

  @Test
  /* Test the copy propagation for castExpr */
  public void testCastExprBody() {

    Body body = createCastExprBody();
    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());
    CopyPropagator propagator = new CopyPropagator();
    propagator.interceptBody(builder, null);

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

    // build stmtsGraph for the builder
    builder.addFlow(startingStmt, stmt1);
    builder.addFlow(stmt1, stmt2);
    builder.addFlow(stmt2, stmt3);
    builder.addFlow(stmt3, stmt4);
    builder.addFlow(stmt4, ret);

    // set startingStmt
    builder.setStartingStmt(startingStmt);

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

    // build stmtsGraph for the builder
    builder.addFlow(startingStmt, stmt1);
    builder.addFlow(stmt1, stmt2);
    builder.addFlow(stmt2, estmt3);
    builder.addFlow(estmt3, estmt4);
    builder.addFlow(estmt4, ret);

    // set startingStmt
    builder.setStartingStmt(startingStmt);

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

    // build stmtsGraph for the builder
    builder.addFlow(startingStmt, stmt5);
    builder.addFlow(stmt5, stmt6);
    builder.addFlow(stmt6, stmt7);
    builder.addFlow(stmt7, stmt8);
    builder.addFlow(stmt8, stmt9);
    builder.addFlow(stmt9, gotoStmt);
    builder.addFlow(gotoStmt, stmt7);
    builder.addFlow(stmt7, ret);

    // set startingStmt
    builder.setStartingStmt(startingStmt);

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

    // build stmtsGraph for the builder
    builder.addFlow(startingStmt, stmt5);
    builder.addFlow(stmt5, stmt6);
    builder.addFlow(stmt6, estmt7);
    builder.addFlow(estmt7, estmt8);
    builder.addFlow(estmt8, stmt9);
    builder.addFlow(stmt9, gotoStmt);
    builder.addFlow(gotoStmt, estmt7);
    builder.addFlow(estmt7, ret);

    // set startingStmt
    builder.setStartingStmt(startingStmt);

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

    // build stmtsGraph for the builder
    builder.addFlow(startingStmt, stmt10);
    builder.addFlow(stmt10, stmt11);
    builder.addFlow(stmt11, stmt12);
    builder.addFlow(stmt12, estmt4);
    builder.addFlow(estmt4, stmt13);
    builder.addFlow(stmt13, stmt14);
    builder.addFlow(stmt14, ret);

    // set startingStmt
    builder.setStartingStmt(startingStmt);

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

    // build stmtsGraph for the builder
    builder.addFlow(startingStmt, stmt10);
    builder.addFlow(stmt10, stmt11);
    builder.addFlow(stmt11, stmt12);
    builder.addFlow(stmt12, eestmt4);
    builder.addFlow(eestmt4, estmt13);
    builder.addFlow(estmt13, stmt14);
    builder.addFlow(stmt14, ret);

    // set startingStmt
    builder.setStartingStmt(startingStmt);

    // set Position
    builder.setPosition(NoPositionInformation.getInstance());

    return builder.build();
  }
}
