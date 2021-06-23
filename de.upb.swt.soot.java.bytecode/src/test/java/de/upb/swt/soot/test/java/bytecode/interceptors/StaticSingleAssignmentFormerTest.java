package de.upb.swt.soot.test.java.bytecode.interceptors;

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
import de.upb.swt.soot.core.types.VoidType;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.bytecode.interceptors.StaticSingleAssigmentFormer;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.Collections;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Zun Wang */
@Category(Java8Test.class)
public class StaticSingleAssignmentFormerTest {

  // Preparation
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

  JavaClassType intType = factory.getClassType("int");
  JavaClassType classType = factory.getClassType("Test");
  MethodSignature methodSignature =
      new MethodSignature(classType, "test", Collections.emptyList(), VoidType.getInstance());
  IdentityRef identityRef = JavaJimple.newThisRef(classType);

  // build locals
  Local l0 = JavaJimple.newLocal("l0", intType);
  Local l1 = JavaJimple.newLocal("l1", intType);
  Local l2 = JavaJimple.newLocal("l2", intType);
  Local l3 = JavaJimple.newLocal("l3", intType);

  Stmt startingStmt = JavaJimple.newIdentityStmt(l0, identityRef, noStmtPositionInfo);
  Stmt stmt1 = JavaJimple.newAssignStmt(l1, IntConstant.getInstance(1), noStmtPositionInfo);
  Stmt stmt2 = JavaJimple.newAssignStmt(l2, IntConstant.getInstance(1), noStmtPositionInfo);
  Stmt stmt3 = JavaJimple.newAssignStmt(l3, IntConstant.getInstance(0), noStmtPositionInfo);
  Stmt stmt4 =
      JavaJimple.newIfStmt(
          JavaJimple.newLtExpr(l3, IntConstant.getInstance(100)), noStmtPositionInfo);
  Stmt stmt5 =
      JavaJimple.newIfStmt(
          JavaJimple.newLtExpr(l2, IntConstant.getInstance(20)), noStmtPositionInfo);
  Stmt stmt6 = JavaJimple.newReturnStmt(l2, noStmtPositionInfo);
  Stmt stmt7 = JavaJimple.newAssignStmt(l2, l1, noStmtPositionInfo);
  Stmt stmt8 =
      JavaJimple.newAssignStmt(
          l3, JavaJimple.newAddExpr(l3, IntConstant.getInstance(1)), noStmtPositionInfo);
  Stmt stmt9 = JavaJimple.newAssignStmt(l2, l3, noStmtPositionInfo);
  Stmt stmt10 =
      JavaJimple.newAssignStmt(
          l3, JavaJimple.newAddExpr(l3, IntConstant.getInstance(2)), noStmtPositionInfo);
  Stmt stmt11 = JavaJimple.newGotoStmt(noStmtPositionInfo);

  @Test
  public void testSSA() {
    StaticSingleAssigmentFormer ssa = new StaticSingleAssigmentFormer();
    Body.BodyBuilder builder = new Body.BodyBuilder(createBody(), Collections.emptySet());
    ssa.interceptBody(builder);

    // expected Blocks in BlockGraph
    /*Block eblock1 = new Block(startingStmt, stmt2, body);
    Block eblock2 = new Block(stmt3, stmt4, body);
    Block eblock3 = new Block(stmt5, stmt6, body);
    Block eblock4 = new Block(ret, ret, body);*/

  }

  /**
   * bodycreater for BinaryBranches
   *
   * <pre>
   *    l0 := @this Test
   *    l1 = 0
   *    if l1 >= 0 goto label1
   *    l1 = l1 + 1
   *    goto label2
   * label1:
   *    l1 = l1 - 1
   *    l1 = l1 + 2
   * label2:
   *    return l1
   * </pre>
   *
   * after
   *
   * <pre>
   *    l0#00 := @this Test
   *    l1#01 = 0
   *    if l1#01 >= 0 goto label1
   *    l1#10 = l1#01 + 1
   *    goto label2
   *    label1:
   *    l1#20 = l1#01 - 1
   *    l1#21 = l1#20 + 2
   *    label2:
   *    return phi(l1#10, l1#21)
   * </pre>
   */
  /* private Body.BodyBuilder createBBBody() {
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1);
    builder.setLocals(locals);

    // set graph
    builder.addFlow(startingStmt, stmt1);
    builder.addFlow(stmt1, stmt2);
    builder.addFlow(stmt2, stmt3);
    builder.addFlow(stmt3, stmt4);
    builder.addFlow(stmt4, ret);
    builder.addFlow(stmt2, stmt5);
    builder.addFlow(stmt5, stmt6);
    builder.addFlow(stmt6, ret);

    // build startingStmt
    builder.setStartingStmt(startingStmt);

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    return builder;
  }*/

  /**
   *
   *
   * <pre>
   *    l0 := @this Test
   *    l1 = 0
   * label1:
   *    l4 = l1
   *    l3 = 10
   *    if l4 >= l3 goto label2
   *    l2 = l1 + 1
   *    l1 = l2 + 1
   *    l1 = l1 - 1
   *    goto label1
   * label2:
   *    return
   * </pre>
   */
  /* private Body.BodyBuilder createLoopBody() {

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, l3, l4);

    builder.setLocals(locals);

    // set graph
    builder.addFlow(startingStmt, stmt1);
    builder.addFlow(stmt1, stmt7);
    builder.addFlow(stmt7, stmt8);
    builder.addFlow(stmt8, stmt9);
    builder.addFlow(stmt9, stmt10);
    builder.addFlow(stmt9, ret);
    builder.addFlow(stmt10, stmt11);
    builder.addFlow(stmt11, stmt5);
    builder.addFlow(stmt5, stmt4);
    builder.addFlow(stmt4, stmt7);

    // build startingStmt
    builder.setStartingStmt(startingStmt);

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    return builder;
  }*/

  /**
   *
   *
   * <pre>
   *    l0 := @this Test
   *    l1 = 1
   *    l2 = 1
   *    l3 = 0
   * label1:
   *    if l3 < 100 goto label3
   *    if l2 < 20 goto label 2
   *    l2 = l1
   *    l3 = l3 + 1
   *    goto label1;
   * label2:
   *    l2 = l3
   *    l3 = l3 + 2
   * label3:
   *    return l2
   * </pre>
   */
  private Body createBody() {
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, l3);
    builder.setLocals(locals);

    // set graph
    builder.addFlow(startingStmt, stmt1);
    builder.addFlow(stmt1, stmt2);
    builder.addFlow(stmt2, stmt3);
    builder.addFlow(stmt3, stmt4);
    builder.addFlow(stmt4, stmt5);
    builder.addFlow(stmt4, stmt6);
    builder.addFlow(stmt5, stmt7);
    builder.addFlow(stmt5, stmt9);
    builder.addFlow(stmt7, stmt8);
    builder.addFlow(stmt9, stmt10);
    builder.addFlow(stmt8, stmt11);
    builder.addFlow(stmt10, stmt11);
    builder.addFlow(stmt11, stmt4);

    // build startingStmt
    builder.setStartingStmt(startingStmt);

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    return builder.build();
  }
}
