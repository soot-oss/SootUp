package de.upb.swt.soot.test.java.bytecode.interceptors;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.NoPositionInformation;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.expr.Expr;
import de.upb.swt.soot.core.jimple.common.ref.IdentityRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.VoidType;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.bytecode.interceptors.CopyPropagator;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.Collections;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
  Local l0 = JavaJimple.newLocal("l0", intType);
  Local l1 = JavaJimple.newLocal("l1", intType);
  Local l2 = JavaJimple.newLocal("l2", intType);
  Local l3 = JavaJimple.newLocal("l3", intType);
  Local r0 = JavaJimple.newLocal("r0", intType);

  // build expr
  Expr expr = JavaJimple.newNewExpr(refType);

  // build Stmts
  // l0 := @this Test
  Stmt startingStmt = JavaJimple.newIdentityStmt(l0, identityRef, noStmtPositionInfo);
  // r0 = new ref
  Stmt stmt1 = JavaJimple.newAssignStmt(r0, expr, noStmtPositionInfo);
  // l1 = r0
  Stmt stmt2 = JavaJimple.newAssignStmt(l1, r0, noStmtPositionInfo);
  // l2 = l1
  Stmt stmt3 = JavaJimple.newAssignStmt(l2, l1, noStmtPositionInfo);
  // l3 = l2
  Stmt stmt4 = JavaJimple.newAssignStmt(l3, l2, noStmtPositionInfo);
  // return
  Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

  // l2 = rLocal
  Stmt estmt3 = JavaJimple.newAssignStmt(l2, r0, noStmtPositionInfo);
  // l3 = rLocal
  Stmt estmt4 = JavaJimple.newAssignStmt(l3, r0, noStmtPositionInfo);

  // l1 = 5
  Stmt stmt5 = JavaJimple.newAssignStmt(l1, IntConstant.getInstance(5), noStmtPositionInfo);
  // l2 = 0
  Stmt stmt6 = JavaJimple.newAssignStmt(l2, IntConstant.getInstance(0), noStmtPositionInfo);
  // if l2 > l1 goto
  Value condition = JavaJimple.newGtExpr(l2, l1);
  Stmt stmt7 = JavaJimple.newIfStmt(condition, noStmtPositionInfo);
  // l3 = l1 + 1
  Expr add1 = JavaJimple.newAddExpr(l1, IntConstant.getInstance(1));
  Stmt stmt8 = JavaJimple.newAssignStmt(l3, add1, noStmtPositionInfo);
  // l2 = l2 + 1
  Expr add2 = JavaJimple.newAddExpr(l2, IntConstant.getInstance(1));
  Stmt stmt9 = JavaJimple.newAssignStmt(l2, add2, noStmtPositionInfo);
  Stmt gotoStmt = JavaJimple.newGotoStmt(noStmtPositionInfo);

  // if l2 > 5 goto
  Value econdition = JavaJimple.newGtExpr(l2, IntConstant.getInstance(5));
  Stmt estmt7 = JavaJimple.newIfStmt(econdition, noStmtPositionInfo);
  // l3 = 5 + 1
  Expr eadd1 = JavaJimple.newAddExpr(IntConstant.getInstance(5), IntConstant.getInstance(1));
  Stmt estmt8 = JavaJimple.newAssignStmt(l3, eadd1, noStmtPositionInfo);

  @Test
  /** Test the copy propagation's chain */
  public void testChainBody() {

    Body body = createChainBody();
    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());
    CopyPropagator propagator = new CopyPropagator();
    propagator.interceptBody(builder);

    Body expectedBody = createExpectedChainBody();
    AssertUtils.assertStmtGraphEquiv(expectedBody, builder.build());
  }

  @Test
  /** Test the copy propagation's chain */
  public void testLoopBody() {

    Body body = createLoopBody();
    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());
    CopyPropagator propagator = new CopyPropagator();
    propagator.interceptBody(builder);

    Body expectedBody = createExpectedLoopBody();
    AssertUtils.assertStmtGraphEquiv(expectedBody, builder.build());
  }

  /** l0 := @this Test; r0 = new ref; l1 = r0; l2 = l1; l3 = l2; return */
  private Body createChainBody() {

    // build an instance of BodyBuilder
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(r0, l0, l1, l2, l3);
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

  /** l0 := @this Test; r0 = new; l1 = r0; l2 = r0; l3 = r0; return */
  private Body createExpectedChainBody() {

    // build an instance of BodyBuilder
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(r0, l0, l1, l2, l3);
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
   * l0 := @this Test; l1 = 5; l2 = 0; if l2 > l1 goto label2; l3 = l1 + 1; l2 = l2 + 1; goto
   * label1; return
   */
  private Body createLoopBody() {

    // build an instance of BodyBuilder
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, l3);
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

    // set Position
    builder.setPosition(NoPositionInformation.getInstance());

    return builder.build();
  }

  /**
   * l0 := @this Test; l1 = 5; l2 = 0; if l2 > l1 goto label2; l3 = l1 + 1; l2 = l2 + 1; goto
   * label1; return
   */
  private Body createExpectedLoopBody() {
    // build an instance of BodyBuilder
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, l3);
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
}
