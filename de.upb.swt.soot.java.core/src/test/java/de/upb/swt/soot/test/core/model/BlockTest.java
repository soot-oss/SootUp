package de.upb.swt.soot.test.core.model;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.graph.Block;
import de.upb.swt.soot.core.graph.BlockGraph;
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
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Zun Wang */
@Category(Java8Test.class)
public class BlockTest {

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
  Local l4 = JavaJimple.newLocal("l4", intType);

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

  @Test
  public void testBlock() {
    Body body = createBBBody();
    Block block = new Block(startingStmt, stmt2, body);

    assertTrue(block.getBlockLength() == 3);
    List<Stmt> expected = ImmutableUtils.immutableList(startingStmt, stmt1, stmt2);
    assertArrayEquals(expected.toArray(), block.getBlockStmts().toArray());

    String expectedString = "[ l0 := @this: Test\n" + "  l1 = 0\n" + "  if l1 >= 0 ]";
    assertEquals(expectedString, block.toString());
  }

  @Test
  public void testBlockGraphWithBranch() {
    Body body = createBBBody();
    BlockGraph graph = new BlockGraph(body);

    // expected Blocks in BlockGraph
    Block eblock1 = new Block(startingStmt, stmt2, body);
    Block eblock2 = new Block(stmt3, stmt4, body);
    Block eblock3 = new Block(stmt5, stmt6, body);
    Block eblock4 = new Block(ret, ret, body);
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
        assertTrue(graph.predecessors(stmt).isEmpty());
        assertTrue(graph.successors(stmt).get(0).equals(eblock2));
        assertTrue(graph.successors(stmt).get(1).equals(eblock3));
        assertTrue(graph.getBlock(stmt).equals(eblock1));
      } else if (i < 5) {
        assertTrue(graph.predecessors(stmt).get(0).equals(eblock1));
        assertTrue(graph.successors(stmt).get(0).equals(eblock4));
        assertTrue(graph.getBlock(stmt).equals(eblock2));
      } else if (i < 7) {
        assertTrue(graph.predecessors(stmt).get(0).equals(eblock1));
        assertTrue(graph.successors(stmt).get(0).equals(eblock4));
        assertTrue(graph.getBlock(stmt).equals(eblock3));
      } else {
        assertTrue(graph.predecessors(stmt).get(0).equals(eblock2));
        assertTrue(graph.predecessors(stmt).get(1).equals(eblock3));
        assertTrue(graph.successors(stmt).isEmpty());
        assertTrue(graph.getBlock(stmt).equals(eblock4));
      }
    }
  }

  @Test
  public void testBlockGraphWithLoop() {
    Body body = createLoopBody();
    BlockGraph graph = new BlockGraph(body);

    // expected Blocks in BlockGraph
    Block eblock1 = new Block(startingStmt, stmt1, body);
    Block eblock2 = new Block(stmt7, stmt9, body);
    Block eblock3 = new Block(stmt10, stmt4, body);
    Block eblock4 = new Block(ret, ret, body);
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
        assertTrue(graph.predecessors(stmt).isEmpty());
        assertTrue(graph.successors(stmt).get(0).equals(eblock2));
        assertTrue(graph.getBlock(stmt).equals(eblock1));
      } else if (i < 5) {
        assertTrue(graph.predecessors(stmt).get(0).equals(eblock1));
        assertTrue(graph.predecessors(stmt).get(1).equals(eblock3));
        assertTrue(graph.successors(stmt).get(0).equals(eblock3));
        assertTrue(graph.successors(stmt).get(1).equals(eblock4));
        assertTrue(graph.getBlock(stmt).equals(eblock2));
      } else if (i < 9) {
        assertTrue(graph.predecessors(stmt).get(0).equals(eblock2));
        assertTrue(graph.successors(stmt).get(0).equals(eblock2));
        assertTrue(graph.getBlock(stmt).equals(eblock3));
      } else {
        assertTrue(graph.predecessors(stmt).get(0).equals(eblock2));
        assertTrue(graph.successors(stmt).isEmpty());
        assertTrue(graph.getBlock(stmt).equals(eblock4));
      }
    }
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
}
