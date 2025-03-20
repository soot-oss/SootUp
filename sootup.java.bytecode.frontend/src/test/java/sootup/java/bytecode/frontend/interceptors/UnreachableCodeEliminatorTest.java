package sootup.java.bytecode.frontend.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.AbstractConditionExpr;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.VoidType;
import sootup.core.util.ImmutableUtils;
import sootup.core.util.printer.BriefStmtPrinter;
import sootup.interceptors.UnreachableCodeEliminator;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

/**
 * @author Zun Wang
 */
public class UnreachableCodeEliminatorTest {

  public final BriefStmtPrinter briefStmtPrinter = new BriefStmtPrinter();

  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  JavaJimple javaJimple = JavaJimple.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();
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
  FallsThroughStmt startingStmt = JavaJimple.newIdentityStmt(l0, identityRef, noStmtPositionInfo);
  FallsThroughStmt stmt1 =
      JavaJimple.newAssignStmt(l1, IntConstant.getInstance(1), noStmtPositionInfo);
  FallsThroughStmt stmt2 =
      JavaJimple.newAssignStmt(l2, IntConstant.getInstance(2), noStmtPositionInfo);

  FallsThroughStmt stmt3 =
      JavaJimple.newAssignStmt(l3, IntConstant.getInstance(3), noStmtPositionInfo);

  BranchingStmt jGotoStmt = JavaJimple.newGotoStmt(noStmtPositionInfo);

  AbstractConditionExpr conditionExpr = JavaJimple.newEqExpr(l1, l2);
  BranchingStmt jIfStmt = JavaJimple.newIfStmt(conditionExpr, noStmtPositionInfo);

  FallsThroughStmt stmtinsideif =
      JavaJimple.newAssignStmt(l3, IntConstant.getInstance(5), noStmtPositionInfo);

  Stmt ret1 = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);
  Stmt ret2 = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

  FallsThroughStmt handlerStmt = JavaJimple.newIdentityStmt(stack0, idRef, noStmtPositionInfo);
  FallsThroughStmt beginStmt = JavaJimple.newAssignStmt(l3, stack0, noStmtPositionInfo);
  FallsThroughStmt endStmt =
      JavaJimple.newAssignStmt(l4, IntConstant.getInstance(4), noStmtPositionInfo);

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
    Set<Local> locals = new LinkedHashSet<>(Arrays.asList(l0, l1, l2, l3));

    builder.setLocals(locals);

    // build stmtsGraph for the builder
    final MutableStmtGraph stmtGraph = builder.getStmtGraph();
    stmtGraph.putEdge(startingStmt, stmt1);

    stmtGraph.putEdge(stmt1, ret1);
    stmtGraph.putEdge(stmt2, stmt3);
    stmtGraph.putEdge(stmt3, ret2);

    // set startingStmt
    stmtGraph.setStartingStmt(startingStmt);

    // set Position
    builder.setPosition(NoPositionInformation.getInstance());

    UnreachableCodeEliminator eliminator = new UnreachableCodeEliminator();
    eliminator.interceptBody(builder, new JavaView(Collections.emptyList()));

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
    Set<Local> locals = new LinkedHashSet<>(Arrays.asList(l0, l1, l3, l4, stack0));

    builder.setLocals(locals);

    // build stmtsGraph for the builder
    graph.putEdge(startingStmt, stmt1);
    graph.putEdge(stmt1, ret1);
    graph.addBlock(
        Arrays.asList(beginStmt, endStmt), Collections.singletonMap(exception, handlerStmt));

    // set startingStmt
    graph.setStartingStmt(startingStmt);

    new UnreachableCodeEliminator().interceptBody(builder, new JavaView(Collections.emptyList()));

    briefStmtPrinter.buildTraps(builder.getStmtGraph());
    assertEquals(0, briefStmtPrinter.getTraps().size());

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
    Set<Local> locals = new LinkedHashSet<>(Arrays.asList(l0, l1, l3, stack0));

    builder.setLocals(locals);

    // build stmtsGraph for the builder
    graph.putEdge(startingStmt, stmt1);
    graph.putEdge(stmt1, ret1);
    graph.addNode(beginStmt, Collections.singletonMap(exception, handlerStmt));
    graph.putEdge(handlerStmt, beginStmt);

    // set startingStmt
    graph.setStartingStmt(startingStmt);

    UnreachableCodeEliminator eliminator = new UnreachableCodeEliminator();
    eliminator.interceptBody(builder, new JavaView(Collections.emptyList()));

    briefStmtPrinter.buildTraps(builder.getStmtGraph());
    assertEquals(0, briefStmtPrinter.getTraps().size());

    Set<Stmt> expectedStmtsSet = ImmutableUtils.immutableSet(startingStmt, stmt1, ret1);
    assertEquals(expectedStmtsSet, builder.getStmtGraph().getNodes());
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
    graph.addBlock(Arrays.asList(handlerStmt, jGotoStmt));
    graph.putEdge(jGotoStmt, JGotoStmt.BRANCH_IDX, ret1);

    // set startingStmt
    graph.setStartingStmt(startingStmt);

    MutableStmtGraph inputGraph = new MutableBlockStmtGraph(builder.getStmtGraph());
    new UnreachableCodeEliminator().interceptBody(builder, new JavaView(Collections.emptyList()));

    assertEquals(inputGraph, builder.getStmtGraph());
  }

  @Test
  public void testUCERemoveUnreachableBlock() {
    // build an instance of BodyBuilder
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = new LinkedHashSet<>(Arrays.asList(l0, l1, l2, l3));
    builder.setLocals(locals);

    // build stmtsGraph for the builder
    final MutableStmtGraph stmtGraph = builder.getStmtGraph();
    stmtGraph.putEdge(startingStmt, stmt1);
    stmtGraph.putEdge(stmt1, stmt2);
    stmtGraph.putEdge(stmt2, jIfStmt);
    // stmtGraph.putEdge(jIfStmt, 0, stmtinsideif);
    stmtGraph.putEdge(jIfStmt, 1, stmt3);
    stmtGraph.putEdge(stmtinsideif, stmt3);
    stmtGraph.putEdge(stmt3, ret2);

    // set startingStmt
    stmtGraph.setStartingStmt(startingStmt);
    // set Position
    builder.setPosition(NoPositionInformation.getInstance());

    UnreachableCodeEliminator eliminator = new UnreachableCodeEliminator();
    eliminator.interceptBody(builder, new JavaView(Collections.emptyList()));

    // stmtinsideif got eliminated
    Set<Stmt> expectedStmtsSet =
        ImmutableUtils.immutableSet(startingStmt, stmt1, stmt2, jIfStmt, stmt3, ret1);
    assertEquals(expectedStmtsSet.size(), builder.getStmtGraph().getNodes().size());
  }

  @Test
  public void testUCERemoveUnreachableBlock1() {
    // build an instance of BodyBuilder
    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // add locals into builder
    Set<Local> locals = new LinkedHashSet<>(Arrays.asList(l0, l1, l2, l3));
    builder.setLocals(locals);

    // build stmtsGraph for the builder
    final MutableStmtGraph stmtGraph = builder.getStmtGraph();
    stmtGraph.putEdge(startingStmt, stmt1);
    stmtGraph.putEdge(stmt1, stmt2);
    stmtGraph.putEdge(stmt2, jGotoStmt);
    stmtGraph.putEdge(jGotoStmt, JGotoStmt.BRANCH_IDX, ret1);
    stmtGraph.putEdge(stmtinsideif, stmt3);
    stmtGraph.putEdge(stmt3, ret1);

    // set startingStmt
    stmtGraph.setStartingStmt(startingStmt);
    // set Position
    builder.setPosition(NoPositionInformation.getInstance());

    UnreachableCodeEliminator eliminator = new UnreachableCodeEliminator();
    eliminator.interceptBody(builder, new JavaView(Collections.emptyList()));

    // stmtinsideif, stmt3 got eliminated
    Set<Stmt> expectedStmtsSet =
        ImmutableUtils.immutableSet(startingStmt, stmt1, stmt2, jGotoStmt, ret1);
    assertEquals(expectedStmtsSet.size(), builder.getStmtGraph().getNodes().size());
  }
}
