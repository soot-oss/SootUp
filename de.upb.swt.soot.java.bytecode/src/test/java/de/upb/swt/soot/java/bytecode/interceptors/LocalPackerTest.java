package de.upb.swt.soot.java.bytecode.interceptors;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.NoPositionInformation;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.ref.IdentityRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.VoidType;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Zun Wang */
@Category(Java8Test.class)
public class LocalPackerTest {
  // Preparation
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

  JavaClassType classType = factory.getClassType("Test");
  JavaClassType intType = factory.getClassType("int");

  IdentityRef identityRef = JavaJimple.newThisRef(classType);

  IdentityRef identityRef0 = JavaJimple.newParameterRef(intType, 0);
  IdentityRef identityRef1 = JavaJimple.newParameterRef(intType, 1);

  // build locals
  Local l0 = JavaJimple.newLocal("l0", classType);
  Local l1 = JavaJimple.newLocal("l1", intType);
  Local l1hash1 = JavaJimple.newLocal("l1#1", intType);
  Local l2 = JavaJimple.newLocal("l2", intType);
  Local l2hash2 = JavaJimple.newLocal("l2#2", intType);
  Local l3 = JavaJimple.newLocal("l3", intType);
  Local l2hash3 = JavaJimple.newLocal("l2#3", intType);
  Local l1hash4 = JavaJimple.newLocal("l1#4", intType);
  Local l1hash5 = JavaJimple.newLocal("l1#5", intType);

  // build stmts
  Stmt startingStmt = JavaJimple.newIdentityStmt(l0, identityRef, noStmtPositionInfo);
  Stmt identityStmt0 = JavaJimple.newIdentityStmt(l1hash1, identityRef0, noStmtPositionInfo);
  Stmt identityStmt1 = JavaJimple.newIdentityStmt(l2hash2, identityRef1, noStmtPositionInfo);
  Stmt stmt1 = JavaJimple.newAssignStmt(l3, IntConstant.getInstance(10), noStmtPositionInfo);
  Stmt stmt2 = JavaJimple.newAssignStmt(l2hash3, l3, noStmtPositionInfo);
  Stmt stmt3 = JavaJimple.newAssignStmt(l1hash4, IntConstant.getInstance(0), noStmtPositionInfo);
  Stmt stmt4 =
      JavaJimple.newAssignStmt(
          l1hash5, JavaJimple.newAddExpr(l1hash4, IntConstant.getInstance(1)), noStmtPositionInfo);
  Stmt stmt5 =
      JavaJimple.newAssignStmt(
          l1hash5, JavaJimple.newAddExpr(l1hash5, IntConstant.getInstance(1)), noStmtPositionInfo);
  Stmt stmt6 = JavaJimple.newIfStmt(JavaJimple.newGtExpr(l1hash5, l3), noStmtPositionInfo);
  Stmt gt = JavaJimple.newGotoStmt(noStmtPositionInfo);
  Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

  Stmt eidentityStmt0 = JavaJimple.newIdentityStmt(l1, identityRef0, noStmtPositionInfo);
  Stmt eidentityStmt1 = JavaJimple.newIdentityStmt(l2, identityRef1, noStmtPositionInfo);
  Stmt estmt1 = JavaJimple.newAssignStmt(l1, IntConstant.getInstance(10), noStmtPositionInfo);
  Stmt estmt2 = JavaJimple.newAssignStmt(l2, l1, noStmtPositionInfo);
  Stmt estmt3 = JavaJimple.newAssignStmt(l2, IntConstant.getInstance(0), noStmtPositionInfo);
  Stmt estmt4 =
      JavaJimple.newAssignStmt(
          l2, JavaJimple.newAddExpr(l2, IntConstant.getInstance(1)), noStmtPositionInfo);
  Stmt estmt5 =
      JavaJimple.newAssignStmt(
          l2, JavaJimple.newAddExpr(l2, IntConstant.getInstance(1)), noStmtPositionInfo);
  Stmt estmt6 = JavaJimple.newIfStmt(JavaJimple.newGtExpr(l2, l1), noStmtPositionInfo);

  /**
   *
   *
   * <pre>
   *    Test l0;
   *    int l1, l2, l3, l1#1, l2#2, l2#3, l1#4, l1#5
   *
   *    l0 := @this Test
   *    l1#1 := @parameter0: int;
   *    l2#2 := @parameter1: int;
   *    l3 = 10;
   *    l2#3 = l3;
   *    l1#4 = 0;
   *    l1#5 = l1#4 + 1;
   *  label1:
   *    if l1#5 > l3 goto label2;
   *    goto label1;
   *  label2:
   *    return;
   * </pre>
   *
   * to:
   *
   * <pre>
   *    Test l0;
   *    int l1, l2;
   *
   *    l0 := @this: Test;
   *    l1 := @parameter0: int;
   *    l2 := @parameter1: int;
   *    l1 = 10;
   *    l2 = l1;
   *    l2 = 0;
   *    l2 = l2 + 1;
   *  label1:
   *    l2 = l2 + 1;
   *    if l2 > l1 goto label2;
   *  label2:
   *     return;
   * </pre>
   */
  @Test
  public void testLocalPacker() {
    Body.BodyBuilder builder = createBodyBuilder();

    new LocalPacker().interceptBody(builder);
    Body body = builder.build();

    Body expectedBody = createExpectedBody();

    AssertUtils.assertLocalsEquiv(expectedBody, body);
    AssertUtils.assertStmtGraphEquiv(expectedBody, body);
  }

  private Body.BodyBuilder createBodyBuilder() {

    Body.BodyBuilder builder = Body.builder();

    List<Type> parameters = new ArrayList<>();
    parameters.add(intType);
    // parameters.add(doubleType);
    MethodSignature methodSignature =
        new MethodSignature(classType, "test", parameters, VoidType.getInstance());
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals =
        ImmutableUtils.immutableSet(l0, l1, l2, l3, l1hash1, l2hash2, l2hash3, l1hash4, l1hash5);
    builder.setLocals(locals);

    // build stmtGraph
    builder.addFlow(startingStmt, identityStmt0);
    builder.addFlow(identityStmt0, identityStmt1);
    builder.addFlow(identityStmt1, stmt1);
    builder.addFlow(stmt1, stmt2);
    builder.addFlow(stmt2, stmt3);
    builder.addFlow(stmt3, stmt4);
    builder.addFlow(stmt4, stmt5);
    builder.addFlow(stmt5, stmt6);
    builder.addFlow(stmt6, gt);
    builder.addFlow(gt, stmt5);
    builder.addFlow(stmt6, ret);

    builder.setStartingStmt(startingStmt);

    return builder;
  }

  private Body createExpectedBody() {

    Body.BodyBuilder builder = Body.builder();

    List<Type> parameters = new ArrayList<>();
    parameters.add(intType);
    // parameters.add(doubleType);
    MethodSignature methodSignature =
        new MethodSignature(classType, "test", parameters, VoidType.getInstance());
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2);
    builder.setLocals(locals);

    // build stmtGraph
    builder.addFlow(startingStmt, eidentityStmt0);
    builder.addFlow(eidentityStmt0, eidentityStmt1);
    builder.addFlow(eidentityStmt1, estmt1);
    builder.addFlow(estmt1, estmt2);
    builder.addFlow(estmt2, estmt3);
    builder.addFlow(estmt3, estmt4);
    builder.addFlow(estmt4, estmt5);
    builder.addFlow(estmt5, estmt6);
    builder.addFlow(estmt6, gt);
    builder.addFlow(gt, estmt5);
    builder.addFlow(estmt6, ret);

    builder.setStartingStmt(startingStmt);

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    return builder.build();
  }
}
