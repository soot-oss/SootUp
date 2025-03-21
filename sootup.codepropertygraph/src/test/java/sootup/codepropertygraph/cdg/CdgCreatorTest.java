package sootup.codepropertygraph.cdg;

import static org.junit.jupiter.api.Assertions.*;

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
import sootup.core.types.PrimitiveType;

public class CdgCreatorTest extends GraphTestSuiteBase {

  @Test
  public void testCdgForIfStmt() {
    SootMethod testMethod = createIfStmtMethod();
    PropertyGraph cdgGraph = cdgCreator.createGraph(testMethod);
    assertNotNull(cdgGraph);
    verifyEdges(cdgGraph, CdgEdge.class);

    assertGraphStructure(
        cdgGraph,
        new String[] {"JIfStmt", "JReturnVoidStmt"},
        new String[][] {
          {"JIfStmt", "JReturnVoidStmt"},
          {"JIfStmt", "JReturnVoidStmt"}
        });
  }

  @Test
  public void testCdgForGotoStmt() {
    SootMethod testMethod = createGotoStmtMethod();
    PropertyGraph cdgGraph = cdgCreator.createGraph(testMethod);
    assertNotNull(cdgGraph);
    verifyEdges(cdgGraph, CdgEdge.class);

    assertGraphStructure(cdgGraph, new String[] {}, new String[][] {});
  }

  @Test
  public void testCdgForSwitchStmt() {
    SootMethod testMethod = createSwitchStmtMethod();
    PropertyGraph cdgGraph = cdgCreator.createGraph(testMethod);
    assertNotNull(cdgGraph);
    verifyEdges(cdgGraph, CdgEdge.class);

    assertGraphStructure(
        cdgGraph,
        new String[] {"JSwitchStmt", "JReturnVoidStmt", "JReturnVoidStmt"},
        new String[][] {
          {"JSwitchStmt", "JReturnVoidStmt"},
          {"JSwitchStmt", "JReturnVoidStmt"},
          {"JSwitchStmt", "JReturnVoidStmt"}
        });
  }

  private void assertGraphStructure(
      PropertyGraph cdgGraph, String[] expectedNodeTypes, String[][] expectedEdges) {
    assertEquals(expectedNodeTypes.length, cdgGraph.getNodes().size());
    for (String expectedNodeType : expectedNodeTypes) {
      assertTrue(
          cdgGraph.getNodes().stream()
              .map(node -> (StmtGraphNode) node)
              .anyMatch(
                  node -> node.getStmt().getClass().getSimpleName().equals(expectedNodeType)));
    }

    for (String[] expectedEdge : expectedEdges) {
      assertTrue(
          cdgGraph.getEdges().stream()
              .anyMatch(
                  edge -> {
                    StmtGraphNode src = (StmtGraphNode) edge.getSource();
                    StmtGraphNode dst = (StmtGraphNode) edge.getDestination();
                    return src.getStmt().getClass().getSimpleName().equals(expectedEdge[0])
                        && dst.getStmt().getClass().getSimpleName().equals(expectedEdge[1]);
                  }));
    }
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
}
