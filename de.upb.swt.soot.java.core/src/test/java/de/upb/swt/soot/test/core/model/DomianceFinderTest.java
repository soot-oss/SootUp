package de.upb.swt.soot.test.core.model;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.graph.Block;
import de.upb.swt.soot.core.graph.BlockGraph;
import de.upb.swt.soot.core.graph.DominanceFinder;
import de.upb.swt.soot.core.graph.DominanceTree;
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
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Zun Wang */
@Category(Java8Test.class)
public class DomianceFinderTest {
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
  public void testImmediateDominator() {
    Body body = createBody();
    BlockGraph graph = new BlockGraph(body.getStmtGraph());
    List<Block> blocks = graph.getBlocks();
    Block expectedBlock1 = blocks.get(0);
    Block expectedBlock2 = blocks.get(1);
    Block expectedBlock3 = blocks.get(2);
    DominanceFinder df = new DominanceFinder(graph);

    for (int i = 0; i < graph.getBlocks().size(); i++) {
      if (i == 0 || i == 1) {
        assertTrue(df.getImmediateDominator(blocks.get(i)) == expectedBlock1);
      } else if (i == 2 || i == 3) {
        assertTrue(df.getImmediateDominator(blocks.get(i)) == expectedBlock2);
      } else {
        assertTrue(df.getImmediateDominator(blocks.get(i)) == expectedBlock3);
      }
    }
  }

  @Test
  public void testDominanceFrontiers() {
    Body body = createBody();
    BlockGraph graph = new BlockGraph(body.getStmtGraph());
    List<Block> blocks = graph.getBlocks();
    Block eblock1 = blocks.get(1);
    Block eblock2 = blocks.get(6);

    DominanceFinder df = new DominanceFinder(graph);

    for (int i = 0; i < graph.getBlocks().size(); i++) {
      if (i == 0 || i == 3) {
        assertTrue(df.getDominanceFrontiers(blocks.get(i)).isEmpty());
      } else if (i == 4 || i == 5) {

        assertTrue(df.getDominanceFrontiers(blocks.get(i)).size() == 1);
        assertTrue(df.getDominanceFrontiers(blocks.get(i)).toArray()[0] == eblock2);
      } else {
        assertTrue(df.getDominanceFrontiers(blocks.get(i)).size() == 1);
        assertTrue(df.getDominanceFrontiers(blocks.get(i)).toArray()[0] == eblock1);
      }
    }
  }

  @Test
  public void testDominanceTree() {
    Body body = createBody();
    BlockGraph graph = new BlockGraph(body.getStmtGraph());
    List<Block> blocks = graph.getBlocks();

    DominanceFinder df = new DominanceFinder(graph);
    for (DominanceTree tree : df.getDominanceTrees()) {
      if (tree.getContent() == blocks.get(0)) {
        assertTrue(tree.getChildren().size() == 1);
        List<DominanceTree> list = new ArrayList<>(tree.getChildren());
        assertTrue(list.get(0).getContent() == blocks.get(1));
      } else if (tree.getContent() == blocks.get(1)) {
        assertTrue(tree.getChildren().size() == 2);
        List<DominanceTree> list = new ArrayList<>(tree.getChildren());
        if (list.get(0).getContent() == blocks.get(3)) {
          assertTrue(list.get(1).getContent() == blocks.get(2));
        } else {
          assertTrue(list.get(1).getContent() == blocks.get(3));
        }
      } else if (tree.getContent() == blocks.get(2)) {
        assertTrue(tree.getChildren().size() == 3);
        Set<Block> contents = new HashSet<>();
        for (DominanceTree dt : tree.getChildren()) {
          contents.add(dt.getContent());
        }
        assertTrue(contents.contains(blocks.get(4)));
        assertTrue(contents.contains(blocks.get(5)));
        assertTrue(contents.contains(blocks.get(6)));

      } else {
        assertTrue(tree.getChildren().size() == 0);
      }
    }
  }

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
