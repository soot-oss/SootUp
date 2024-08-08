package sootup.codepropertygraph.cfg;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import sootup.codepropertygraph.GraphTestSuiteBase;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.edges.*;
import sootup.codepropertygraph.propertygraph.nodes.StmtGraphNode;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.SimpleStmtPositionInfo;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.PackageName;
import sootup.core.types.PrimitiveType;
import sootup.java.core.types.JavaClassType;

public class CfgCreatorTest extends GraphTestSuiteBase {
  @Test
  public void testCfgForGotoStmt() {
    SootMethod testMethod = createGotoStmtMethod();
    PropertyGraph cfgGraph = cfgCreator.createGraph(testMethod);
    assertNotNull(cfgGraph);
    verifyEdges(cfgGraph, GotoCfgEdge.class);

    assertGraphStructure(
        cfgGraph,
        new String[] {"JGotoStmt", "JReturnVoidStmt"},
        new String[][] {{"JGotoStmt", "JReturnVoidStmt"}});
  }

  @Test
  public void testCfgForSwitchStmt() {
    SootMethod testMethod = createSwitchStmtMethod();
    PropertyGraph cfgGraph = cfgCreator.createGraph(testMethod);
    assertNotNull(cfgGraph);
    verifyEdges(cfgGraph, SwitchCfgEdge.class);

    assertGraphStructure(
        cfgGraph,
        new String[] {"JSwitchStmt", "JReturnVoidStmt", "JReturnVoidStmt", "JReturnVoidStmt"},
        new String[][] {
          {"JSwitchStmt", "JReturnVoidStmt"},
          {"JSwitchStmt", "JReturnVoidStmt"},
          {"JSwitchStmt", "JReturnVoidStmt"}
        });
  }

  @Test
  public void testCfgForNormalStmt() {
    SootMethod testMethod = createNormalStmtMethod();
    PropertyGraph cfgGraph = cfgCreator.createGraph(testMethod);
    assertNotNull(cfgGraph);
    verifyEdges(cfgGraph, NormalCfgEdge.class);

    assertGraphStructure(
        cfgGraph,
        new String[] {"JAssignStmt", "JReturnVoidStmt"},
        new String[][] {{"JAssignStmt", "JReturnVoidStmt"}});
  }

  @Test
  public void testCfgForExceptionalEdges() {
    SootMethod testMethod = createExceptionalEdgesMethod();
    PropertyGraph cfgGraph = cfgCreator.createGraph(testMethod);
    assertNotNull(cfgGraph);
    verifyEdges(cfgGraph, ExceptionalCfgEdge.class);

    assertGraphStructure(
        cfgGraph,
        new String[] {"JThrowStmt", "JReturnVoidStmt"},
        new String[][] {{"JThrowStmt", "JReturnVoidStmt"}});
  }

  private void assertGraphStructure(
      PropertyGraph cfgGraph, String[] expectedNodeTypes, String[][] expectedEdges) {
    assertEquals(expectedNodeTypes.length, cfgGraph.getNodes().size());
    for (String expectedNodeType : expectedNodeTypes) {
      assertTrue(
          cfgGraph.getNodes().stream()
              .map(node -> (StmtGraphNode) node)
              .anyMatch(
                  node -> node.getStmt().getClass().getSimpleName().equals(expectedNodeType)));
    }

    for (String[] expectedEdge : expectedEdges) {
      assertTrue(
          cfgGraph.getEdges().stream()
              .anyMatch(
                  edge -> {
                    StmtGraphNode src = (StmtGraphNode) edge.getSource();
                    StmtGraphNode dst = (StmtGraphNode) edge.getDestination();
                    return src.getStmt().getClass().getSimpleName().equals(expectedEdge[0])
                        && dst.getStmt().getClass().getSimpleName().equals(expectedEdge[1]);
                  }));
    }
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
        new JavaClassType("CustomException", new PackageName("cfg.exceptions")),
        returnStmt);

    return createSootMethod(stmtGraph, "exceptionalEdgesMethod");
  }
}
