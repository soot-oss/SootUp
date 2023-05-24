package sootup.java.bytecode.interceptors;

import categories.Java8Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.Position;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.Type;
import sootup.core.types.VoidType;
import sootup.core.util.DotExporter;
import sootup.core.util.ImmutableUtils;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;

/** @author Zun Wang */
@Category(Java8Test.class)
public class LocalPackerTest {
  // Preparation
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  JavaJimple javaJimple = JavaJimple.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

  JavaClassType classType = factory.getClassType("Test");
  JavaClassType intType = factory.getClassType("int");
  JavaClassType exception = factory.getClassType("Exception");

  IdentityRef identityRef = JavaJimple.newThisRef(classType);

  IdentityRef identityRef0 = JavaJimple.newParameterRef(intType, 0);
  IdentityRef identityRef1 = JavaJimple.newParameterRef(intType, 1);
  IdentityRef caughtExceptionRef = javaJimple.newCaughtExceptionRef();

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
  Local l4 = JavaJimple.newLocal("l4", exception);
  Local el4 = JavaJimple.newLocal("l3", exception);

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
  Stmt trapHandler = JavaJimple.newIdentityStmt(l4, caughtExceptionRef, noStmtPositionInfo);
  Stmt throwStmt = JavaJimple.newThrowStmt(l4, noStmtPositionInfo);

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
  Stmt etrapHandler = JavaJimple.newIdentityStmt(el4, caughtExceptionRef, noStmtPositionInfo);
  Stmt ethrowStmt = JavaJimple.newThrowStmt(el4, noStmtPositionInfo);

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
   *    l1#5 = l1#5 + 1;
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
   *    goto labe1;
   *  label2:
   *     return;
   * </pre>
   */
  @Test
  public void testLocalPacker() {
    Body.BodyBuilder builder = createBodyBuilder();

    new LocalPacker().interceptBody(builder, null);
    Body body = builder.build();

    Body expectedBody = createExpectedBody();

    AssertUtils.assertLocalsEquiv(expectedBody, body);
    AssertUtils.assertStmtGraphEquiv(expectedBody, body);
  }

  /**
   *
   *
   * <pre>
   *    Test l0;
   *    int l1, l2, l3, l1#1, l2#2, l2#3, l1#4, l1#5;
   *    Exception l4;
   *
   *    l0 := @this Test
   *    l1#1 := @parameter0: int;
   *    l2#2 := @parameter1: int;
   *    l3 = 10;
   *    l2#3 = l3;
   *    l1#4 = 0;
   *    l1#5 = l1#4 + 1;
   *  label1:
   *    l1#5 = l1#5 + 1;
   *  label2:
   *    if l1#5 > l3 goto label2;
   *    goto label1;
   *  label3:
   *    l4 := @caughtexception;
   *    throw l4;
   *  label4:
   *    return;
   *
   *  catch Exception from label1 to label2 with label3;
   * </pre>
   *
   * to:
   *
   * <pre>
   *    Test l0;
   *    int l1, l2;
   *    Exception l3;
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
   *  label2:
   *    if l2 > l1 goto label2;
   *    goto labe1;
   *  label3:
   *    l3 := @caughtexception;
   *    throw l3;
   *  label4:
   *     return;
   *
   *  catch Exception from label1 to label2 with label3;
   * </pre>
   */
  @Test
  @Ignore("FIXME: does currently not work - Isssue #487")
  public void testLocalPackerWithTrap() {
    Body.BodyBuilder builder = createTrapBody();

    System.out.println(DotExporter.createUrlToWebeditor(builder.getStmtGraph()));

    LocalPacker localPacker = new LocalPacker();
    localPacker.interceptBody(builder, null);

    Body body = builder.build();
    Body expectedBody = createExpectedTrapBody().build();

    AssertUtils.assertLocalsEquiv(expectedBody, body);
    AssertUtils.assertStmtGraphEquiv(expectedBody, body);
  }

  private Body.BodyBuilder createBodyBuilder() {

    final MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    Body.BodyBuilder builder = Body.builder(graph);

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
    graph.putEdge(startingStmt, identityStmt0);
    graph.putEdge(identityStmt0, identityStmt1);
    graph.putEdge(identityStmt1, stmt1);
    graph.putEdge(stmt1, stmt2);
    graph.putEdge(stmt2, stmt3);
    graph.putEdge(stmt3, stmt4);
    graph.putEdge(stmt4, stmt5);
    graph.putEdge(stmt5, stmt6);
    graph.putEdge(stmt6, gt);
    graph.putEdge(gt, stmt5);
    graph.putEdge(stmt6, ret);

    builder.setStartingStmt(startingStmt);

    return builder;
  }

  private Body createExpectedBody() {

    final MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    Body.BodyBuilder builder = Body.builder(graph);

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
    graph.putEdge(startingStmt, eidentityStmt0);
    graph.putEdge(eidentityStmt0, eidentityStmt1);
    graph.putEdge(eidentityStmt1, estmt1);
    graph.putEdge(estmt1, estmt2);
    graph.putEdge(estmt2, estmt3);
    graph.putEdge(estmt3, estmt4);
    graph.putEdge(estmt4, estmt5);
    graph.putEdge(estmt5, estmt6);
    graph.putEdge(estmt6, gt);
    graph.putEdge(gt, estmt5);
    graph.putEdge(estmt6, ret);

    builder.setStartingStmt(startingStmt);

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    return builder.build();
  }

  private Body.BodyBuilder createTrapBody() {

    final MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    Body.BodyBuilder builder = Body.builder(graph);

    List<Type> parameters = new ArrayList<>();
    parameters.add(intType);
    // parameters.add(doubleType);
    MethodSignature methodSignature =
        new MethodSignature(classType, "test", parameters, VoidType.getInstance());
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals =
        ImmutableUtils.immutableSet(
            l0, l1, l2, l3, l4, l1hash1, l2hash2, l2hash3, l1hash4, l1hash5);
    builder.setLocals(locals);

    // build stmtGraph
    graph.addNode(stmt5, Collections.singletonMap(exception, etrapHandler));

    graph.putEdge(startingStmt, identityStmt0);
    graph.putEdge(identityStmt0, identityStmt1);
    graph.putEdge(identityStmt1, stmt1);
    graph.putEdge(stmt1, stmt2);
    graph.putEdge(stmt2, stmt3);
    graph.putEdge(stmt3, stmt4);
    graph.putEdge(stmt4, stmt5);
    graph.putEdge(stmt5, stmt6);
    graph.putEdge(stmt6, gt);
    graph.putEdge(gt, stmt5);
    graph.putEdge(stmt6, ret);
    graph.putEdge(trapHandler, throwStmt);

    builder.setStartingStmt(startingStmt);

    return builder;
  }

  private Body.BodyBuilder createExpectedTrapBody() {

    final MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    Body.BodyBuilder builder = Body.builder(graph);

    List<Type> parameters = new ArrayList<>();
    parameters.add(intType);

    MethodSignature methodSignature =
        new MethodSignature(classType, "test", parameters, VoidType.getInstance());
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, el4);
    builder.setLocals(locals);

    // build stmtGraph
    graph.putEdge(startingStmt, eidentityStmt0);
    graph.putEdge(eidentityStmt0, eidentityStmt1);
    graph.putEdge(eidentityStmt1, estmt1);
    graph.putEdge(estmt1, estmt2);
    graph.putEdge(estmt2, estmt3);
    graph.putEdge(estmt3, estmt4);
    graph.addNode(estmt5, Collections.singletonMap(exception, etrapHandler));
    graph.putEdge(estmt4, estmt5);

    graph.putEdge(estmt6, gt);
    graph.putEdge(gt, estmt5);
    graph.putEdge(estmt6, ret);
    graph.putEdge(etrapHandler, ethrowStmt);

    builder.setStartingStmt(startingStmt);

    return builder;
  }
}
