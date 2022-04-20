package de.upb.swt.soot.java.bytecode.interceptors;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.NoPositionInformation;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.common.constant.BooleanConstant;
import de.upb.swt.soot.core.jimple.common.constant.DoubleConstant;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.expr.Expr;
import de.upb.swt.soot.core.jimple.common.ref.IdentityRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.UnknownType;
import de.upb.swt.soot.core.types.VoidType;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
  Local e0 = JavaJimple.newLocal("e0", UnknownType.getInstance());

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

  @Test
  public void testBody() {

    Body body = createBody();
    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());

    LocalNameStandardizer standardizer = new LocalNameStandardizer();
    standardizer.interceptBody(builder);

    assertEquals(builder.getLocals().size(), expectedLocals.size());
    List<Local> localsList = new ArrayList<>(builder.getLocals());
    List<Local> expectedLocalsList = new ArrayList<>(expectedLocals);
    for (int i = 0; i < localsList.size(); i++) {
      assertEquals(localsList.get(i), expectedLocalsList.get(i));
    }
  }

  private Body createBody() {

    // build an instance of BodyBuilder
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, l3, l4, l5, l6, l7);

    builder.setLocals(locals);

    // build stmtsGraph for the builder
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
}
