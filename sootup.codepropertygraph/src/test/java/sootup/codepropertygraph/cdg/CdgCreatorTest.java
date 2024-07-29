package sootup.codepropertygraph.cdg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import sootup.codepropertygraph.GraphTestSuiteBase;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.edges.CdgEdge;
import sootup.codepropertygraph.propertygraph.nodes.StmtGraphNode;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.SimpleStmtPositionInfo;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JEqExpr;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.PackageName;
import sootup.core.types.PrimitiveType;
import sootup.java.core.types.JavaClassType;

public class CdgCreatorTest extends GraphTestSuiteBase {

  @Test
  public void testCdgForIfStmt() {
    SootMethod testMethod = createIfStmtMethod();
    PropertyGraph cdgGraph = cdgCreator.createGraph(testMethod);
    assertNotNull(cdgGraph);
    verifyEdges(cdgGraph, CdgEdge.class);

    assertEquals(3, cdgGraph.getNodes().size());
    assertTrue(
        cdgGraph.getNodes().stream()
            .map(node -> (StmtGraphNode) node)
            .anyMatch(node -> node.getStmt() instanceof JIfStmt));
    assertTrue(
        cdgGraph.getNodes().stream()
            .map(node -> (StmtGraphNode) node)
            .anyMatch(node -> node.getStmt() instanceof JReturnVoidStmt));
  }

  @Test
  public void testCdgForGotoStmt() {
    SootMethod testMethod = createGotoStmtMethod();
    PropertyGraph cdgGraph = cdgCreator.createGraph(testMethod);
    assertNotNull(cdgGraph);
    verifyEdges(cdgGraph, CdgEdge.class);

    assertEquals(0, cdgGraph.getNodes().size());
  }

  @Test
  public void testCdgForSwitchStmt() {
    SootMethod testMethod = createSwitchStmtMethod();
    PropertyGraph cdgGraph = cdgCreator.createGraph(testMethod);
    assertNotNull(cdgGraph);
    verifyEdges(cdgGraph, CdgEdge.class);

    assertEquals(4, cdgGraph.getNodes().size());
    assertTrue(
        cdgGraph.getNodes().stream()
            .map(node -> (StmtGraphNode) node)
            .anyMatch(node -> node.getStmt() instanceof JSwitchStmt));
    assertTrue(
        cdgGraph.getNodes().stream()
            .map(node -> (StmtGraphNode) node)
            .anyMatch(node -> node.getStmt() instanceof JReturnVoidStmt));
  }

  @Test
  public void testCdgForNormalStmt() {
    SootMethod testMethod = createNormalStmtMethod();
    PropertyGraph cdgGraph = cdgCreator.createGraph(testMethod);
    assertNotNull(cdgGraph);
    verifyEdges(cdgGraph, CdgEdge.class);

    assertEquals(2, cdgGraph.getNodes().size());
    assertTrue(
        cdgGraph.getNodes().stream()
            .map(node -> (StmtGraphNode) node)
            .anyMatch(node -> node.getStmt() instanceof JAssignStmt));
    assertTrue(
        cdgGraph.getNodes().stream()
            .map(node -> (StmtGraphNode) node)
            .anyMatch(node -> node.getStmt() instanceof JReturnVoidStmt));
  }

  @Test
  public void testCdgForExceptionalEdges() {
    SootMethod testMethod = createExceptionalEdgesMethod();
    PropertyGraph cdgGraph = cdgCreator.createGraph(testMethod);
    assertNotNull(cdgGraph);
    verifyEdges(cdgGraph, CdgEdge.class);

    assertEquals(2, cdgGraph.getNodes().size());
    assertTrue(
        cdgGraph.getNodes().stream()
            .map(node -> (StmtGraphNode) node)
            .anyMatch(node -> node.getStmt() instanceof JThrowStmt));
    assertTrue(
        cdgGraph.getNodes().stream()
            .map(node -> (StmtGraphNode) node)
            .anyMatch(node -> node.getStmt() instanceof JReturnVoidStmt));
  }

  private SootMethod createIfStmtMethod() {
    MutableStmtGraph stmtGraph = new MutableBlockStmtGraph();
    Local a = Jimple.newLocal("a", PrimitiveType.IntType.getInstance());
    Local b = Jimple.newLocal("b", PrimitiveType.IntType.getInstance());
    JIfStmt ifStmt = Jimple.newIfStmt(new JEqExpr(a, b), new SimpleStmtPositionInfo(1));
    JReturnVoidStmt trueStmt = Jimple.newReturnVoidStmt(new SimpleStmtPositionInfo(2));
    JReturnVoidStmt falseStmt = new JReturnVoidStmt(new SimpleStmtPositionInfo(3));

    stmtGraph.addBlock(Collections.singletonList(ifStmt));
    stmtGraph.addBlock(Collections.singletonList(trueStmt));
    stmtGraph.addBlock(Collections.singletonList(falseStmt));
    stmtGraph.setStartingStmt(ifStmt);
    stmtGraph.putEdge(ifStmt, JIfStmt.TRUE_BRANCH_IDX, trueStmt);
    stmtGraph.putEdge(ifStmt, JIfStmt.FALSE_BRANCH_IDX, falseStmt);

    return createSootMethod(stmtGraph, "ifStmtMethod");
  }

  private SootMethod createGotoStmtMethod() {
    MutableStmtGraph stmtGraph = new MutableBlockStmtGraph();
    JReturnVoidStmt targetStmt = new JReturnVoidStmt(StmtPositionInfo.getNoStmtPositionInfo());
    JGotoStmt gotoStmt = new JGotoStmt(targetStmt.getPositionInfo());

    stmtGraph.addBlock(Collections.singletonList(gotoStmt));
    stmtGraph.addBlock(Collections.singletonList(targetStmt));
    stmtGraph.setStartingStmt(gotoStmt);
    stmtGraph.putEdge(gotoStmt, JGotoStmt.BRANCH_IDX, targetStmt);

    return createSootMethod(stmtGraph, "gotoStmtMethod");
  }

  private SootMethod createSwitchStmtMethod() {
    MutableStmtGraph stmtGraph = new MutableBlockStmtGraph();
    Local a = Jimple.newLocal("a", PrimitiveType.IntType.getInstance());
    JSwitchStmt switchStmt =
        Jimple.newTableSwitchStmt(IntConstant.getInstance(23), 1, 2, new SimpleStmtPositionInfo(1));

    JReturnVoidStmt target1 = Jimple.newReturnVoidStmt(new SimpleStmtPositionInfo(2));
    JReturnVoidStmt target2 = Jimple.newReturnVoidStmt(new SimpleStmtPositionInfo(3));
    JReturnVoidStmt defaultTarget =
        Jimple.newReturnVoidStmt(StmtPositionInfo.getNoStmtPositionInfo());

    stmtGraph.addBlock(Collections.singletonList(switchStmt));
    stmtGraph.addBlock(Collections.singletonList(target1));
    stmtGraph.addBlock(Collections.singletonList(target2));
    stmtGraph.addBlock(Collections.singletonList(defaultTarget));
    stmtGraph.setStartingStmt(switchStmt);

    int ctr = 0;
    for (Stmt target : Arrays.asList(target1, target2)) {
      stmtGraph.addBlock(Collections.singletonList(target));
      stmtGraph.putEdge(switchStmt, ctr, target);
      ctr++;
    }
    stmtGraph.putEdge(switchStmt, ctr, defaultTarget);

    return createSootMethod(stmtGraph, "switchStmtMethod");
  }

  private SootMethod createNormalStmtMethod() {
    MutableStmtGraph stmtGraph = new MutableBlockStmtGraph();
    JAssignStmt assignStmt =
        Jimple.newAssignStmt(
            Jimple.newLocal("a", PrimitiveType.IntType.getInstance()),
            IntConstant.getInstance(1),
            StmtPositionInfo.getNoStmtPositionInfo());
    JReturnVoidStmt returnStmt = new JReturnVoidStmt(StmtPositionInfo.getNoStmtPositionInfo());

    stmtGraph.addBlock(Arrays.asList(assignStmt, returnStmt));
    stmtGraph.setStartingStmt(assignStmt);

    return createSootMethod(stmtGraph, "normalStmtMethod");
  }

  private SootMethod createExceptionalEdgesMethod() {
    MutableStmtGraph stmtGraph = new MutableBlockStmtGraph();
    JThrowStmt throwStmt =
        new JThrowStmt(
            Jimple.newLocal("exception", PrimitiveType.IntType.getInstance()),
            StmtPositionInfo.getNoStmtPositionInfo());
    JReturnVoidStmt returnStmt = new JReturnVoidStmt(StmtPositionInfo.getNoStmtPositionInfo());

    stmtGraph.addBlock(Collections.singletonList(throwStmt));
    stmtGraph.addBlock(Collections.singletonList(returnStmt));
    stmtGraph.setStartingStmt(throwStmt);
    stmtGraph.addExceptionalEdge(
        throwStmt,
        new JavaClassType("CustomException", new PackageName("cdg.exceptions")),
        returnStmt);

    return createSootMethod(stmtGraph, "exceptionalEdgesMethod");
  }
}
