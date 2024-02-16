package sootup.core.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JAddExpr;
import sootup.core.jimple.common.expr.JLeExpr;
import sootup.core.jimple.common.stmt.*;
import sootup.core.types.PrimitiveType;

@Tag("Java8")
public class DominanceFinderTest {

  StmtPositionInfo noPosInfo = StmtPositionInfo.getNoStmtPositionInfo();

  @Test
  public void testDominanceFinder() {
    MutableBlockStmtGraph graph = createStmtGraph();
    Map<BasicBlock<?>, Integer> blockToId = new HashMap<>();
    // assign ids according to getBlocksSorted order
    int i = 0;
    for (BasicBlock<?> block : graph.getBlocksSorted()) {
      blockToId.put(block, i);
      i++;
    }

    DominanceFinder dom = new DominanceFinder(graph);

    Map<Integer, Set<Integer>> expectedFrontiers = new HashMap<>();
    expectedFrontiers.put(0, new HashSet<>());
    expectedFrontiers.put(1, new HashSet<>(Collections.singletonList(1)));
    expectedFrontiers.put(2, new HashSet<>());
    expectedFrontiers.put(3, new HashSet<>(Collections.singletonList(1)));
    expectedFrontiers.put(4, new HashSet<>(Collections.singletonList(6)));
    expectedFrontiers.put(5, new HashSet<>(Collections.singletonList(6)));
    expectedFrontiers.put(6, new HashSet<>(Collections.singletonList(1)));

    Map<Integer, Integer> expectedDominators = new HashMap<>();
    expectedDominators.put(0, 0);
    expectedDominators.put(1, 0);
    expectedDominators.put(2, 1);
    expectedDominators.put(3, 1);
    expectedDominators.put(4, 3);
    expectedDominators.put(5, 3);
    expectedDominators.put(6, 3);

    // check dominators
    for (BasicBlock<?> block : graph.getBlocksSorted()) {
      BasicBlock<?> dominator = dom.getImmediateDominator(block);
      Integer dominatorId = blockToId.get(dominator);
      Integer expectedId = expectedDominators.get(blockToId.get(block));

      assertEquals(expectedId, dominatorId);
    }

    // check frontiers
    for (BasicBlock<?> block : graph.getBlocksSorted()) {
      Set<BasicBlock<?>> frontier = dom.getDominanceFrontiers(block);
      Set<Integer> frontierIds = frontier.stream().map(blockToId::get).collect(Collectors.toSet());
      Set<Integer> expectedIds = expectedFrontiers.get(blockToId.get(block));

      assertEquals(expectedIds, frontierIds);
    }
  }

  @Test
  public void testBlockToIdxInverse() {
    MutableBlockStmtGraph graph = createStmtGraph();
    DominanceFinder dom = new DominanceFinder(graph);

    // check that getBlockToIdx and getIdxToBlock are inverses
    for (BasicBlock<?> block : graph.getBlocksSorted()) {
      List<BasicBlock<?>> idxToBlock = dom.getIdxToBlock();
      Map<BasicBlock<?>, Integer> blockToIdx = dom.getBlockToIdx();
      assertEquals(block, idxToBlock.get(blockToIdx.get(block)));
    }
  }

  private MutableBlockStmtGraph createStmtGraph() {
    // reconstruct the example given in
    // https://soot-oss.github.io/SootUp/v1.1.2/advanced-topics/#dominancefinder.
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();

    Local l1 = new Local("l1", PrimitiveType.IntType.getInstance());
    Local l2 = new Local("l2", PrimitiveType.IntType.getInstance());
    Local l3 = new Local("l3", PrimitiveType.IntType.getInstance());

    JAssignStmt assign01 = new JAssignStmt(l1, IntConstant.getInstance(1), noPosInfo);
    JAssignStmt assign02 = new JAssignStmt(l2, IntConstant.getInstance(2), noPosInfo);
    JAssignStmt assign03 = new JAssignStmt(l2, IntConstant.getInstance(0), noPosInfo);
    JAssignStmt assign41 = new JAssignStmt(l2, l3, noPosInfo);
    JAssignStmt assign42 =
        new JAssignStmt(l3, new JAddExpr(l3, IntConstant.getInstance(2)), noPosInfo);
    JAssignStmt assign51 = new JAssignStmt(l2, l1, noPosInfo);
    JAssignStmt assign52 =
        new JAssignStmt(l3, new JAddExpr(l3, IntConstant.getInstance(1)), noPosInfo);

    BranchingStmt if1 = new JIfStmt(new JLeExpr(l3, IntConstant.getInstance(100)), noPosInfo);
    BranchingStmt if3 = new JIfStmt(new JLeExpr(l2, IntConstant.getInstance(20)), noPosInfo);

    JReturnStmt return2 = new JReturnStmt(l2, noPosInfo);
    JGotoStmt goto4 = new JGotoStmt(noPosInfo);
    JGotoStmt goto5 = new JGotoStmt(noPosInfo);
    JGotoStmt goto6 = new JGotoStmt(noPosInfo);

    // block 0
    graph.setStartingStmt(assign01);
    graph.putEdge(assign01, assign02);
    graph.putEdge(assign02, assign03);
    graph.putEdge(assign03, if1);

    // block 1
    graph.putEdge(if1, JIfStmt.FALSE_BRANCH_IDX, return2);
    graph.putEdge(if1, JIfStmt.TRUE_BRANCH_IDX, if3);

    // block 3
    graph.putEdge(if3, JIfStmt.FALSE_BRANCH_IDX, assign41);
    graph.putEdge(if3, JIfStmt.TRUE_BRANCH_IDX, assign51);

    // block 4
    graph.putEdge(assign41, assign42);
    graph.putEdge(assign42, goto4);
    graph.putEdge(goto4, JGotoStmt.BRANCH_IDX, goto6);

    // block 5
    graph.putEdge(assign51, assign52);
    graph.putEdge(assign52, goto5);
    graph.putEdge(goto5, JGotoStmt.BRANCH_IDX, goto6);

    // block 6
    graph.putEdge(goto6, JGotoStmt.BRANCH_IDX, if1);
    return graph;
  }
}
