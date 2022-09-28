package de.upb.swt.soot.test.core.model;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.graph.*;
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.ref.IdentityRef;
import de.upb.swt.soot.core.jimple.common.stmt.BranchingStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.GraphVizExporter;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.*;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Zun Wang */
@Category(Java8Test.class)
@Ignore("ms: FIX IT")
public class DominanceFinderTest {
  // Preparation
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  JavaJimple javaJimple = JavaJimple.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

  JavaClassType intType = factory.getClassType("int");
  JavaClassType classType = factory.getClassType("Test");
  IdentityRef identityRef = JavaJimple.newThisRef(classType);
  IdentityRef caughtExceptionRef = javaJimple.newCaughtExceptionRef();
  ClassType exception = factory.getClassType("Exception");
  JavaClassType refType = factory.getClassType("ref");

  // build locals
  Local l0 = JavaJimple.newLocal("l0", intType);
  Local l1 = JavaJimple.newLocal("l1", intType);
  Local l2 = JavaJimple.newLocal("l2", intType);
  Local l3 = JavaJimple.newLocal("l3", intType);
  Local stack4 = JavaJimple.newLocal("stack4", refType);

  Stmt startingStmt = JavaJimple.newIdentityStmt(l0, identityRef, noStmtPositionInfo);
  Stmt l1assign1 = JavaJimple.newAssignStmt(l1, IntConstant.getInstance(1), noStmtPositionInfo);
  Stmt l2assign1 = JavaJimple.newAssignStmt(l2, IntConstant.getInstance(1), noStmtPositionInfo);
  Stmt l3assign0 = JavaJimple.newAssignStmt(l3, IntConstant.getInstance(0), noStmtPositionInfo);
  BranchingStmt jIfStmt2 =
      JavaJimple.newIfStmt(
          JavaJimple.newLtExpr(l3, IntConstant.getInstance(100)), noStmtPositionInfo);
  BranchingStmt jIfStmt1 =
      JavaJimple.newIfStmt(
          JavaJimple.newLtExpr(l2, IntConstant.getInstance(20)), noStmtPositionInfo);
  Stmt jReturnStmt = JavaJimple.newReturnStmt(l2, noStmtPositionInfo);
  Stmt l2assignl1 = JavaJimple.newAssignStmt(l2, l1, noStmtPositionInfo);
  Stmt l3assignl3plus1 =
      JavaJimple.newAssignStmt(
          l3, JavaJimple.newAddExpr(l3, IntConstant.getInstance(1)), noStmtPositionInfo);
  Stmt l2assignl3 = JavaJimple.newAssignStmt(l2, l3, noStmtPositionInfo);
  Stmt l3assignl3plus2 =
      JavaJimple.newAssignStmt(
          l3, JavaJimple.newAddExpr(l3, IntConstant.getInstance(2)), noStmtPositionInfo);
  BranchingStmt jGotoStmt2 = JavaJimple.newGotoStmt(noStmtPositionInfo);
  BranchingStmt jGotoStmt1 = JavaJimple.newGotoStmt(noStmtPositionInfo);
  Stmt stack4Stmt = JavaJimple.newIdentityStmt(stack4, caughtExceptionRef, noStmtPositionInfo);
  Stmt l2assign0 = JavaJimple.newAssignStmt(l2, IntConstant.getInstance(0), noStmtPositionInfo);
  Trap trap = new Trap(exception, l2assignl1, l3assignl3plus1, stack4Stmt);

  @Test
  public void testImmediateDominator() {
    MutableBlockStmtGraph graph = createGraph();
    List<? extends BasicBlock<?>> blocks = graph.getBlocksSorted();
    BasicBlock<?> expectedBlock1 = blocks.get(0);
    BasicBlock<?> expectedBlock2 = blocks.get(1);
    BasicBlock<?> expectedBlock3 = blocks.get(3);
    DominanceFinder df = new DominanceFinder(graph);

    System.out.println(GraphVizExporter.createUrlToWebeditor(graph));
    for (int i = 0; i < blocks.size(); i++) {
      System.out.println(
          i + ": " + blocks.get(i) + " => " + df.getImmediateDominator(blocks.get(i)));
    }

    for (int i = 0; i < blocks.size(); i++) {
      switch (i) {
        case 0:
        case 1:
          assertSame(df.getImmediateDominator(blocks.get(i)), expectedBlock1);
          break;
        case 2:
        case 3:
          assertSame(df.getImmediateDominator(blocks.get(i)), expectedBlock2);
          break;
        default:
          assertSame(df.getImmediateDominator(blocks.get(i)), expectedBlock3);
          break;
      }
    }
  }

  @Test
  public void testDominanceFrontiers() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph(createGraph());
    List<? extends BasicBlock<?>> blocks = graph.getBlocksSorted();
    DominanceFinder df = new DominanceFinder(graph);

    System.out.println(GraphVizExporter.createUrlToWebeditor(graph));
    for (int i = 0; i < blocks.size(); i++) {
      System.out.println(
          i + ": " + blocks.get(i) + " => " + df.getImmediateDominator(blocks.get(i)));
    }

    BasicBlock<?> eblock1 = blocks.get(1);
    BasicBlock<?> eblock2 = blocks.get(6);

    for (int i = 0; i < blocks.size(); i++) {
      switch (i) {
        case 0:
        case 2:
          assertTrue(df.getDominanceFrontiers(blocks.get(i)).isEmpty());
          break;
        case 4:
        case 5:
          assertEquals(1, df.getDominanceFrontiers(blocks.get(i)).size());
          assertSame(df.getDominanceFrontiers(blocks.get(i)).toArray()[0], eblock2);
          break;
        default:
          assertEquals(1, df.getDominanceFrontiers(blocks.get(i)).size());
          assertSame(df.getDominanceFrontiers(blocks.get(i)).toArray()[0], eblock1);
          break;
      }
    }
  }

  @Test
  public void testDominanceTree() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph(createGraph());
    List<? extends BasicBlock<?>> blocks = graph.getBlocksSorted();

    DominanceFinder df = new DominanceFinder(graph);
    DominanceTree tree = new DominanceTree(df);
    assertSame(blocks.get(0), tree.getRoot());
    for (int i = 0; i < blocks.size(); i++) {
      BasicBlock<?> block = blocks.get(i);
      switch (i) {
        case 0:
          assertEquals(1, tree.getChildren(block).size());
          assertNull(tree.getParent(block));
          break;
        case 1:
          assertEquals(2, tree.getChildren(block).size());
          assertSame(tree.getParent(block), blocks.get(0));
          break;
        case 3:
          assertEquals(3, tree.getChildren(block).size());
          assertSame(tree.getParent(block), blocks.get(1));
          break;
        case 2:
          assertEquals(0, tree.getChildren(block).size());
          assertSame(tree.getParent(block), blocks.get(1));
          break;
        default:
          assertEquals(0, tree.getChildren(block).size());
          assertSame(tree.getParent(block), blocks.get(3));
          break;
      }
    }
  }

  @Test
  public void testImmediateDominatorWithTrap() {
    MutableBlockStmtGraph graph = createTrapGraph();
    List<? extends BasicBlock<?>> blocks = graph.getBlocksSorted();
    BasicBlock<?> expectedBlock1 = blocks.get(0);
    BasicBlock<?> expectedBlock2 = blocks.get(1);
    BasicBlock<?> expectedBlock3 = blocks.get(3);
    BasicBlock<?> expectedBlock4 = blocks.get(5);
    DominanceFinder df = new DominanceFinder(graph);

    for (int i = 0; i < blocks.size(); i++) {
      switch (i) {
        case 0:
        case 1:
          assertSame(df.getImmediateDominator(blocks.get(i)), expectedBlock1);
          break;
        case 2:
        case 3:
          assertSame(df.getImmediateDominator(blocks.get(i)), expectedBlock2);
          break;
        case 4:
        case 5:
        case 8:
          assertSame(df.getImmediateDominator(blocks.get(i)), expectedBlock3);
          break;
        default:
          assertSame(df.getImmediateDominator(blocks.get(i)), expectedBlock4);
          break;
      }
    }
  }

  @Test
  public void testDominanceFrontiersWithTrap() {
    MutableBlockStmtGraph graph = createTrapGraph();
    List<? extends BasicBlock<?>> blocks = graph.getBlocksSorted();
    BasicBlock<?> eblock1 = blocks.get(1);
    BasicBlock<?> eblock2 = blocks.get(7);
    BasicBlock<?> eblock3 = blocks.get(8);
    DominanceFinder df = new DominanceFinder(graph);

    System.out.println(GraphVizExporter.createUrlToWebeditor(graph));
    for (int i = 0; i < blocks.size(); i++) {
      System.out.println(
          i + ": " + blocks.get(i) + " => " + df.getImmediateDominator(blocks.get(i)));
    }

    for (int i = 0; i < blocks.size(); i++) {
      switch (i) {
        case 0:
        case 2:
          assertTrue(df.getDominanceFrontiers(blocks.get(i)).isEmpty());
          break;
        case 1:
        case 3:
        case 8:
          assertEquals(1, df.getDominanceFrontiers(blocks.get(i)).size());
          assertSame(df.getDominanceFrontiers(blocks.get(i)).toArray()[0], eblock1);
          break;
        case 6:
          assertEquals(1, df.getDominanceFrontiers(blocks.get(i)).size());
          assertSame(df.getDominanceFrontiers(blocks.get(i)).toArray()[0], eblock2);
          break;
        default:
          assertEquals(1, df.getDominanceFrontiers(blocks.get(i)).size());
          assertSame(df.getDominanceFrontiers(blocks.get(i)).toArray()[0], eblock3);
          break;
      }
    }
  }

  @Test
  public void testDominanceTreeWithTrap() {
    MutableBlockStmtGraph graph = createTrapGraph();
    List<? extends BasicBlock<?>> blocks = graph.getBlocksSorted();

    DominanceFinder df = new DominanceFinder(graph);
    DominanceTree tree = new DominanceTree(df);
    assertSame(blocks.get(0), tree.getRoot());
    for (int i = 0; i < blocks.size(); i++) {
      BasicBlock<?> block = blocks.get(i);
      switch (i) {
        case 0:
          assertEquals(1, tree.getChildren(block).size());
          assertNull(tree.getParent(block));
          break;
        case 1:
          assertEquals(2, tree.getChildren(block).size());
          assertSame(tree.getParent(block), blocks.get(0));
          break;
        case 2:
          assertEquals(0, tree.getChildren(block).size());
          assertSame(tree.getParent(block), blocks.get(1));
          break;
        case 3:
          assertEquals(3, tree.getChildren(block).size());
          assertSame(tree.getParent(block), blocks.get(1));
          break;
        case 4:
        case 8:
          assertEquals(0, tree.getChildren(block).size());
          assertSame(tree.getParent(block), blocks.get(3));
          break;
        case 5:
          assertEquals(2, tree.getChildren(block).size());
          assertSame(tree.getParent(block), blocks.get(3));
          break;
        default:
          assertEquals(0, tree.getChildren(block).size());
          assertSame(tree.getParent(block), blocks.get(5));
          break;
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
   *    if l2 < 20 goto label2
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
  private MutableBlockStmtGraph createGraph() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();

    final HashMap<BranchingStmt, List<Stmt>> branchingMap = new HashMap<>();
    branchingMap.put(jIfStmt2, Collections.singletonList(jReturnStmt));
    branchingMap.put(jIfStmt1, Collections.singletonList(l2assignl3));
    branchingMap.put(jGotoStmt1, Collections.singletonList(jIfStmt2));

    graph.initializeWith(
        Arrays.asList(
            startingStmt,
            l1assign1,
            l2assign1,
            l3assign0,
            jIfStmt2,
            jIfStmt1,
            l2assignl1,
            l3assignl3plus1,
            jGotoStmt1,
            l2assignl3,
            l3assignl3plus2,
            jReturnStmt),
        branchingMap,
        Collections.emptyList());

    return graph;
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
   *    if l3 < 100 goto label6
   *    if l2 < 20 goto label5
   * label2:
   *    l2 = l1
   * label3:
   *    l3 = l3 + 1
   *    goto label1;
   * label4:
   *    stack4 := @caughtexception
   *    l2 = 0;
   *    goto label3;
   * label5:
   *    l2 = l3
   *    l3 = l3 + 2
   * label6:
   *    return l2
   *
   * catch Exception from label2 to label3 with label4;
   * </pre>
   */
  private MutableBlockStmtGraph createTrapGraph() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();

    final HashMap<BranchingStmt, List<Stmt>> branchingMap = new HashMap<>();
    branchingMap.put(jIfStmt2, Collections.singletonList(jReturnStmt));
    branchingMap.put(jIfStmt1, Collections.singletonList(l2assignl3));
    branchingMap.put(jGotoStmt1, Collections.singletonList(jIfStmt2));
    branchingMap.put(jGotoStmt2, Collections.singletonList(l3assignl3plus1));

    graph.initializeWith(
        Arrays.asList(
            startingStmt,
            l1assign1,
            l2assign1,
            l3assign0,
            jIfStmt2,
            jIfStmt1,
            l2assignl1,
            l3assignl3plus1,
            jGotoStmt1,
            stack4Stmt,
            l2assign0,
            jGotoStmt2,
            l2assignl3,
            l3assignl3plus2,
            jReturnStmt),
        branchingMap,
        Collections.singletonList(trap));

    return graph;
  }
}