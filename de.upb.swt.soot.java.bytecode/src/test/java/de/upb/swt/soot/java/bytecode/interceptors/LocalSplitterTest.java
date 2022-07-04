package de.upb.swt.soot.java.bytecode.interceptors;

import categories.Java8Test;
import de.upb.swt.soot.core.graph.MutableBlockStmtGraph;
import de.upb.swt.soot.core.graph.MutableStmtGraph;
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.ref.IdentityRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
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
  Local l3 = JavaJimple.newLocal("l3", intType);
  Local stack3 = JavaJimple.newLocal("stack3", intType);
  Local stack4 = JavaJimple.newLocal("stack4", intType);
  Local l1hash1 = JavaJimple.newLocal("l1#1", intType);
  Local l1hash2 = JavaJimple.newLocal("l1#2", intType);
  Local l1hash3 = JavaJimple.newLocal("l1#3", intType);
  Local l2hash2 = JavaJimple.newLocal("l2#2", intType);
  Local l2hash4 = JavaJimple.newLocal("l2#4", intType);

  ClassType exception = factory.getClassType("Exception");
  JavaJimple javaJimple = JavaJimple.getInstance();
  IdentityRef caughtExceptionRef = javaJimple.newCaughtExceptionRef();
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
    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());
    LocalSplitter localSplitter = new LocalSplitter();
    localSplitter.interceptBody(builder);
    Body expectedBody = createExpectedBBBody();

    final Body interceptedBody = builder.build();
    // check newBody's locals
    AssertUtils.assertLocalsEquiv(expectedBody, interceptedBody);

    // check newBody's stmtGraph
    AssertUtils.assertStmtGraphEquiv(expectedBody, interceptedBody);
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
    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());
    LocalSplitter localSplitter = new LocalSplitter();
    localSplitter.interceptBody(builder);
    Body expectedBody = createExpectedMuiltilocalsBody();

    // check newBody's locals
    AssertUtils.assertLocalsEquiv(expectedBody, builder.build());

    // check newBody's stmtGraph
    AssertUtils.assertStmtGraphEquiv(expectedBody, builder.build());
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
    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());
    LocalSplitter localSplitter = new LocalSplitter();
    localSplitter.interceptBody(builder);
    Body expectedBody = createExpectedLoopBody();

    // check newBody's locals
    AssertUtils.assertLocalsEquiv(expectedBody, builder.build());

    // check newBody's stmtGraph
    AssertUtils.assertStmtGraphEquiv(expectedBody, builder.build());
  }

  /**
   * for(int i = 0; i < 10; i++){ i = i + 1 } transform:
   *
   * <pre>
   *    l0 := @this Test
   *    l1 = 0
   *    l1 = 1
   *    l2 = 2
   *    return
   *    $stack3 := @caughtexception
   *    l3 = l1
   *    goto return
   * </pre>
   *
   * to:
   *
   * <pre>
   *    l0 := @this Test
   *    l1#1 = 0
   *    l1#2 = 1
   *    l2 = 2
   *    return
   *    $stack3 := @caughtexception
   *    l3 = l1#2
   *    goto return
   * </pre>
   */
  @Test
  public void testLocalSplitterInTraps() {

    Body.BodyBuilder builder = createTrapBody();
    new LocalSplitter().interceptBody(builder);
    Body expectedBody = createExpectedTrapBody();

    // check newBody's locals
    AssertUtils.assertLocalsEquiv(expectedBody, builder.build());

    // check newBody's stmtGraph
    AssertUtils.assertStmtGraphEquiv(expectedBody, builder.build());
  }

  /** bodycreater for BinaryBranches */
  private Body createBBBody() {
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    Body.BodyBuilder builder = Body.builder(graph);
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

    graph.addBlock(Arrays.asList(startingStmt, stmt1, stmt2), Collections.emptyMap());
    graph.setEdges(stmt2, Arrays.asList(stmt3, stmt5));
    graph.addBlock(Arrays.asList(stmt3, stmt4), Collections.emptyMap());
    graph.addBlock(Arrays.asList(stmt5, stmt6), Collections.emptyMap());
    graph.putEdge(stmt4, ret);
    graph.putEdge(stmt6, ret);

    graph.setStartingStmt(startingStmt);

    /* set graph
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
    */

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    return builder.build();
  }

  private Body createExpectedBBBody() {

    MutableStmtGraph graph = new MutableBlockStmtGraph();
    Body.BodyBuilder builder = Body.builder(graph);
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

    graph.addBlock(Arrays.asList(startingStmt, stmt1, stmt2), Collections.emptyMap());
    graph.setEdges(stmt2, Arrays.asList(stmt3, stmt5));
    graph.addBlock(Arrays.asList(stmt3, stmt4), Collections.emptyMap());
    graph.putEdge(stmt4, ret);
    graph.addBlock(Arrays.asList(stmt5, stmt6), Collections.emptyMap());
    graph.putEdge(stmt6, ret);

    graph.setStartingStmt(startingStmt);

    /* set graph
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
    */
    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    return builder.build();
  }

  /** bodycreater for multilocals */
  private Body createMultilocalsBody() {

    MutableStmtGraph graph = new MutableBlockStmtGraph();
    Body.BodyBuilder builder = Body.builder(graph);
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

    graph.addBlock(
        Arrays.asList(startingStmt, stmt1, stmt2, stmt3, stmt4, ret), Collections.emptyMap());
    graph.setStartingStmt(startingStmt);

    /* set graph
        builder.addFlow(startingStmt, stmt1);
        builder.addFlow(stmt1, stmt2);
        builder.addFlow(stmt2, stmt3);
        builder.addFlow(stmt3, stmt4);
        builder.addFlow(stmt4, ret);

        // set first stmt
        builder.setStartingStmt(startingStmt);
    */

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    return builder.build();
  }

  private Body createExpectedMuiltilocalsBody() {

    MutableStmtGraph graph = new MutableBlockStmtGraph();
    Body.BodyBuilder builder = Body.builder(graph);
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

    graph.addBlock(
        Arrays.asList(startingStmt, stmt1, stmt2, stmt3, stmt4, ret), Collections.emptyMap());
    graph.setStartingStmt(startingStmt);

    /* set graph
    builder.addFlow(startingStmt, stmt1);
    builder.addFlow(stmt1, stmt2);
    builder.addFlow(stmt2, stmt3);
    builder.addFlow(stmt3, stmt4);
    builder.addFlow(stmt4, ret);

    // set first stmt
    builder.setStartingStmt(startingStmt);
    */

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    return builder.build();
  }

  /** bodycreater for Loop */
  private Body createLoopBody() {

    MutableStmtGraph graph = new MutableBlockStmtGraph();
    Body.BodyBuilder builder = Body.builder(graph);
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

    graph.addBlock(Arrays.asList(startingStmt, stmt1, stmt2, stmt3, stmt4), Collections.emptyMap());
    graph.setEdges(stmt4, Arrays.asList(stmt5, ret));
    graph.addBlock(Arrays.asList(stmt5, stmt6, stmt7, stmt8), Collections.emptyMap());
    graph.putEdge(stmt8, stmt2);

    graph.setStartingStmt(startingStmt);

    /* set graph
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
    */

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    return builder.build();
  }

  private Body createExpectedLoopBody() {

    MutableStmtGraph graph = new MutableBlockStmtGraph();
    Body.BodyBuilder builder = Body.builder(graph);
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

    graph.addBlock(Arrays.asList(startingStmt, stmt1, stmt2, stmt3, stmt4), Collections.emptyMap());
    graph.setEdges(stmt4, Arrays.asList(stmt5, ret));
    graph.addBlock(Arrays.asList(stmt5, stmt6, stmt7, stmt8), Collections.emptyMap());
    graph.putEdge(stmt8, stmt2);

    graph.setStartingStmt(startingStmt);

    /* set graph
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
    */

    // build startingStmt
    builder.setStartingStmt(startingStmt);

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    return builder.build();
  }

  private Body.BodyBuilder createTrapBody() {
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    Body.BodyBuilder builder = Body.builder(graph);
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, stack3, l3);

    builder.setLocals(locals);

    Stmt stmt1 = JavaJimple.newAssignStmt(l1, IntConstant.getInstance(0), noStmtPositionInfo);
    Stmt stmt2 = JavaJimple.newAssignStmt(l1, IntConstant.getInstance(1), noStmtPositionInfo);
    Stmt stmt3 = JavaJimple.newAssignStmt(l2, IntConstant.getInstance(2), noStmtPositionInfo);
    Stmt stmt4 = JavaJimple.newIdentityStmt(stack3, caughtExceptionRef, noStmtPositionInfo);
    Stmt stmt5 = JavaJimple.newAssignStmt(l3, l1, noStmtPositionInfo);
    Stmt stmt6 = JavaJimple.newGotoStmt(noStmtPositionInfo);
    Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

    // build graph
    graph.addBlock(
        Arrays.asList(startingStmt, stmt1, stmt2), Collections.singletonMap(exception, stmt4));
    graph.addBlock(Arrays.asList(stmt4, stmt5, stmt6), Collections.emptyMap());
    graph.addNode(stmt3);
    graph.putEdge(stmt2, stmt3);
    graph.putEdge(stmt3, ret);
    graph.putEdge(stmt6, ret);

    graph.setStartingStmt(startingStmt);

    return builder;
  }

  private Body createExpectedTrapBody() {

    MutableStmtGraph graph = new MutableBlockStmtGraph();
    Body.BodyBuilder builder = Body.builder(graph);
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, l3, stack3, l1hash1, l1hash2);

    builder.setLocals(locals);

    Stmt stmt1 = JavaJimple.newAssignStmt(l1hash1, IntConstant.getInstance(0), noStmtPositionInfo);
    Stmt stmt2 = JavaJimple.newAssignStmt(l1hash2, IntConstant.getInstance(1), noStmtPositionInfo);
    Stmt stmt3 = JavaJimple.newAssignStmt(l2, IntConstant.getInstance(2), noStmtPositionInfo);
    Stmt stmt4 = JavaJimple.newIdentityStmt(stack3, caughtExceptionRef, noStmtPositionInfo);
    Stmt stmt5 = JavaJimple.newAssignStmt(l3, l1hash2, noStmtPositionInfo);
    Stmt stmt6 = JavaJimple.newGotoStmt(noStmtPositionInfo);
    Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

    graph.addBlock(
        Arrays.asList(startingStmt, stmt1, stmt2), Collections.singletonMap(exception, stmt4));
    graph.addBlock(Arrays.asList(stmt4, stmt5, stmt6), Collections.emptyMap());
    graph.putEdge(stmt2, stmt3);
    graph.putEdge(stmt3, ret);
    graph.putEdge(stmt6, ret);

    graph.setStartingStmt(startingStmt);

    return builder.build();
  }
}
