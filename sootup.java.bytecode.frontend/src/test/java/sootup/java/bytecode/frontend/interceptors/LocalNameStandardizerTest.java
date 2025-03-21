package sootup.java.bytecode.frontend.interceptors;

import java.util.*;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.BooleanConstant;
import sootup.core.jimple.common.constant.DoubleConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.Expr;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.PrimitiveType;
import sootup.core.types.UnknownType;
import sootup.core.types.VoidType;
import sootup.core.util.ImmutableUtils;
import sootup.interceptors.LocalNameStandardizer;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

/**
 * @author Zun Wang
 */
public class LocalNameStandardizerTest {

  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();
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
  FallsThroughStmt startingStmt = JavaJimple.newIdentityStmt(l0, identityRef, noStmtPositionInfo);
  FallsThroughStmt stmt1 =
      JavaJimple.newAssignStmt(l1, IntConstant.getInstance(1), noStmtPositionInfo);
  FallsThroughStmt stmt2 =
      JavaJimple.newAssignStmt(l2, BooleanConstant.getInstance(true), noStmtPositionInfo);
  FallsThroughStmt stmt3 =
      JavaJimple.newAssignStmt(l3, IntConstant.getInstance(0), noStmtPositionInfo);

  Expr expr = JavaJimple.newNewExpr(otherRefType);
  FallsThroughStmt stmt4 = JavaJimple.newAssignStmt(l4, expr, noStmtPositionInfo);
  FallsThroughStmt stmt5 =
      JavaJimple.newAssignStmt(l5, IntConstant.getInstance(2), noStmtPositionInfo);
  FallsThroughStmt stmt6 = JavaJimple.newAssignStmt(l6, l6, noStmtPositionInfo);
  FallsThroughStmt stmt7 =
      JavaJimple.newAssignStmt(l7, DoubleConstant.getInstance(1.1), noStmtPositionInfo);
  Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

  FallsThroughStmt estartingStmt = JavaJimple.newIdentityStmt(r2, identityRef, noStmtPositionInfo);
  FallsThroughStmt estmt1 =
      JavaJimple.newAssignStmt(i0, IntConstant.getInstance(1), noStmtPositionInfo);
  FallsThroughStmt estmt2 =
      JavaJimple.newAssignStmt(z0, BooleanConstant.getInstance(true), noStmtPositionInfo);
  FallsThroughStmt estmt3 =
      JavaJimple.newAssignStmt(e0, IntConstant.getInstance(0), noStmtPositionInfo);

  FallsThroughStmt estmt4 = JavaJimple.newAssignStmt(r0, expr, noStmtPositionInfo);
  FallsThroughStmt estmt5 =
      JavaJimple.newAssignStmt(i1, IntConstant.getInstance(2), noStmtPositionInfo);
  FallsThroughStmt estmt6 = JavaJimple.newAssignStmt(r1, r1, noStmtPositionInfo);
  FallsThroughStmt estmt7 =
      JavaJimple.newAssignStmt(d0, DoubleConstant.getInstance(1.1), noStmtPositionInfo);

  @Test
  public void testBody() {

    Body body = createBody();
    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());

    LocalNameStandardizer standardizer = new LocalNameStandardizer();
    standardizer.interceptBody(builder, new JavaView(Collections.emptyList()));

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

    final MutableStmtGraph stmtGraph = builder.getStmtGraph();
    // build stm
    // builder.getStmtGraph()tGraph for the builder
    stmtGraph.putEdge(startingStmt, stmt1);
    stmtGraph.putEdge(stmt1, stmt2);
    stmtGraph.putEdge(stmt2, stmt3);
    stmtGraph.putEdge(stmt3, stmt4);
    stmtGraph.putEdge(stmt4, stmt5);
    stmtGraph.putEdge(stmt5, stmt6);
    stmtGraph.putEdge(stmt6, stmt7);
    stmtGraph.putEdge(stmt7, ret);

    // set startingStmt
    stmtGraph.setStartingStmt(startingStmt);

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
    final MutableStmtGraph stmtGraph = builder.getStmtGraph();
    stmtGraph.putEdge(estartingStmt, estmt1);
    stmtGraph.putEdge(estmt1, estmt2);
    stmtGraph.putEdge(estmt2, estmt3);
    stmtGraph.putEdge(estmt3, estmt4);
    stmtGraph.putEdge(estmt4, estmt5);
    stmtGraph.putEdge(estmt5, estmt6);
    stmtGraph.putEdge(estmt6, estmt7);
    stmtGraph.putEdge(estmt7, ret);

    // set startingStmt
    stmtGraph.setStartingStmt(estartingStmt);

    // set Position
    builder.setPosition(NoPositionInformation.getInstance());

    return builder.build();
  }
}
