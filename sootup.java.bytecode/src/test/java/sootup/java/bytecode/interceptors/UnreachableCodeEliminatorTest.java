package sootup.java.bytecode.interceptors;

import static org.junit.Assert.*;

import categories.Java8Test;
import java.util.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Trap;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.VoidType;
import sootup.core.util.ImmutableUtils;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;

/** @author Zun Wang */
@Category(Java8Test.class)
public class UnreachableCodeEliminatorTest {

  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  JavaJimple javaJimple = JavaJimple.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();
  JavaClassType classType = factory.getClassType("Test");
  MethodSignature methodSignature =
      new MethodSignature(classType, "test", Collections.emptyList(), VoidType.getInstance());
  JavaClassType refType = factory.getClassType("ref");
  ClassType exception = factory.getClassType("RuntimeException");

  IdentityRef identityRef = JavaJimple.newThisRef(classType);

  // build locals
  Local l0 = JavaJimple.newLocal("l0", refType);
  Local l1 = JavaJimple.newLocal("l1", PrimitiveType.getInt());
  Local l2 = JavaJimple.newLocal("l2", PrimitiveType.getInt());
  Local l3 = JavaJimple.newLocal("l3", PrimitiveType.getInt());
  Local l4 = JavaJimple.newLocal("l3", PrimitiveType.getInt());
  Local stack0 = JavaJimple.newLocal("stack0", refType);
  IdentityRef idRef = javaJimple.newCaughtExceptionRef();

  // build stmts
  Stmt startingStmt = JavaJimple.newIdentityStmt(l0, identityRef, noStmtPositionInfo);
  Stmt stmt1 = JavaJimple.newAssignStmt(l1, IntConstant.getInstance(1), noStmtPositionInfo);
  Stmt stmt2 = JavaJimple.newAssignStmt(l2, IntConstant.getInstance(2), noStmtPositionInfo);

  Stmt stmt3 = JavaJimple.newAssignStmt(l3, IntConstant.getInstance(3), noStmtPositionInfo);

  Stmt jump = JavaJimple.newGotoStmt(noStmtPositionInfo);

  Stmt ret1 = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);
  Stmt ret2 = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

  Stmt handlerStmt = JavaJimple.newIdentityStmt(stack0, idRef, noStmtPositionInfo);
  Stmt beginStmt = JavaJimple.newAssignStmt(l3, stack0, noStmtPositionInfo);
  Stmt endStmt = JavaJimple.newAssignStmt(l4, IntConstant.getInstance(4), noStmtPositionInfo);

  Trap trap2 = JavaJimple.newTrap(exception, beginStmt, beginStmt, handlerStmt);

  /**
   * Test the simpleBody l0:= @this Test -> l1 = 1 -> return l2 = 2 -> l3 = 3 -> return l2 = 2 and
   * l3 = 3 are unreachable
   */
  @Test
  public void testSimpleBody() {

    // build an instance of BodyBuilder
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, l3);

    builder.setLocals(locals);

    // build stmtsGraph for the builder
    builder.addFlow(startingStmt, stmt1);

    builder.addFlow(stmt1, ret1);
    builder.addFlow(stmt2, stmt3);
    builder.addFlow(stmt3, ret2);

    // set startingStmt
    builder.setStartingStmt(startingStmt);

    // set Position
    builder.setPosition(NoPositionInformation.getInstance());

    UnreachableCodeEliminator eliminator = new UnreachableCodeEliminator();
    eliminator.interceptBody(builder, null);

    Set<Stmt> expectedStmtsSet = ImmutableUtils.immutableSet(startingStmt, stmt1, ret1);
    AssertUtils.assertSetsEquiv(expectedStmtsSet, builder.getStmtGraph().getNodes());
  }

  /**
   * Test the Body with unreachable trap l0:= @this Test -> l1 = 1 -> return
   *
   * <p>trap: stack0 := @caughtexception l3 = stack0 l4 = 4
   */
  @Test
  public void testTrappedBody1() {

    // build an instance of BodyBuilder
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    Body.BodyBuilder builder = Body.builder(graph);
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l3, l4, stack0);

    builder.setLocals(locals);

    // build stmtsGraph for the builder
    graph.putEdge(startingStmt, stmt1);
    graph.putEdge(stmt1, ret1);
    graph.addBlock(
        Arrays.asList(beginStmt, endStmt), Collections.singletonMap(exception, handlerStmt));

    // set startingStmt
    graph.setStartingStmt(startingStmt);

    new UnreachableCodeEliminator().interceptBody(builder, null);

    assertEquals(0, builder.getStmtGraph().getTraps().size());

    Set<Stmt> expectedStmtsSet = ImmutableUtils.immutableSet(startingStmt, stmt1, ret1);
    AssertUtils.assertSetsEquiv(expectedStmtsSet, builder.getStmtGraph().getNodes());
  }

  /**
   * Test the Body with unreachable trap l0:= @this Test -> l1 = 1 -> return
   *
   * <p>trap: stack0 := @caughtexception l3 = stack0
   */
  @Test
  public void testTrappedBody2() {

    // build an instance of BodyBuilder
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    Body.BodyBuilder builder = Body.builder(graph);
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l3, stack0);

    builder.setLocals(locals);

    // build stmtsGraph for the builder
    graph.putEdge(startingStmt, stmt1);
    graph.putEdge(stmt1, ret1);
    graph.addNode(beginStmt, Collections.singletonMap(exception, handlerStmt));
    graph.putEdge(handlerStmt, beginStmt);

    // set startingStmt
    builder.setStartingStmt(startingStmt);

    UnreachableCodeEliminator eliminator = new UnreachableCodeEliminator();
    eliminator.interceptBody(builder, null);

    assertEquals(0, builder.getStmtGraph().getTraps().size());

    Set<Stmt> expectedStmtsSet = ImmutableUtils.immutableSet(startingStmt, stmt1, ret1);
    Assert.assertEquals(expectedStmtsSet, builder.getStmtGraph().getNodes());
    AssertUtils.assertSetsEquiv(expectedStmtsSet, builder.getStmtGraph().getNodes());
  }

  @Test
  public void testTrappedBody3() {
    // stmts & traphandler are all reachable!

    // build an instance of BodyBuilder
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    Body.BodyBuilder builder = Body.builder(graph);
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l3, l4, stack0);

    builder.setLocals(locals);

    // build stmtsGraph for the builder
    graph.addBlock(
        Arrays.asList(startingStmt, stmt1, ret1), Collections.singletonMap(exception, handlerStmt));
    graph.addBlock(Arrays.asList(handlerStmt, jump));
    graph.putEdge(jump, ret1);

    // set startingStmt
    graph.setStartingStmt(startingStmt);

    MutableStmtGraph inputGraph = new MutableBlockStmtGraph(builder.getStmtGraph());
    new UnreachableCodeEliminator().interceptBody(builder, null);

    assertEquals(inputGraph, builder.getStmtGraph());
  }
}
