package de.upb.swt.soot.test.java.bytecode.interceptors;

import static org.junit.Assert.*;

import categories.Java8Test;
import com.google.common.collect.Lists;
import de.upb.swt.soot.core.graph.StmtGraph;
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
import de.upb.swt.soot.java.bytecode.interceptors.LocalSplitter;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Zun Wang */
@Category(Java8Test.class)
public class LocalSplitterTest {

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
  Local stack3 = JavaJimple.newLocal("stack3", intType);
  Local stack4 = JavaJimple.newLocal("stack4", intType);
  Local l1hash1 = JavaJimple.newLocal("l1#1", intType);
  Local l1hash2 = JavaJimple.newLocal("l1#2", intType);
  Local l1hash3 = JavaJimple.newLocal("l1#3", intType);
  Local l2hash2 = JavaJimple.newLocal("l2#2", intType);
  Local l2hash4 = JavaJimple.newLocal("l2#4", intType);

  Stmt startingStmt = JavaJimple.newIdentityStmt(l0, identityRef, noStmtPositionInfo);

  /**
   * int a = 0; if(a<0) a = a + 1; else {a = a+ 1; a = a +1;} return a
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
   * to:
   *
   * <pre>
   *    l0 := @this Test
   *    l1#1 = 0
   *    if l1#1 >= 0 goto label1
   *    l1#2 = l1#1 + 1
   *    goto label2
   * label1:
   *    l1#3 = l1#1 - 1
   *    l1#2 = l1#3 + 2
   * label2:
   *    return l1#2
   * </pre>
   */
  @Test
  public void testLocalSplitterForBinaryBranches() {

    Body body = createBBBody();
    LocalSplitter localSplitter = new LocalSplitter();
    Body newBody = localSplitter.interceptBody(body);
    Body expectedBody = createExpectedBBBody();

    // check newBody's locals
    assertLocalsEquiv(expectedBody.getLocals(), newBody.getLocals());

    // check newBody's stmtGraph
    assertStmtGraphEquiv(expectedBody.getStmtGraph(), newBody.getStmtGraph());
  }

  /**
   * int a = 0; int b = 0; a = a + 1; b = b + 1;
   *
   * <pre>
   *    l0 := @this Test
   *    l1 = 0
   *    l2 = 1
   *    l1 = l1 + 1
   *    l2 = l2 + 1
   *    return
   * </pre>
   *
   * to:
   *
   * <pre>
   *    l0 := @this Test
   *    l1#1 = 0
   *    l2#2 = 1
   *    l1#3 = l1#1 + 1
   *    l2#4 = l2#2 + 1
   *    return
   * </pre>
   */
  @Test
  public void testLocalSplitterForMultilocals() {

    Body body = createMultilocalsBody();
    LocalSplitter localSplitter = new LocalSplitter();
    Body newBody = localSplitter.interceptBody(body);
    Body expectedBody = createExpectedMuiltilocalsBody();

    // check newBody's locals
    assertLocalsEquiv(expectedBody.getLocals(), newBody.getLocals());

    // check newBody's stmtGraph
    assertStmtGraphEquiv(expectedBody.getStmtGraph(), newBody.getStmtGraph());
  }

  /**
   * for(int i = 0; i < 10; i++){ i = i + 1 } transform:
   *
   * <pre>
   *    l0 := @this Test
   *    l1 = 0
   * label1:
   *    $stack4 = l1
   *    $stack3 = 10
   *    if $stack4 >= $stack3 goto label2
   *    l2 = l1 + 1
   *    l1 = l2 + 1
   *    l1 = l1 +1
   *    goto label1
   * label2:
   *    return
   * </pre>
   *
   * to:
   *
   * <pre>
   *    l0 := @this Test
   *    l1#1 = 0
   * label1:
   *    $stack4 = l1#1
   *    $stack3 = 10
   *    if $stack4 >= $stack3 goto label2
   *    l2 = l1#1 + 1
   *    l1#2 = l2 + 1
   *    l1#1 = l1#2 +1
   *    goto label1
   * label2:
   *    return
   * </pre>
   */
  @Test
  public void testLocalSplitterForLoop() {

    Body body = createLoopBody();
    LocalSplitter localSplitter = new LocalSplitter();
    Body newBody = localSplitter.interceptBody(body);
    Body expectedBody = createExpectedLoopBody();

    // check newBody's locals
    assertLocalsEquiv(expectedBody.getLocals(), newBody.getLocals());

    // check newBody's stmtGraph
    assertStmtGraphEquiv(expectedBody.getStmtGraph(), newBody.getStmtGraph());
  }

  /** bodycreater for BinaryBranches */
  private Body createBBBody() {
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1);
    builder.setLocals(locals);

    Stmt stmt1 = JavaJimple.newAssignStmt(l1, IntConstant.getInstance(0), noStmtPositionInfo);
    Stmt stmt2 =
        JavaJimple.newIfStmt(
            JavaJimple.newGeExpr(l1, IntConstant.getInstance(0)), noStmtPositionInfo);
    Stmt stmt3 =
        JavaJimple.newAssignStmt(
            l1, JavaJimple.newAddExpr(l1, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt4 = JavaJimple.newGotoStmt(noStmtPositionInfo);
    Stmt stmt5 =
        JavaJimple.newAssignStmt(
            l1, JavaJimple.newSubExpr(l1, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt6 =
        JavaJimple.newAssignStmt(
            l1, JavaJimple.newAddExpr(l1, IntConstant.getInstance(2)), noStmtPositionInfo);
    Stmt ret = JavaJimple.newReturnStmt(l1, noStmtPositionInfo);

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

    return builder.build();
  }

  private Body createExpectedBBBody() {

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l1hash1, l1hash2, l1hash3);
    builder.setLocals(locals);

    Stmt stmt1 = JavaJimple.newAssignStmt(l1hash1, IntConstant.getInstance(0), noStmtPositionInfo);
    Stmt stmt2 =
        JavaJimple.newIfStmt(
            JavaJimple.newGeExpr(l1hash1, IntConstant.getInstance(0)), noStmtPositionInfo);
    Stmt stmt3 =
        JavaJimple.newAssignStmt(
            l1hash2,
            JavaJimple.newAddExpr(l1hash1, IntConstant.getInstance(1)),
            noStmtPositionInfo);
    Stmt stmt4 = JavaJimple.newGotoStmt(noStmtPositionInfo);
    Stmt stmt5 =
        JavaJimple.newAssignStmt(
            l1hash3,
            JavaJimple.newSubExpr(l1hash1, IntConstant.getInstance(1)),
            noStmtPositionInfo);
    Stmt stmt6 =
        JavaJimple.newAssignStmt(
            l1hash2,
            JavaJimple.newAddExpr(l1hash3, IntConstant.getInstance(2)),
            noStmtPositionInfo);
    Stmt ret = JavaJimple.newReturnStmt(l1hash2, noStmtPositionInfo);

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

    return builder.build();
  }

  /** bodycreater for multilocals */
  private Body createMultilocalsBody() {

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2);
    builder.setLocals(locals);

    Stmt stmt1 = JavaJimple.newAssignStmt(l1, IntConstant.getInstance(0), noStmtPositionInfo);
    Stmt stmt2 = JavaJimple.newAssignStmt(l2, IntConstant.getInstance(1), noStmtPositionInfo);
    Stmt stmt3 =
        JavaJimple.newAssignStmt(
            l1, JavaJimple.newAddExpr(l1, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt4 =
        JavaJimple.newAssignStmt(
            l2, JavaJimple.newAddExpr(l2, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

    // set graph
    builder.addFlow(startingStmt, stmt1);
    builder.addFlow(stmt1, stmt2);
    builder.addFlow(stmt2, stmt3);
    builder.addFlow(stmt3, stmt4);
    builder.addFlow(stmt4, ret);

    // set first stmt
    builder.setStartingStmt(startingStmt);

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    return builder.build();
  }

  private Body createExpectedMuiltilocalsBody() {

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, l1hash1, l2hash2, l1hash3, l2hash4);
    builder.setLocals(locals);

    Stmt stmt1 = JavaJimple.newAssignStmt(l1hash1, IntConstant.getInstance(0), noStmtPositionInfo);
    Stmt stmt2 = JavaJimple.newAssignStmt(l2hash2, IntConstant.getInstance(1), noStmtPositionInfo);
    Stmt stmt3 =
        JavaJimple.newAssignStmt(
            l1hash3,
            JavaJimple.newAddExpr(l1hash1, IntConstant.getInstance(1)),
            noStmtPositionInfo);
    Stmt stmt4 =
        JavaJimple.newAssignStmt(
            l2hash4,
            JavaJimple.newAddExpr(l2hash2, IntConstant.getInstance(1)),
            noStmtPositionInfo);
    Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

    // set graph
    builder.addFlow(startingStmt, stmt1);
    builder.addFlow(stmt1, stmt2);
    builder.addFlow(stmt2, stmt3);
    builder.addFlow(stmt3, stmt4);
    builder.addFlow(stmt4, ret);

    // set first stmt
    builder.setStartingStmt(startingStmt);

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    return builder.build();
  }

  /** bodycreater for Loop */
  private Body createLoopBody() {

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, stack3, stack4);
    builder.setLocals(locals);

    Stmt stmt1 = JavaJimple.newAssignStmt(l1, IntConstant.getInstance(0), noStmtPositionInfo);
    Stmt stmt2 = JavaJimple.newAssignStmt(stack4, l1, noStmtPositionInfo);
    Stmt stmt3 = JavaJimple.newAssignStmt(stack3, IntConstant.getInstance(10), noStmtPositionInfo);
    Stmt stmt4 =
        JavaJimple.newIfStmt(
            JavaJimple.newGeExpr(stack4, stack3), noStmtPositionInfo); // branch to ret
    Stmt stmt5 =
        JavaJimple.newAssignStmt(
            l2, JavaJimple.newAddExpr(l1, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt6 =
        JavaJimple.newAssignStmt(
            l1, JavaJimple.newAddExpr(l2, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt7 =
        JavaJimple.newAssignStmt(
            l1, JavaJimple.newAddExpr(l1, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt8 = JavaJimple.newGotoStmt(noStmtPositionInfo); // goto stmt2
    Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

    // set graph
    builder.addFlow(startingStmt, stmt1);
    builder.addFlow(stmt1, stmt2);
    builder.addFlow(stmt2, stmt3);
    builder.addFlow(stmt3, stmt4);
    builder.addFlow(stmt4, stmt5);
    builder.addFlow(stmt4, ret);
    builder.addFlow(stmt5, stmt6);
    builder.addFlow(stmt6, stmt7);
    builder.addFlow(stmt7, stmt8);
    builder.addFlow(stmt8, stmt2);

    // build startingStmt
    builder.setStartingStmt(startingStmt);

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    return builder.build();
  }

  private Body createExpectedLoopBody() {

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, stack3, stack4, l1hash1, l1hash2);
    builder.setLocals(locals);

    Stmt stmt1 = JavaJimple.newAssignStmt(l1hash1, IntConstant.getInstance(0), noStmtPositionInfo);
    Stmt stmt2 = JavaJimple.newAssignStmt(stack4, l1hash1, noStmtPositionInfo);
    Stmt stmt3 = JavaJimple.newAssignStmt(stack3, IntConstant.getInstance(10), noStmtPositionInfo);
    Stmt stmt4 =
        JavaJimple.newIfStmt(
            JavaJimple.newGeExpr(stack4, stack3), noStmtPositionInfo); // branch to ret
    Stmt stmt5 =
        JavaJimple.newAssignStmt(
            l2, JavaJimple.newAddExpr(l1hash1, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt6 =
        JavaJimple.newAssignStmt(
            l1hash2, JavaJimple.newAddExpr(l2, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt7 =
        JavaJimple.newAssignStmt(
            l1hash1,
            JavaJimple.newAddExpr(l1hash2, IntConstant.getInstance(1)),
            noStmtPositionInfo);
    Stmt stmt8 = JavaJimple.newGotoStmt(noStmtPositionInfo); // goto stmt2
    Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

    // set graph
    builder.addFlow(startingStmt, stmt1);
    builder.addFlow(stmt1, stmt2);
    builder.addFlow(stmt2, stmt3);
    builder.addFlow(stmt3, stmt4);
    builder.addFlow(stmt4, stmt5);
    builder.addFlow(stmt4, ret);
    builder.addFlow(stmt5, stmt6);
    builder.addFlow(stmt6, stmt7);
    builder.addFlow(stmt7, stmt8);
    builder.addFlow(stmt8, stmt2);

    // build startingStmt
    builder.setStartingStmt(startingStmt);

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    return builder.build();
  }

  private static void assertLocalsEquiv(Set<Local> expected, Set<Local> actual) {
    assertNotNull(expected);
    assertNotNull(actual);
    assertEquals(expected.size(), actual.size());
    boolean isEqual = true;
    for (Local local : actual) {
      if (!expected.contains(local)) {
        isEqual = false;
        break;
      }
    }
    assertTrue(isEqual);
  }

  private void assertStmtGraphEquiv(StmtGraph expected, StmtGraph actual) {
    assertNotNull(expected);
    assertNotNull(actual);
    final boolean condition = expected.equivTo(actual);
    if (!condition) {
      System.out.println("expected:");
      System.out.println(Lists.newArrayList(expected.iterator()));
      System.out.println("actual:");
      System.out.println(Lists.newArrayList(actual.iterator()) + "\n");

      for (Stmt s : expected) {
        System.out.println(s + " => " + expected.successors(s));
      }
      System.out.println();
      for (Stmt s : actual) {
        System.out.println(s + " => " + actual.successors(s));
      }
    }
    assertEquals(Lists.newArrayList(expected.iterator()), Lists.newArrayList(actual.iterator()));
    assertTrue(condition);
  }
}
