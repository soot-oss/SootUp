package de.upb.swt.soot.test.core.model;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.graph.Block;
import de.upb.swt.soot.core.graph.BlockGraph;
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
public class BlockGraphTest {

  // Preparation
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  JavaJimple javaJimple = JavaJimple.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

  JavaClassType intType = factory.getClassType("int");
  JavaClassType classType = factory.getClassType("Test");
  MethodSignature methodSignature =
      new MethodSignature(classType, "test", Collections.emptyList(), VoidType.getInstance());
  IdentityRef identityRef = JavaJimple.newThisRef(classType);
  JavaClassType refType = factory.getClassType("ref");
  IdentityRef caughtExceptionRef = javaJimple.newCaughtExceptionRef();
  ClassType exception = factory.getClassType("Exception");

  // build locals
  Local l0 = JavaJimple.newLocal("l0", intType);
  Local l1 = JavaJimple.newLocal("l1", intType);
  Local l2 = JavaJimple.newLocal("l2", intType);
  Local l3 = JavaJimple.newLocal("l3", intType);
  Local l4 = JavaJimple.newLocal("l4", intType);
  Local stack5 = JavaJimple.newLocal("stack5", refType);
  Local stack6 = JavaJimple.newLocal("stack6", refType);

  Stmt startingStmt = JavaJimple.newIdentityStmt(l0, identityRef, noStmtPositionInfo);
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

  Stmt stmt7 = JavaJimple.newAssignStmt(l4, l1, noStmtPositionInfo);
  Stmt stmt8 = JavaJimple.newAssignStmt(l3, IntConstant.getInstance(10), noStmtPositionInfo);
  Stmt stmt9 =
      JavaJimple.newIfStmt(JavaJimple.newGeExpr(l4, l3), noStmtPositionInfo); // branch to ret
  Stmt stmt10 =
      JavaJimple.newAssignStmt(
          l2, JavaJimple.newAddExpr(l1, IntConstant.getInstance(1)), noStmtPositionInfo);
  Stmt stmt11 =
      JavaJimple.newAssignStmt(
          l1, JavaJimple.newAddExpr(l2, IntConstant.getInstance(1)), noStmtPositionInfo);
  Stmt gotoStmt1 = JavaJimple.newGotoStmt(noStmtPositionInfo);
  Stmt gotoStmt2 = JavaJimple.newGotoStmt(noStmtPositionInfo);

  Stmt stack5Stmt = JavaJimple.newIdentityStmt(stack5, caughtExceptionRef, noStmtPositionInfo);
  Stmt stack6Stmt = JavaJimple.newIdentityStmt(stack6, caughtExceptionRef, noStmtPositionInfo);

  JTrap trap1 = new JTrap(exception, stmt1, ret, stack5Stmt);
  JTrap trap2 = new JTrap(exception, stmt10, stmt7, stack6Stmt);

  @Test
  public void testBlockGraphWithBranch() {
    Body body = createBBBody();
    BlockGraph graph = new BlockGraph(body.getStmtGraph());

    // expected Blocks in BlockGraph
    Block eblock1 = new Block(startingStmt, stmt2);
    Block eblock2 = new Block(stmt3, stmt4);
    Block eblock3 = new Block(stmt5, stmt6);
    Block eblock4 = new Block(ret, ret);
    List<Block> expectedBlocks = ImmutableUtils.immutableList(eblock1, eblock2, eblock3, eblock4);
    List<Block> actualBlocks = graph.getBlocks();

    assertTrue(graph.getStartingBlock().equals(eblock1));
    assertEquals(expectedBlocks.size(), actualBlocks.size());

    for (int i = 0; i < 4; i++) {
      assertTrue(expectedBlocks.get(i).equals(actualBlocks.get(i)));
    }

    List<Stmt> stmts = body.getStmts();
    for (int i = 0; i < stmts.size(); i++) {
      Stmt stmt = stmts.get(i);
      if (i < 3) {
        assertTrue(graph.blockPredecessors(stmt).isEmpty());
        assertTrue(graph.blockSuccessors(stmt).get(0).equals(eblock2));
        assertTrue(graph.blockSuccessors(stmt).get(1).equals(eblock3));
        assertTrue(graph.getBlock(stmt).equals(eblock1));
      } else if (i < 5) {
        assertTrue(graph.blockPredecessors(stmt).get(0).equals(eblock1));
        assertTrue(graph.blockSuccessors(stmt).get(0).equals(eblock4));
        assertTrue(graph.getBlock(stmt).equals(eblock2));
      } else if (i < 7) {
        assertTrue(graph.blockPredecessors(stmt).get(0).equals(eblock1));
        assertTrue(graph.blockSuccessors(stmt).get(0).equals(eblock4));
        assertTrue(graph.getBlock(stmt).equals(eblock3));
      } else {
        assertTrue(graph.blockPredecessors(stmt).get(0).equals(eblock2));
        assertTrue(graph.blockPredecessors(stmt).get(1).equals(eblock3));
        assertTrue(graph.blockSuccessors(stmt).isEmpty());
        assertTrue(graph.getBlock(stmt).equals(eblock4));
      }
    }

    List<Stmt> expectedBlockStmts1 = ImmutableUtils.immutableList(startingStmt, stmt1, stmt2);
    List<Stmt> expectedBlockStmts2 = ImmutableUtils.immutableList(stmt3, stmt4);
    List<Stmt> expectedBlockStmts3 = ImmutableUtils.immutableList(stmt5, stmt6);
    List<Stmt> expectedBlockStmts4 = ImmutableUtils.immutableList(ret);

    int i = 0;
    for (Block block : graph.getBlocks()) {
      List<Stmt> blockStmts = graph.getBlockStmts(block);
      List<Stmt> expectedBlockStmts;
      if (i == 0) {
        expectedBlockStmts = expectedBlockStmts1;
      } else if (i == 1) {
        expectedBlockStmts = expectedBlockStmts2;
      } else if (i == 2) {
        expectedBlockStmts = expectedBlockStmts3;
      } else {
        expectedBlockStmts = expectedBlockStmts4;
      }
      for (int j = 0; j < expectedBlockStmts.size(); j++) {
        assertTrue(blockStmts.get(j) == expectedBlockStmts.get(j));
      }
      i++;
    }
  }

  @Test
  public void testBlockGraphWithLoop() {
    Body body = createLoopBody();
    BlockGraph graph = new BlockGraph(body.getStmtGraph());

    // expected Blocks in BlockGraph
    Block eblock1 = new Block(startingStmt, stmt1);
    Block eblock2 = new Block(stmt7, stmt9);
    Block eblock3 = new Block(stmt10, stmt4);
    Block eblock4 = new Block(ret, ret);
    List<Block> expectedBlocks = ImmutableUtils.immutableList(eblock1, eblock2, eblock3, eblock4);
    List<Block> actualBlocks = graph.getBlocks();

    assertTrue(graph.getStartingBlock().equals(eblock1));
    assertEquals(expectedBlocks.size(), actualBlocks.size());

    for (int i = 0; i < 4; i++) {
      assertTrue(expectedBlocks.get(i).equals(actualBlocks.get(i)));
    }

    List<Stmt> stmts = body.getStmts();
    for (int i = 0; i < stmts.size(); i++) {
      Stmt stmt = stmts.get(i);
      if (i < 2) {
        assertTrue(graph.blockPredecessors(stmt).isEmpty());
        assertTrue(graph.blockSuccessors(stmt).get(0).equals(eblock2));
        assertTrue(graph.getBlock(stmt).equals(eblock1));
      } else if (i < 5) {
        assertTrue(graph.blockPredecessors(stmt).get(0).equals(eblock1));
        assertTrue(graph.blockPredecessors(stmt).get(1).equals(eblock3));
        assertTrue(graph.blockSuccessors(stmt).get(0).equals(eblock3));
        assertTrue(graph.blockSuccessors(stmt).get(1).equals(eblock4));
        assertTrue(graph.getBlock(stmt).equals(eblock2));
      } else if (i < 9) {
        assertTrue(graph.blockPredecessors(stmt).get(0).equals(eblock2));
        assertTrue(graph.blockSuccessors(stmt).get(0).equals(eblock2));
        assertTrue(graph.getBlock(stmt).equals(eblock3));
      } else {
        assertTrue(graph.blockPredecessors(stmt).get(0).equals(eblock2));
        assertTrue(graph.blockSuccessors(stmt).isEmpty());
        assertTrue(graph.getBlock(stmt).equals(eblock4));
      }
    }
    List<Stmt> expectedBlockStmts1 = ImmutableUtils.immutableList(startingStmt, stmt1);
    List<Stmt> expectedBlockStmts2 = ImmutableUtils.immutableList(stmt7, stmt8, stmt9);
    List<Stmt> expectedBlockStmts3 = ImmutableUtils.immutableList(stmt10, stmt11, stmt5, stmt4);
    List<Stmt> expectedBlockStmts4 = ImmutableUtils.immutableList(ret);

    int i = 0;
    for (Block block : graph.getBlocks()) {
      List<Stmt> blockStmts = graph.getBlockStmts(block);
      List<Stmt> expectedBlockStmts;
      if (i == 0) {
        expectedBlockStmts = expectedBlockStmts1;
      } else if (i == 1) {
        expectedBlockStmts = expectedBlockStmts2;
      } else if (i == 2) {
        expectedBlockStmts = expectedBlockStmts3;
      } else {
        expectedBlockStmts = expectedBlockStmts4;
      }
      for (int j = 0; j < expectedBlockStmts.size(); j++) {
        assertTrue(blockStmts.get(j) == expectedBlockStmts.get(j));
      }
      i++;
    }
  }

  @Test
  public void testBlockGraphWithTrap() {
    Body body = createTrapBody();
    BlockGraph graph = new BlockGraph(body.getStmtGraph());
    List<Block> actualBlocks = graph.getBlocks();

    /*for(Block b : actualBlocks){
      System.out.println(b.toString());
    }*/

  }

  @Test
  public void testAddNewPhiStmt() {
    Body body = createBBBody();
    BlockGraph graph = new BlockGraph(body.getStmtGraph());
    List<Block> actualBlocks = graph.getBlocks();

    List<Local> args = ImmutableUtils.immutableList(l1, l1);
    Map<Local, Block> argToBlock = new HashMap<>();
    argToBlock.put(l1, actualBlocks.get(1));
    argToBlock.put(l1, actualBlocks.get(2));
    Stmt phiStmt =
        JavaJimple.newAssignStmt(l1, JavaJimple.newPhiExpr(args, argToBlock), noStmtPositionInfo);

    graph.addStmtOnTopOfBlock(phiStmt, actualBlocks.get(3));

    // expected Blocks in BlockGraph
    Block eblock = new Block(phiStmt, ret);
    assertTrue(graph.getBlocks().get(3).equals(eblock));

    assertTrue(graph.getStmtGraph().containsNode(phiStmt));
    assertTrue(graph.getStmtGraph().predecessors(ret).get(0) == phiStmt);
    assertTrue(graph.getStmtGraph().successors(phiStmt).get(0) == ret);

    assertTrue(graph.getStmtGraph().predecessors(phiStmt).get(0) == stmt4);
    assertTrue(graph.getStmtGraph().predecessors(phiStmt).get(1) == stmt6);
    assertTrue(graph.getStmtGraph().successors(stmt4).get(0) == phiStmt);
    assertTrue(graph.getStmtGraph().successors(stmt6).get(0) == phiStmt);
  }

  @Test
  public void testRepalceStmt() {
    Body body = createBBBody();
    BlockGraph graph = new BlockGraph(body.getStmtGraph());
    List<Block> actualBlocks = graph.getBlocks();

    List<Local> args = ImmutableUtils.immutableList(l1, l1);
    Map<Local, Block> argToBlock = new HashMap<>();
    argToBlock.put(l1, actualBlocks.get(1));
    argToBlock.put(l1, actualBlocks.get(2));
    Stmt phiStmt =
        JavaJimple.newAssignStmt(l1, JavaJimple.newPhiExpr(args, argToBlock), noStmtPositionInfo);

    graph.replaceStmtInBlock(stmt6, phiStmt, actualBlocks.get(2));

    // expected Blocks in BlockGraph
    Block eblock = new Block(stmt5, phiStmt);
    assertTrue(graph.getBlocks().get(2).equals(eblock));
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
   */
  private Body createBBBody() {
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

    return builder.build();
  }

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
  private Body createLoopBody() {

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

    return builder.build();
  }

  /**
   *
   *
   * <pre>
   *    l0 := @this Test
   * label1:
   *    l1 = 0
   * label2
   *    l2 = l1 + 1
   *    l3 = 10
   * label3
   *    l4 = l1
   * label4:
   *    return
   * label5:
   *    stack5 := @caughtexception;
   *    goto label4;
   * label6:
   *    stack6 := @caughtexception;
   *    goto label4;
   * </pre>
   */
  private Body createTrapBody() {

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, l3, l4, stack5, stack6);

    builder.setLocals(locals);

    // set graph
    builder.addFlow(startingStmt, stmt1);
    builder.addFlow(stmt1, stmt10);
    builder.addFlow(stmt10, stmt8);
    builder.addFlow(stmt8, stmt7);
    builder.addFlow(stack5Stmt, gotoStmt1);
    builder.addFlow(stack6Stmt, gotoStmt2);
    builder.addFlow(gotoStmt1, ret);
    builder.addFlow(gotoStmt2, ret);
    builder.addFlow(stmt7, ret);

    // build startingStmt
    builder.setStartingStmt(startingStmt);

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    List<Trap> traps = ImmutableUtils.immutableList(trap1, trap2);
    builder.setTraps(traps);

    return builder.build();
  }
}
