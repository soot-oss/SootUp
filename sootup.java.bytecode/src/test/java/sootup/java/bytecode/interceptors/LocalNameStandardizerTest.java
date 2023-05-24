package sootup.java.bytecode.interceptors;

import categories.Java8Test;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.BooleanConstant;
import sootup.core.jimple.common.constant.DoubleConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.Expr;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.PrimitiveType;
import sootup.core.types.UnknownType;
import sootup.core.types.VoidType;
import sootup.core.util.ImmutableUtils;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;

/** @author Zun Wang */
@Category(Java8Test.class)
public class LocalNameStandardizerTest {

  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();
  JavaClassType classType = factory.getClassType("Test");
  MethodSignature methodSignature =
      new MethodSignature(classType, "test", Collections.emptyList(), VoidType.getInstance());
  JavaClassType refType = factory.getClassType("ref");
  JavaClassType otherRefType = factory.getClassType("otherRef");

  IdentityRef identityRef = JavaJimple.newThisRef(classType);

  // build locals
  Local l0 = JavaJimple.newLocal("l0", refType);
  Local l1 = JavaJimple.newLocal("l1", PrimitiveType.getInt());
  Local l2 = JavaJimple.newLocal("l2", PrimitiveType.getBoolean());
  Local l3 = JavaJimple.newLocal("l3", UnknownType.getInstance());
  Local l4 = JavaJimple.newLocal("l4", otherRefType);
  Local l5 = JavaJimple.newLocal("l5", PrimitiveType.getInt());
  Local l6 = JavaJimple.newLocal("l6", otherRefType);
  Local l7 = JavaJimple.newLocal("l7", PrimitiveType.getDouble());

  Local z0 = JavaJimple.newLocal("z0", PrimitiveType.getBoolean());
  Local d0 = JavaJimple.newLocal("d0", PrimitiveType.getDouble());
  Local i0 = JavaJimple.newLocal("i0", PrimitiveType.getInt());
  Local i1 = JavaJimple.newLocal("i1", PrimitiveType.getInt());
  Local r0 = JavaJimple.newLocal("r0", otherRefType);
  Local r1 = JavaJimple.newLocal("r1", otherRefType);
  Local r2 = JavaJimple.newLocal("r2", refType);
  Local e0 = JavaJimple.newLocal("u0", UnknownType.getInstance());

  Set<Local> expectedLocals = ImmutableUtils.immutableSet(z0, d0, i0, i1, r0, r1, r2, e0);

  // build stmts
  Stmt startingStmt = JavaJimple.newIdentityStmt(l0, identityRef, noStmtPositionInfo);
  Stmt stmt1 = JavaJimple.newAssignStmt(l1, IntConstant.getInstance(1), noStmtPositionInfo);
  Stmt stmt2 = JavaJimple.newAssignStmt(l2, BooleanConstant.getInstance(true), noStmtPositionInfo);
  Stmt stmt3 = JavaJimple.newAssignStmt(l3, IntConstant.getInstance(0), noStmtPositionInfo);

  Expr expr = JavaJimple.newNewExpr(otherRefType);
  Stmt stmt4 = JavaJimple.newAssignStmt(l4, expr, noStmtPositionInfo);
  Stmt stmt5 = JavaJimple.newAssignStmt(l5, IntConstant.getInstance(2), noStmtPositionInfo);
  Stmt stmt6 = JavaJimple.newAssignStmt(l6, l6, noStmtPositionInfo);
  Stmt stmt7 = JavaJimple.newAssignStmt(l7, DoubleConstant.getInstance(1.1), noStmtPositionInfo);
  Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

  Stmt estartingStmt = JavaJimple.newIdentityStmt(r2, identityRef, noStmtPositionInfo);
  Stmt estmt1 = JavaJimple.newAssignStmt(i0, IntConstant.getInstance(1), noStmtPositionInfo);
  Stmt estmt2 = JavaJimple.newAssignStmt(z0, BooleanConstant.getInstance(true), noStmtPositionInfo);
  Stmt estmt3 = JavaJimple.newAssignStmt(e0, IntConstant.getInstance(0), noStmtPositionInfo);

  Stmt estmt4 = JavaJimple.newAssignStmt(r0, expr, noStmtPositionInfo);
  Stmt estmt5 = JavaJimple.newAssignStmt(i1, IntConstant.getInstance(2), noStmtPositionInfo);
  Stmt estmt6 = JavaJimple.newAssignStmt(r1, r1, noStmtPositionInfo);
  Stmt estmt7 = JavaJimple.newAssignStmt(d0, DoubleConstant.getInstance(1.1), noStmtPositionInfo);

  @Test
  public void testBody() {

    Body body = createBody();
    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());

    LocalNameStandardizer standardizer = new LocalNameStandardizer();
    standardizer.interceptBody(builder, null);

    Body expectedBody = createExpectedBody();

    AssertUtils.assertLocalsEquiv(expectedBody, builder.build());
    AssertUtils.assertStmtGraphEquiv(expectedBody, builder.build());
  }

  private Body createBody() {

    // build an instance of BodyBuilder
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, l3, l4, l5, l6, l7);

    builder.setLocals(locals);

    // build stmtGraph for the builder
    builder.addFlow(startingStmt, stmt1);
    builder.addFlow(stmt1, stmt2);
    builder.addFlow(stmt2, stmt3);
    builder.addFlow(stmt3, stmt4);
    builder.addFlow(stmt4, stmt5);
    builder.addFlow(stmt5, stmt6);
    builder.addFlow(stmt6, stmt7);
    builder.addFlow(stmt7, ret);

    // set startingStmt
    builder.setStartingStmt(startingStmt);

    // set Position
    builder.setPosition(NoPositionInformation.getInstance());

    return builder.build();
  }

  private Body createExpectedBody() {

    // build an instance of BodyBuilder
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(z0, d0, i0, i1, r0, r1, r2, e0);

    builder.setLocals(locals);

    // build stmtGraph for the builder
    builder.addFlow(estartingStmt, estmt1);
    builder.addFlow(estmt1, estmt2);
    builder.addFlow(estmt2, estmt3);
    builder.addFlow(estmt3, estmt4);
    builder.addFlow(estmt4, estmt5);
    builder.addFlow(estmt5, estmt6);
    builder.addFlow(estmt6, estmt7);
    builder.addFlow(estmt7, ret);

    // set startingStmt
    builder.setStartingStmt(estartingStmt);

    // set Position
    builder.setPosition(NoPositionInformation.getInstance());

    return builder.build();
  }
}
